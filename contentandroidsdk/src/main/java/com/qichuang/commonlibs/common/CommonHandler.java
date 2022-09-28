package com.qichuang.commonlibs.common;

import android.os.Handler;

import java.lang.ref.WeakReference;

/**
 * author ：Seven
 * date : 4/30/21
 * description :无内存泄漏的延时Handler
 */
public class CommonHandler<T> extends Handler implements Runnable{

    public WeakReference<T> ref;
    private PostDelayCallBack callBack;

    public CommonHandler(T t) {
        super();
        this.ref = new WeakReference<T>(t);
    }

    public void post(){
        postDelay(0,null);
    }

    public void postDelay(long delayMillis,PostDelayCallBack callBack) {
        this.callBack=callBack;
        if (delayMillis < 0) {
            delayMillis = 0;
        }
        postDelayed(this, delayMillis);
    }

    @Override
    public void run() {
        if (callBack!=null) {
            callBack.callBack();
        }
    }

    public void releaseHandler() {
        removeCallbacks(this);
        removeCallbacksAndMessages(null);
    }

    public interface PostDelayCallBack {
        void callBack();
    }
}
