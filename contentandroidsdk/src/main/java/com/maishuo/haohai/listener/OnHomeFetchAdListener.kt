package com.maishuo.haohai.listener

import android.view.View
import com.corpize.sdk.ivoice.admanager.QcAdManager

/**
 * author : xpSun
 * date : 11/24/21
 * description :
 */
interface OnHomeFetchAdListener {

    fun onAdReceive(manager: QcAdManager?, view: View?)

    fun onRollAdClickClose()

    fun onAdError()
}