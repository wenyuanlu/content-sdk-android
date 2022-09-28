package com.maishuo.contentandroidsdk.common

import android.app.Application
import android.text.TextUtils
import com.maishuo.contentandroidsdk.utils.PreferencesUtils
import com.maishuo.haohai.common.ContentAndroidSDK

/**
 * author : xpSun
 * date : 12/13/21
 * description :
 */
class AppApplication : Application() {
    companion object {
        var instance: AppApplication? = null
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        CrashHandler.getInstance().init(this)

        val oaid = PreferencesUtils.getString("oaid")

        ContentAndroidSDK.init(
            this,
            if (TextUtils.isEmpty(oaid))
                "509c74ee2263b6c8"
            else
                oaid,
            SimpleConstant.getMID(),
            SimpleConstant.getContentId(),
            0
        )
    }

}