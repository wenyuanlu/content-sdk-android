package com.qichuang.retrofitlibs.retrofit

object ApiConstants {

    const val SDK_VERSION = "0.8.2"
    const val SDK_VERSION_CODE = "82"

    //正式包修改成false
    var isDebug: Boolean = true

    //生产环境
    private const val BASE_URL_RELEASE = "https://api.haohaiyoo.cn/Api/"

    //测试环境
    private const val BASE_URL_TEST = "http://api.haohai.corpize.com/Api/"

    //发送曝光
    const val SEND_CONTENT_PLAYER_REQUEST:String = "http://api.content.ivoicead.com/content/ctrace"

    //获取url
    fun fetchBaseUrl(): String {
        return if (isDebug) {
            BASE_URL_TEST
        } else {
            BASE_URL_RELEASE
        }
    }

    const val CHARSET_NAME: String = "UTF-8"

    //header
    const val USER_AGENT: String = "user-agent"
    const val ACCEPT: String = "Accept"
    const val CONTENT_TYPE: String = "Content-Type"
    const val HEADER_BUNDLE: String = "bundle"
    const val COMMON_PAGER_NAME: String = "com.maishuo.haohai"
    const val APP_OS: String = "os"
    const val APP_VERSION: String = "version"
    const val APP_VERSION_CODE: String = "versioncode"
    const val APP_IMEI: String = "imei"
    const val APP_OAID: String = "oaid"
    const val APP_STOREID: String = "storeid"
    const val APP_TOKEN: String = "token"
    const val APP_UUID: String = "uuid"
    const val APP_P: String = "p"
    const val APP_SDK_VERSION: String = "sdkversion"
    const val APP_SDK_VERSION_CODE: String = "sdkversioncode"

    const val APP_SDK_TYPE:String = "sdk"

}
