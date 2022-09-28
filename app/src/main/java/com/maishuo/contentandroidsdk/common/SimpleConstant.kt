package com.maishuo.contentandroidsdk.common

import com.qichuang.retrofitlibs.retrofit.ApiConstants

/**
 * author : xpSun
 * date : 12/14/21
 * description :
 */
object SimpleConstant {

    private const val COMMON_QC_MID_TEST = "D748D167CE5F8CC4307067F643F83A2C"//测试
    private const val COMMON_QC_MID_RELEASE = "0ecf89051d0cbd7995e80603919625fb"//正式
    private const val CONTENT_ID_TEST = "6ca6968c4005a5362d74637896b2854f"//测试
    private const val CONTENT_ID_RELEASE = "e916dae39f40ac9614ea84e25c830ade"//正式

    fun getMID(): String {
        return if (ApiConstants.isDebug) {
            COMMON_QC_MID_TEST
        } else {
            COMMON_QC_MID_RELEASE
        }
    }

    fun getContentId(): String {
        return if (ApiConstants.isDebug) {
            CONTENT_ID_TEST
        } else {
            CONTENT_ID_RELEASE
        }
    }
}