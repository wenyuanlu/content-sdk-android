package com.qichuang.retrofitlibs.retrofit

import android.app.Activity
import android.net.ParseException
import android.text.TextUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.JsonParseException
import com.maishuo.haohai.R
import com.qichuang.commonlibs.basic.CustomBasicApplication
import com.qichuang.commonlibs.utils.LoggerUtils
import com.qichuang.retrofitlibs.dialog.ProgressDialogUtil
import io.reactivex.disposables.Disposable
import org.json.JSONException
import retrofit2.HttpException
import java.io.InterruptedIOException
import java.net.ConnectException
import java.net.UnknownHostException


abstract class CommonObserver<T> @JvmOverloads constructor(
    private var customStartHideLoading: Boolean = false,
    private var customStopHideLoading: Boolean = false
) :
    CommonBasicObserver<T>() {

    private var activity: Activity? = null

    init {
        activity = CustomBasicApplication.fetchActivity()
    }

    override fun onComplete() {
        if (!customStopHideLoading) {
            ProgressDialogUtil.instance.dismiss()
        }
    }

    override fun onSubscribe(d: Disposable) {
        if (activity is AppCompatActivity && !customStartHideLoading) {
            ProgressDialogUtil.instance.showProgressDialog(activity as AppCompatActivity)
        }
    }

    override fun onError(e: Throwable) {
        super.onError(e)
        try {
            ProgressDialogUtil.instance.dismiss()
            val errorMessage = if (e is HttpException) {     //   HTTP错误
                onException(CommonExceptionReason.BAD_NETWORK)
            } else if (e is ConnectException
                || e is UnknownHostException
            ) {   //   连接错误
                onException(CommonExceptionReason.CONNECT_ERROR)
            } else if (e is InterruptedIOException) {   //  连接超时
                onException(CommonExceptionReason.CONNECT_TIMEOUT)
            } else if (e is JsonParseException
                || e is JSONException
                || e is ParseException
            ) { //  解析错误
                onException(CommonExceptionReason.PARSE_ERROR)
            } else {
                e.message ?: ""
            }
            onResponseError(errorMessage, e)
        } catch (e: Exception) {
            LoggerUtils.e(e.toString())
        }
    }

    /**
     * 网络请求异常
     *
     * @param reason
     */
    private fun onException(reason: CommonExceptionReason): String? {
        return when (reason) {
            CommonExceptionReason.BAD_NETWORK -> {
                activity?.getString(R.string.bad_network)
            }
            CommonExceptionReason.CONNECT_ERROR -> {
                activity?.getString(R.string.connect_error)
            }
            CommonExceptionReason.CONNECT_TIMEOUT -> {
                activity?.getString(R.string.connect_timeout)
            }
            CommonExceptionReason.PARSE_ERROR -> {
                activity?.getString(R.string.parse_error)
            }
            CommonExceptionReason.UNKNOWN_ERROR -> {
                activity?.getString(R.string.unknown_error)
            }
        }
    }

    override fun onResponseError(message: String?, e: Throwable?, code: Int?) {
        LoggerUtils.e(message)
        ProgressDialogUtil.instance.dismiss()
        if (!TextUtils.isEmpty(message) && code != CommonResCodeEnum.RES_CODE_100.recCode && activity != null) {
            Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
        }
    }
}
