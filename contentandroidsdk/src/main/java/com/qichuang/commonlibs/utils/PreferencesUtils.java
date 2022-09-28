package com.qichuang.commonlibs.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.qichuang.commonlibs.basic.CustomBasicApplication;

/**
 * sharePreferences工具类
 */
public class PreferencesUtils {

    public static        Context           mContext = CustomBasicApplication.INSTANCE.getInstance();
    private static final String            SP_NAME  = "qc_android_content_sdk_config";//文件名
    private static       SharedPreferences mSp;

    private static SharedPreferences getSharedPreferences () {
        if (mSp == null) {
            mSp = mContext.getSharedPreferences(SP_NAME, mContext.MODE_PRIVATE);
        }
        return mSp;
    }

    private static SharedPreferences.Editor getEditor () {
        SharedPreferences        sharedPreferences = getSharedPreferences();
        SharedPreferences.Editor edit              = sharedPreferences.edit();
        return edit;
    }

    public static boolean contains (String key) {
        SharedPreferences sharedPreferences = getSharedPreferences();
        return sharedPreferences.contains(key);
    }

    public static void putString (String key, String value) {
        SharedPreferences.Editor edit = getEditor();
        edit.putString(key, value);
        edit.commit();
    }

    public static String getString (String key) {
        return getString(key, "");
    }

    public static String getString (String key, String defaultValue) {
        SharedPreferences sharedPreferences = getSharedPreferences();
        String            result            = sharedPreferences.getString(key, defaultValue);
        return result;
    }

    public static void putBoolean (String key, boolean value) {
        SharedPreferences.Editor edit = getEditor();
        edit.putBoolean(key, value);
        edit.commit();
    }

    public static boolean getBoolean (String key) {
        return getBoolean(key, false);
    }

    public static Boolean getBoolean (String key, boolean defaultValue) {
        SharedPreferences sharedPreferences = getSharedPreferences();
        boolean           result            = sharedPreferences.getBoolean(key, defaultValue);
        return result;
    }

    public static void putInt (String key, int value) {
        SharedPreferences.Editor edit = getEditor();
        edit.putInt(key, value);
        edit.commit();
    }

    public static int getInt (String key) {
        return getInt(key, 0);
    }

    public static int getInt (String key, int defaultValue) {
        SharedPreferences sharedPreferences = getSharedPreferences();
        int               result            = sharedPreferences.getInt(key, defaultValue);
        return result;
    }

    public static void putLong (String key, long value) {
        SharedPreferences.Editor edit = getEditor();
        edit.putLong(key, value);
        edit.commit();
    }

    public static long getLong (String key) {
        return getLong(key, 0);
    }

    public static long getLong (String key, long defaultValue) {
        SharedPreferences sharedPreferences = getSharedPreferences();
        long              result            = sharedPreferences.getLong(key, defaultValue);
        return result;
    }
}
