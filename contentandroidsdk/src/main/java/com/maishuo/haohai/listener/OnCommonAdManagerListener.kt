package com.maishuo.haohai.listener

import android.content.Intent

/**
 * author : xpSun
 * date : 12/15/21
 * description :
 */
interface OnCommonAdManagerListener {

    /**
     * 界面resume的展示
     */
    fun onResume()

    /**
     * 界面pause的展示
     */
    fun onPause()

    /**
     * 界面销毁清除广告
     */
    fun destroy()

    /**
     * 开始播放广告
     */
    fun startPlayAd()

    /**
     * 停止播放 跳过播放广告
     */
    fun stopPlayerAd()

    /**
     * 点击其它页面后回调
     */
    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)

}