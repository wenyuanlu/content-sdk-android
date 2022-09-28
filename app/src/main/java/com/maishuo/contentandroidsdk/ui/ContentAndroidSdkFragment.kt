package com.maishuo.contentandroidsdk.ui

import androidx.fragment.app.Fragment
import com.maishuo.contentandroidsdk.R
import com.maishuo.contentandroidsdk.base.BaseFragment
import com.maishuo.haohai.common.ContentAndroidSDK
import com.maishuo.haohai.listener.OnContentAndroidSdkInitListener
import com.maishuo.haohai.manager.ContentAdSDKManager

/**
 * author : xpSun
 * date : 12/14/21
 * description :
 */
class ContentAndroidSdkFragment : BaseFragment() {

    private var adManager: ContentAdSDKManager? = null

    override fun fetchRootViewById(): Int {
        return R.layout.fragment_content_android_sdk_layout
    }

    override fun initWidgets() {

    }

    override fun initWidgetsEvent() {

    }

    fun showContentSDK(currentPosition: Int?) {
        if (MultipleTabLayoutFragment.CUSTOM_SHOW_SDK_POSITION == currentPosition) {
            ContentAndroidSDK.addOnContentInfo(
                activity,
                false,
                object : OnContentAndroidSdkInitListener {
                override fun onInit(manager: ContentAdSDKManager?, fragment: Fragment?) {
                    adManager = manager
                    commitFragment(R.id.content_android_sdk_contract, fragment)
                }
            })
        } else {
            adManager?.stopPlayerAd()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        adManager?.destroy()
    }

}