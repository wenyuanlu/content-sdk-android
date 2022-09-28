package com.maishuo.haohai.widgets.refresh;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import androidx.core.content.ContextCompat;

import com.maishuo.haohai.R;

public class EmptyView extends LinearLayout {
    private ViewSwitcher layout_empty;
    private TextView text_status;
    private View bindView;// 被绑定的view
    private OnClickListener onReloadListener;
    private long exceptionTime = 0;
    private TextView clickRefresh;
    private int emptyDataResId = 0;

    public EmptyView(Context context) {
        this(context, null);
    }

    public EmptyView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    private void initView() {
        LayoutInflater.from(getContext()).inflate(R.layout.view_recycler_empty, this);
        layout_empty = findViewById(R.id.empty_view);
        text_status = findViewById(R.id.text_status);
        clickRefresh = findViewById(R.id.tv_refresh);
        layout_empty.setDisplayedChild(0);
        layout_empty.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (exceptionTime > System.currentTimeMillis() - 1500) {
                    return;
                }
                exceptionTime = System.currentTimeMillis();

                if (onReloadListener == null) {
                    return;
                }
                onReloadListener.onClick(v);
            }
        });
        loadSuccess();
    }

    public void setEmptyViewHeight(int height) {
        LayoutParams params = (LayoutParams) layout_empty.getLayoutParams();
//        params.height = UIUtil.dip2px(getContext(),height);
        layout_empty.setLayoutParams(params);
    }

    public void bind(View view) {
        this.bindView = view;
    }

    public void loadSuccess() {
        setVisibility(View.GONE);
        if (bindView != null)
            bindView.setVisibility(View.VISIBLE);
    }

    public void loading() {
        layout_empty.setDisplayedChild(0);
        setVisibility(View.VISIBLE);
        if (bindView != null)
            bindView.setVisibility(View.GONE);
    }

    //空数据，加载数据失败，网络异常
    public void loadFail(String tip) {
        if (bindView != null) {
            layout_empty.setDisplayedChild(1);
            setVisibility(View.VISIBLE);
            bindView.setVisibility(View.GONE);
            text_status.setText(tip);
            int drawableId = R.mipmap.common_empty_icon;
            text_status.setCompoundDrawablesWithIntrinsicBounds(null, ContextCompat.getDrawable(getContext(), drawableId), null, null);
        }
    }

    @Override
    public void setBackgroundColor(int color) {
        super.setBackgroundColor(color);
        layout_empty.setBackgroundColor(color);
    }

    public void setOnReloadListener(OnClickListener onReloadListener) {
        this.onReloadListener = onReloadListener;
    }

    public void setCannotRefresh(){
        clickRefresh.setVisibility(View.GONE);
    }

    public void setEmptyDataResId(int resId) {
        emptyDataResId = resId;
    }

    public TextView getTipsTextView() {
        return text_status;
    }
}
