package com.maishuo.contentandroidsdk.utils

import android.util.Log


/**
 * 自定义记录器
 */
object LoggerUtils {

    const val TAG = "LoggerUtils"

    fun v(message: String?) {
        v(TAG, message ?: "")
    }

    fun v(key: String?, message: String?) {
        Log.v(key ?: "", message ?: "")
    }

    fun e(message: String?) {
        e(TAG, message ?: "")
    }

    fun e(key: String?, message: String?) {
        Log.e(key ?: "", message ?: "")
    }

    fun d(message: String?) {
        d(TAG, message ?: "")
    }

    fun d(key: String?, message: String?) {
        Log.d(key ?: "", message ?: "")
    }

    fun i(message: String?) {
        i(TAG, message ?: "")
    }

    fun i(key: String?, message: String?) {
        Log.i(key ?: "", message ?: "")
    }

    fun w(message: String?) {
        w(TAG, message ?: "")
    }

    fun w(key: String?, message: String?) {
        Log.w(key ?: "", message ?: "")
    }

}