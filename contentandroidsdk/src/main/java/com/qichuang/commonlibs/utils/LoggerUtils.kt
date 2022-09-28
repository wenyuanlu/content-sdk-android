package com.qichuang.commonlibs.utils

import android.util.Log
import com.maishuo.haohai.BuildConfig


/**
 * 自定义记录器
 */
object LoggerUtils {

    const val TAG = "LoggerUtils"
    //是否启用日志
    private var isEnableLog:Boolean = BuildConfig.DEBUG

    fun v(message: String?) {
        if(!isEnableLog){
            return
        }
        v(TAG, message ?: "")
    }

    fun v(key: String?, message: String?) {
        if(!isEnableLog){
            return
        }

        Log.v(key ?: "", message ?: "")
    }

    fun e(message: String?) {
        if(!isEnableLog){
            return
        }

        e(TAG, message ?: "")
    }

    fun e(key: String?, message: String?) {
        if(!isEnableLog){
            return
        }

        Log.e(key ?: "", message ?: "")
    }

    fun d(message: String?) {
        if(!isEnableLog){
            return
        }

        d(TAG, message ?: "")
    }

    fun d(key: String?, message: String?) {
        if(!isEnableLog){
            return
        }

        Log.d(key ?: "", message ?: "")
    }

    fun i(message: String?) {
        if(!isEnableLog){
            return
        }

        i(TAG, message ?: "")
    }

    fun i(key: String?, message: String?) {
        if(!isEnableLog){
            return
        }

        Log.i(key ?: "", message ?: "")
    }

    fun w(message: String?) {
        if(!isEnableLog){
            return
        }

        w(TAG, message ?: "")
    }

    fun w(key: String?, message: String?) {
        if(!isEnableLog){
            return
        }

        Log.w(key ?: "", message ?: "")
    }

}