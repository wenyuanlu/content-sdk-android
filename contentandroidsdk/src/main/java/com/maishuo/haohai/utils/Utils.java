package com.maishuo.haohai.utils;

import android.app.Activity;
import android.content.res.Resources;
import android.graphics.RectF;
import android.os.Build;
import android.view.Gravity;
import android.view.View;
import android.view.ViewTreeObserver;

/**
 * author : xpSun
 * date : 2021/3/30
 * description :
 */
public class Utils {

    /**
     * 判断Activity是否Destroy
     * @param activity
     * @return
     */
    public static boolean isDestroy(Activity activity) {
        if (activity == null || activity.isFinishing() || activity.isDestroyed()) {
            return true;
        } else {
            return false;
        }
    }

    // 两次点击按钮之间的点击间隔不能少于1000毫秒
    private static final int  MIN_CLICK_DELAY_TIME = 500;
    private static       long lastClickTime;

    public static boolean isFastClick () {
        boolean flag         = false;
        long    curClickTime = System.currentTimeMillis();
        if (Math.abs((curClickTime - lastClickTime)) >= MIN_CLICK_DELAY_TIME) {
            flag = true;
        }
        lastClickTime = curClickTime;
        return flag;
    }

    public static RectF calculateRectOnScreen (View view) {
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        return new RectF(location[0], location[1], location[0] + view.getMeasuredWidth(), location[1] + view.getMeasuredHeight());
    }

    public static RectF calculateRectInWindow (View view) {
        int[] location = new int[2];
        view.getLocationInWindow(location);
        return new RectF(location[0], location[1], location[0] + view.getMeasuredWidth(), location[1] + view.getMeasuredHeight());
    }

    public static float pxToDp (float px) {
        return px / Resources.getSystem().getDisplayMetrics().density;
    }

    public static float dpToPx (float dp) {
        return dp * Resources.getSystem().getDisplayMetrics().density;
    }

    public static int dpToPx (int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

    public static int gravityToArrowDirection (int gravity) {
        switch (gravity) {
            case Gravity.START:
                return Gravity.END;
            case Gravity.TOP:
                return Gravity.BOTTOM;
            case Gravity.END:
                return Gravity.START;
            case Gravity.BOTTOM:
                return Gravity.TOP;
            default:
                return gravity;
        }
    }

    public static void removeOnGlobalLayoutListener (View view, ViewTreeObserver.OnGlobalLayoutListener listener) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            view.getViewTreeObserver().removeOnGlobalLayoutListener(listener);
        } else {
            view.getViewTreeObserver().removeGlobalOnLayoutListener(listener);
        }
    }
}
