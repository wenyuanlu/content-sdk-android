package com.maishuo.haohai.widgets;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.maishuo.haohai.R;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class AutoScrollView extends FrameLayout {

    private ScrollHandler mHandler;
    private final MyAdapter mAdapter;
    private final RecyclerView recyclerView;
    private int mScrollLeft;
    private int mScrollTop;
    private int mScrollRight;
    private int mScrollBottom;

    public AutoScrollView(@NonNull Context context) {
        this(context, null);
    }

    public AutoScrollView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AutoScrollView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        View.inflate(context, R.layout.view_scroll, this);
        mHandler = new ScrollHandler(this);
        mAdapter = new MyAdapter();
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setAdapter(mAdapter);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        //防止布局重置时重置FloatingView的位置
        ViewParent parent = getParent();
        if (parent instanceof ViewGroup) {
            ((ViewGroup) getParent()).addOnLayoutChangeListener(new OnLayoutChangeListener() {
                @Override
                public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                    if (mScrollBottom != 0)
                        layout(mScrollLeft, mScrollTop, mScrollRight, mScrollBottom);
                }
            });
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return false;       //拦截事件
    }

    /**
     * 重新设置位置
     */
    public void setLayout(int l, int b) {
        mScrollTop = b;
        mScrollBottom = b + 249;//83dp
        if (l == 0) {
            mScrollLeft = 0;
            mScrollRight = getRight();
        } else {
            mScrollLeft = getScreenWidth() - 486;//162dp
            mScrollRight = getScreenWidth();
        }
        layout(mScrollLeft, mScrollTop, mScrollRight, mScrollBottom);
    }

    public void setData(List<String> data) {
        mAdapter.setList(data);
        if (data != null && data.size() > 0) {
            mHandler.sendEmptyMessageDelayed(0, 1500);
        }
    }

    public void smoothScroll() {
        recyclerView.smoothScrollBy(0, 87);//29dp
        mHandler.sendEmptyMessageDelayed(0, 1500);
    }

    public void removeCallback() {
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
            mHandler = null;
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        removeCallback();
    }

    /**
     * 弱引用防止内存泄露
     */
    private static class ScrollHandler extends Handler {
        private final WeakReference<AutoScrollView> view;

        public ScrollHandler(AutoScrollView mView) {
            view = new WeakReference<>(mView);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (view.get() != null) {
                view.get().smoothScroll();
            }
        }
    }

    private static class MyAdapter extends RecyclerView.Adapter<ViewHolder> {

        private final List<String> list;

        public MyAdapter() {
            list = new ArrayList<>();
        }

        @SuppressLint("NotifyDataSetChanged")
        public void setList(List<String> list) {
            this.list.clear();
            this.list.addAll(list);
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_scroll_item, parent, false);
            return new ViewHolder(view);
        }

        @SuppressLint("RecyclerView")
        @Override
        public void onBindViewHolder(ViewHolder holder, final int position) {
            holder.bindData(list.get(position % list.size()));
            holder.contentView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.e("-----------", list.get(position % list.size()));
                }
            });

        }

        @Override
        public int getItemCount() {
            return list.size() > 0 ? Integer.MAX_VALUE : 0;
        }
    }

    private static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView contentView;

        public ViewHolder(View itemView) {
            super(itemView);
            contentView = itemView.findViewById(R.id.content);
        }

        public void bindData(String content) {
            contentView.setText(content);
        }
    }

    /**
     * 得到屏幕宽度
     */
    private int getScreenWidth() {
        WindowManager windowManager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics.widthPixels;
    }

}
