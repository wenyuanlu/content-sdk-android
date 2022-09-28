package com.qichuang.commonlibs.utils;


import com.maishuo.haohai.BuildConfig;

public class LogUtils {

    private static boolean isEnableLog = BuildConfig.DEBUG;
    private static boolean isLOGD      = isEnableLog;
    private static boolean isLOGI      = isEnableLog;
    private static boolean isLOGV      = isEnableLog;
    private static boolean isLOGE      = isEnableLog;
    private static boolean isLOGW      = isEnableLog;

    private static final String LOG_PREFIX = LoggerUtils.TAG;

    public static void D (final String tag, String message) {
        // 根据日志的级别判断是否打印日志
        if (isLOGD) {
            LoggerUtils.INSTANCE.d(tag, message);
        }
    }

    public static void LOGV (final String tag, String message) {
        if (isLOGV) {
            LoggerUtils.INSTANCE.v(tag, message);
        }
    }


    public static void LOGI (String tag, String message) {
        if (isLOGI) {
            LoggerUtils.INSTANCE.i(tag, message);
        }
    }


    public static void LOGW (String tag, String message) {
        if (isLOGW) {
            LoggerUtils.INSTANCE.w(tag, message);
        }
    }


    public static void LOGE (String tag, String message) {
        if (isLOGE) {
            LoggerUtils.INSTANCE.e(tag, message);
        }
    }

    public static void LOGE (String message) {
        if (isLOGE) {
            LoggerUtils.INSTANCE.e(LOG_PREFIX, message);
        }
    }

    private LogUtils () {
    }
}
