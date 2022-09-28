package com.qichuang.retrofitlibs.retrofit

import android.content.Intent
import android.text.TextUtils
import com.qichuang.retrofitlibs.bean.BasicResponse
import com.qichuang.commonlibs.basic.CustomBasicApplication
import com.qichuang.commonlibs.common.PreferencesKey
import com.qichuang.commonlibs.utils.LogUtils
import com.qichuang.commonlibs.utils.PreferencesUtils
import io.reactivex.Observer
import io.reactivex.disposables.Disposable

/**
 * author : xpSun
 * date : 6/10/21
 *description :
 */
abstract class CommonBasicObserver<T> : Observer<BasicResponse<T>> {

    override fun onSubscribe(d: Disposable) {

    }

    override fun onComplete() {

    }

    override fun onNext(data: BasicResponse<T>) {
        when (data.status) {
            CommonResCodeEnum.RES_CODE_200.recCode -> {
                onResponseSuccess(data.data)
            }
            else -> {
                if (!TextUtils.isEmpty(data.msg ?: "")) {
                    onResponseError(data.msg ?: "", null, data.status)
                }
            }
        }
    }

    override fun onError(e: Throwable) {
        LogUtils.LOGE("onError", e.toString())
    }

    /**
     * 可以提供请求错误到界面的error
     */
    open fun onResponseError(message: String?, e: Throwable? = null, code: Int? = null) {

    }

    /**
     * 接口数据回调
     */
    abstract fun onResponseSuccess(response: T?)

}