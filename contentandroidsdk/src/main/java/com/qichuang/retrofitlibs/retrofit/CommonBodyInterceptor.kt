package com.qichuang.retrofitlibs.retrofit

import android.os.Build
import android.text.TextUtils
import com.maishuo.haohai.BuildConfig
import com.qichuang.commonlibs.common.PreferencesKey
import com.qichuang.commonlibs.utils.AESUtils
import com.qichuang.commonlibs.utils.DeviceUtil
import com.qichuang.commonlibs.utils.LoggerUtils
import com.qichuang.commonlibs.utils.PreferencesUtils
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okio.Buffer
import org.json.JSONObject
import java.io.File
import java.nio.charset.Charset
import java.util.*


/**
 * 自定义拦截器可以做一些加密解密处理
 */
class CommonBodyInterceptor : Interceptor {

    @Synchronized
    override fun intercept(chain: Interceptor.Chain): Response {
        val original: Request = chain.request()

        var request: Request = original.newBuilder()
            .addHeader(ApiConstants.USER_AGENT, getUserAgent())
            .addHeader(ApiConstants.ACCEPT, "application/json")
            .addHeader(ApiConstants.CONTENT_TYPE, "application/json; charset=utf-8")
            .addHeader(ApiConstants.HEADER_BUNDLE, fetchBundle())
            .addHeader(ApiConstants.APP_OS, "2")
            .addHeader(ApiConstants.APP_VERSION, DeviceUtil.getVersionName())
            .addHeader(ApiConstants.APP_VERSION_CODE, fetchVersionCode())
            .addHeader(ApiConstants.APP_IMEI, fetchImei())
            .addHeader(ApiConstants.APP_OAID, fetchOAID())
            .addHeader(ApiConstants.APP_STOREID, fetchChannelId())
            .addHeader(ApiConstants.APP_TOKEN, PreferencesUtils.getString(PreferencesKey.TOKEN, ""))
            .addHeader(ApiConstants.APP_UUID, fetchUUID())
            .addHeader(ApiConstants.APP_P, ApiConstants.APP_SDK_TYPE)
            .addHeader(ApiConstants.APP_SDK_VERSION, ApiConstants.SDK_VERSION)
            .addHeader(ApiConstants.APP_SDK_VERSION_CODE, ApiConstants.SDK_VERSION_CODE)
            .build()

        if (BuildConfig.DEBUG) {
            val buffer = Buffer()
            request.body?.writeTo(buffer)
            val contentType: MediaType? = request.body?.contentType()
            val charset = contentType?.charset(Charset.forName(ApiConstants.CHARSET_NAME))
            if (null != charset) {
                val param: String = buffer.readString(charset)
                val log: String = String.format("url:%s\nparam:%s", request.url, param)
                LoggerUtils.e(log)
            } else {
                val log: String = String.format("url:%s", request.url)
                LoggerUtils.e(log)
            }
        }

        val requestBody: RequestBody? = request.body

        if (null != requestBody) {
            val jsonObject = JSONObject()
            var paramJson = JSONObject()
            try {
                val buffer = Buffer()
                requestBody.writeTo(buffer)
                val contentType: MediaType? = requestBody.contentType()
                val charset = contentType?.charset(Charset.forName(ApiConstants.CHARSET_NAME))

                val encryptStr = if (null != charset) {
                    val param: String = buffer.readString(charset)
                    paramJson = JSONObject(param)
                    AESUtils.encrypt(param)
                } else {
                    ""
                }
                jsonObject.put("body", encryptStr)
            } catch (e: Exception) {
                e.printStackTrace()
            }

            val imagePath = paramJson.optString("image")
            val body: RequestBody = if (imagePath.isEmpty()) {
                jsonObject.toString().toRequestBody("application/json;charset=utf-8".toMediaType())
            } else {
                MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart(
                        "image",
                        File(imagePath).name,
                        File(imagePath).asRequestBody("image/png".toMediaTypeOrNull())
                    )
                    .addFormDataPart("body", jsonObject.optString("body"))
                    .build()
            }

            request = request.newBuilder()
                .post(body)
                .build()
        }
        return chain.proceed(request)
    }

    private fun getUserAgent(): String {
        var userAgent: String? = ""
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            try {
                userAgent = System.getProperty("http.agent")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            userAgent = System.getProperty("http.agent")
        }
        val stringBuffer = StringBuilder()
        var i = 0
        val length = userAgent!!.length
        while (i < length) {
            val c = userAgent[i]
            if (c <= '\u001f' || c >= '\u007f') {
                stringBuffer.append(String.format("\\u%04x", c.toInt()))
            } else {
                stringBuffer.append(c)
            }
            i++
        }
        return stringBuffer.toString()
    }

    private fun fetchBundle(): String {
        return AESUtils.encrypt(ApiConstants.COMMON_PAGER_NAME)
    }

    private fun fetchImei(): String {
        val imei = DeviceUtil.getIMEI()
        return if (TextUtils.isEmpty(imei)) {
            ""
        } else {
            AESUtils.encrypt(imei)
        }
    }

    private fun fetchOAID(): String {
        val oaid = PreferencesUtils.getString(PreferencesKey.OAID, "")
        return if (TextUtils.isEmpty(oaid)) {
            ""
        } else {
            AESUtils.encrypt(oaid)
        }
    }

    private fun fetchChannelId(): String {
        return "0"
    }

    private fun fetchVersionCode(): String {
        val versionCode = DeviceUtil.getVersionCode()
        return versionCode.toString()
    }

    private fun fetchUUID(): String {
        return AESUtils.encrypt(
            if (PreferencesUtils.contains(PreferencesKey.APP_HEADER_UUID)) {
                PreferencesUtils.getString(PreferencesKey.APP_HEADER_UUID)
            } else {
                val uuid = UUID.randomUUID().toString()
                PreferencesUtils.putString(PreferencesKey.APP_HEADER_UUID, uuid)
                uuid
            }
        )
    }
}