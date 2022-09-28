package com.maishuo.haohai.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkInfo
import com.maishuo.haohai.api.retrofit.ApiService
import com.qichuang.commonlibs.utils.LoggerUtils
import com.qichuang.commonlibs.utils.NetUtils
import com.qichuang.commonlibs.utils.ToastUtil
import com.qichuang.retrofitlibs.retrofit.CommonObserver
import com.qichuang.roomlib.RoomManager
import retrofit2.Response

/**
 * author : xpSun
 * date : 11/25/21
 * description :
 */
class NetWorkConnectedChangerReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        // 监听网络连接，总网络判断，即包括wifi和移动网络的监听
        if (ConnectivityManager.CONNECTIVITY_ACTION == intent?.action) {
            val networkInfo =
                intent.getParcelableExtra<NetworkInfo>(ConnectivityManager.EXTRA_NETWORK_INFO)
            //连上的网络类型判断：wifi还是移动网络
            if (networkInfo?.type == ConnectivityManager.TYPE_WIFI) {
                LoggerUtils.e("连接的是wifi网络")
            } else if (networkInfo?.type == ConnectivityManager.TYPE_MOBILE) {
                LoggerUtils.e("连接的是移动网络")

                if (NetUtils.isNetworkConnected(context)) {
                    ToastUtil.showToast("当前为非WIFI播放")
                }
            }
        }

        if (NetUtils.isNetworkConnected(context)) {
            onNetConnectSendExposure(context)
        } else {
            ToastUtil.showToast("您的网络好像不太给力,请稍后再试")
        }
    }

    private fun onNetConnectSendExposure(context: Context?) {
        context?.let {
            val sendExposures = RoomManager.getInstance(it).loadAllSendExposure()

            if (!sendExposures.isNullOrEmpty()) {
                for (item in sendExposures.iterator()) {
                    ApiService.instance.contentCtrace(
                        item.mid,
                        item.provider,
                        item.uid,
                        item.type,
                        item.chapterid,
                        item.event
                    ).subscribe(object : CommonObserver<Response<Void>>(true) {
                        override fun onResponseSuccess(response: Response<Void>?) {}
                        override fun onResponseError(message: String?, e: Throwable?, code: Int?) {}
                    })
                }

                RoomManager.getInstance(it).deleteAllSendExposure(sendExposures)
            }
        }
    }
}