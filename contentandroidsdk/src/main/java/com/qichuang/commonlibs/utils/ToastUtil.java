package com.qichuang.commonlibs.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;
import android.view.Gravity;
import android.widget.Toast;

import com.qichuang.commonlibs.basic.CustomBasicApplication;

public class ToastUtil {

    private static Toast toast = null;//Toast对象
    private static Context mContext = CustomBasicApplication.INSTANCE.getInstance();

    /**
     * 程序入口注册,在AppLication中
     */
    public static void register(Context context) {
        mContext = context.getApplicationContext();
    }

    /**
     * Toast提示文本.
     */
    public static void showToast(String text) {
        showToast(text, Gravity.CENTER);
    }

    /**
     * Toast提示文本.
     *
     * @param resId 文本的资源ID
     */
    public static void showToast(int resId) {
        showToast(mContext.getString(resId), Gravity.CENTER);
    }

    /**
     * Toast提示文本.
     *
     * @param resId 文本的资源ID
     */
    public static void showToast(int resId, int gravity) {
        showToast(mContext.getString(resId), gravity);
    }

    /**
     * Toast提示文本.
     */
    @SuppressLint("ShowToast")
    public static void showToast(String text, int gravity) {
        //防止弹出空吐司
        if (TextUtils.isEmpty(text)) {
            return;
        }
        if (toast == null) {
            toast = Toast.makeText(mContext, text, Toast.LENGTH_SHORT);
            toast.setGravity(gravity, 0, 0);
        } else {
            toast.setText(text);
        }
        toast.show();
    }

}