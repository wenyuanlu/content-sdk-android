package com.maishuo.contentandroidsdk.ui

import androidx.fragment.app.Fragment
import com.maishuo.contentandroidsdk.R
import com.maishuo.contentandroidsdk.base.BaseActivity
import com.maishuo.haohai.common.ContentAndroidSDK
import com.maishuo.haohai.listener.OnContentAndroidSdkInitListener
import com.maishuo.haohai.manager.ContentAdSDKManager

/**
 * author : xpSun
 * date : 12/14/21
 * description :
 */
class SimpleMainActivity : BaseActivity() {

    private var adSDKManager: ContentAdSDKManager? = null

    override fun fetchRootViewById(): Int {
        return R.layout.activity_main_simple_layout
    }

    override fun initWidgets() {
        ContentAndroidSDK.addOnContentInfo(
            this,
            null,
            object : OnContentAndroidSdkInitListener {
                override fun onInit(manager: ContentAdSDKManager?, fragment: Fragment?) {
                    adSDKManager = manager
                    commitFragment(R.id.main_simple_content_layout, fragment)
                }
            })
    }

    override fun initWidgetsEvent() {

    }

    override fun onDestroy() {
        super.onDestroy()
        adSDKManager?.destroy()
    }
}