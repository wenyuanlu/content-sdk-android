package com.maishuo.haohai.listener

import androidx.fragment.app.Fragment
import com.maishuo.haohai.manager.ContentAdSDKManager

/**
 * author : xpSun
 * date : 12/14/21
 * description :
 */
interface OnContentAndroidSdkInitListener {

    fun onInit(manager: ContentAdSDKManager?, fragment: Fragment?)
}