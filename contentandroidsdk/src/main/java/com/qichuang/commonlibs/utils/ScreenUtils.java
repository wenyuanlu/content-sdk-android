package com.qichuang.commonlibs.utils;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Point;
import android.os.Build;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

import java.lang.reflect.Method;

/**
 * author : xpSun
 * date : 2021/3/25
 * description :
 */
public class ScreenUtils {

    /**
     * 获取屏幕宽度
     *
     * @param context
     * @return
     */
    public static int getScreenWidth (Context context) {
        int screenWith = -1;
        try {
            screenWith = context.getResources().getDisplayMetrics().widthPixels;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return screenWith;
    }

    /**
     * 获取真实的高度,全面屏水滴屏需要
     *
     * @param context
     * @return
     */
    public static int getRealyScreenHeight (Context context) {
        int screenHeight = -1;
        try {
            //新的获取屏幕高度的方法
            WindowManager wm    = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            Point         point = new Point();
            if (wm != null) {
                wm.getDefaultDisplay().getRealSize(point);
                screenHeight = point.y;
            } else {
                //原来获取屏幕高度的方法
                screenHeight = context.getResources().getDisplayMetrics().heightPixels;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return screenHeight;
    }

    //获取顶部状态栏高度
    public static int getStatusBarHeight (Context context) {
        Resources resources  = context.getResources();
        int       resourceId = resources.getIdentifier("status_bar_height", "dimen", "android");
        int       height     = resources.getDimensionPixelSize(resourceId);
        return height;
    }

    /**
     * 获取导航栏高度
     *
     * @return
     */
    public static int getNavigationHeight (Activity activity) {
        int            windowheight = activity.getWindowManager().getDefaultDisplay().getHeight(); //获取无导航栏状态栏时窗口高度
        int            fullheigh    = 0;   //窗口总高度
        Display        display      = activity.getWindowManager().getDefaultDisplay();
        DisplayMetrics dm           = new DisplayMetrics();
        @SuppressWarnings("rawtypes")
        Class klass;
        try {
            klass = Class.forName("android.view.Display");
            @SuppressWarnings("unchecked")
            Method method = klass.getMethod("getRealMetrics", DisplayMetrics.class);
            method.invoke(display, dm);
            fullheigh = dm.heightPixels;  //获取窗口总高度
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (windowheight == fullheigh) return 0;   //无虚拟导航栏存在
        return fullheigh - windowheight - getStatusBarHeight(activity);  //导航栏高度
    }

    private static boolean checkDeviceHasNavigationBar (Context context) {
        boolean   hasNavigationBar = false;
        Resources rs               = context.getResources();
        int       id               = rs.getIdentifier("config_showNavigationBar", "bool", "android");
        if (id > 0) {
            hasNavigationBar = rs.getBoolean(id);
        }
        try {
            Class  systemPropertiesClass = Class.forName("android.os.SystemProperties");
            Method m                     = systemPropertiesClass.getMethod("get", String.class);
            String navBarOverride        = (String) m.invoke(systemPropertiesClass, "qemu.hw.mainkeys");
            if ("1".equals(navBarOverride)) {
                hasNavigationBar = false;
            } else if ("0".equals(navBarOverride)) {
                hasNavigationBar = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return hasNavigationBar;
    }

    public static boolean isNavigationBarShowing (Context context) {
        //判断手机底部是否支持导航栏显示
        boolean haveNavigationBar = checkDeviceHasNavigationBar(context);
        if (haveNavigationBar) {
            String brand = Build.BRAND;
            String mDeviceInfo;
            if (brand.equalsIgnoreCase("HUAWEI")) {
                mDeviceInfo = "navigationbar_is_min";
            } else if (brand.equalsIgnoreCase("XIAOMI")) {
                mDeviceInfo = "force_fsg_nav_bar";
            } else if (brand.equalsIgnoreCase("VIVO")) {
                mDeviceInfo = "navigation_gesture_on";
            } else if (brand.equalsIgnoreCase("OPPO")) {
                mDeviceInfo = "navigation_gesture_on";
            } else {
                mDeviceInfo = "navigationbar_is_min";
            }

            if (Settings.Global.getInt(context.getContentResolver(), mDeviceInfo, 0) == 0) {
                return true;
            }
        }
        return false;
    }
}
