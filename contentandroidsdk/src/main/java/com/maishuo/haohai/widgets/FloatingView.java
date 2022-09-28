package com.maishuo.haohai.widgets;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.maishuo.haohai.R;

/**
 * author ：Seven
 * date : 2022/3/11
 * description :
 */
public class FloatingView extends FrameLayout {
    private static final String TAG = "FloatingView";
    private float mX;
    private float mY;
    private int mParentWidth;
    private int mParentHeight;
    private boolean mScrollEnable = true;
    private int mScrollLeft = 1;//给个值默认在右边，如果控件在左边就不需要给值
    private int mScrollTop;
    private int mRight;
    private int mScrollRight;
    private int mScrollBottom;
    private boolean hasScroll;
    boolean isScroll = false;
    private boolean isAdsorb = true;//是否自动吸附到两边
    private FloatingViewListener listener;//回调

    public FloatingView(Context context) {
        this(context, null);
    }

    public FloatingView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    @SuppressLint("CustomViewStyleable")
    public FloatingView(Context context, AttributeSet attrs, final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.FloatingView);
        mScrollEnable = ta.getBoolean(R.styleable.FloatingView_scrollEnable, true);
        ta.recycle();
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
                    if (hasScroll && mScrollRight != 0 && mScrollBottom != 0)
                        layout(mScrollLeft, mScrollTop, mScrollRight, mScrollBottom);
                }
            });
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (getParent() instanceof ViewGroup) {
            mParentWidth = ((ViewGroup) getParent()).getWidth();
            mParentHeight = ((ViewGroup) getParent()).getHeight();
        }
        Log.i(TAG, "onSizeChanged: " + mParentWidth + "---" + mParentHeight);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (!mScrollEnable) return super.onTouchEvent(ev);
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mX = ev.getX();
                mY = ev.getY();
                super.onTouchEvent(ev);
                return true;
            case MotionEvent.ACTION_MOVE:
                if (listener != null) {
                    listener.floatingViewMoving(ev.getRawY(), mScrollLeft);
                }
                int scaledTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
                float x = ev.getX();
                float y = ev.getY();
                x = x - mX;
                y = y - mY;
                if (Math.abs(scaledTouchSlop) < Math.abs(x) || Math.abs(scaledTouchSlop) < Math.abs(y)) {
                    isScroll = true;
                }
                if (isScroll) {
                    mScrollLeft = (int) (getX() + x);
                    mScrollTop = (int) (getY() + y);
                    mScrollRight = (int) (getX() + getWidth() + x);
                    mScrollBottom = (int) (getY() + getHeight() + y);
                    //防止滑出父界面
                    if (mScrollLeft < 0 || mScrollRight > mParentWidth) {
                        mScrollLeft = (int) getX();
                        mScrollRight = (int) getX() + getWidth();
                    }
                    if (mScrollTop < 0 || mScrollBottom > mParentHeight) {
                        mScrollTop = (int) getY();
                        mScrollBottom = (int) getY() + getHeight();
                    }
                    layout(mScrollLeft, mScrollTop, mScrollRight, mScrollBottom);
                    hasScroll = true;
                    return true;
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (isScroll) {
                    isScroll = false;
                    setPressed(false);//重置点击状态
                    if (isAdsorb) {//判断是否开启吸附
                        //获取屏幕中间值
                        int mind = getScreenWidth() / 2;
                        //获取控件宽度的中间值
                        int viewWithMind = getWidth() / 2;
                        //手指抬起时 自动吸附到屏幕两边
                        if (mScrollLeft + viewWithMind > mind) {
                            mScrollRight = getScreenWidth();
                            mScrollLeft = getScreenWidth() - getWidth();
                        } else {
                            mScrollRight = getWidth();
                            mScrollLeft = 0;
                        }
                        //为了防止拖动到导航栏上，计算自动回到设置的高度
                        int backHeight = mParentHeight - getHeight() - dip2px(133f);//83+50
                        if (mScrollTop > backHeight) {
                            mScrollTop = backHeight;
                            mScrollBottom = mScrollTop + getHeight();
                        }
                        layout(mScrollLeft, mScrollTop, mScrollRight, mScrollBottom);
                        if (listener != null) {
                            listener.floatingViewUp(ev.getRawY(), mScrollLeft, mScrollBottom);
                        }
                    }
                    return true;
                }
                break;
        }
        return super.onTouchEvent(ev);
    }

    public boolean isAdsorb() {
        return isAdsorb;
    }

    public void setAdsorb(boolean adsorb) {
        isAdsorb = adsorb;
    }

    public void setListener(FloatingViewListener listener) {
        this.listener = listener;
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

    /**
     * 得到屏幕高度
     */
    public int getScreenHeight() {
        WindowManager windowManager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics.heightPixels;
    }

    public interface FloatingViewListener {
        void floatingViewMoving(float y, int scrollLeft);

        void floatingViewUp(float y, int scrollLeft, int scrollBottom);
    }

    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    public int dip2px(float dpValue) {
        WindowManager windowManager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(displayMetrics);
        final float scale = displayMetrics.density;
        return (int) (dpValue * scale + 0.5f);
    }
}

