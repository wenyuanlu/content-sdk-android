package com.maishuo.haohai.common

import android.app.Activity
import android.app.Application
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import com.corpize.sdk.ivoice.QCiVoiceSdk
import com.maishuo.haohai.api.response.GetListResponse
import com.maishuo.haohai.listener.OnContentAndroidSdkInitListener
import com.maishuo.haohai.main.event.GetProgramListParamEvent
import com.maishuo.haohai.main.event.GetProgramListResponseEvent
import com.maishuo.haohai.main.lite.CustomLitePlayerFragment
import com.maishuo.haohai.main.service.AudioPlayerService
import com.maishuo.haohai.main.ui.MainHomeFragment
import com.maishuo.haohai.manager.ContentAdSDKManager
import com.maishuo.haohai.utils.CustomDownCountUtils
import com.qichuang.commonlibs.basic.CustomBasicApplication
import com.qichuang.commonlibs.common.PreferencesKey
import com.qichuang.commonlibs.utils.PreferencesUtils
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * author : xpSun
 * date : 12/14/21
 * description :
 */
object ContentAndroidSDK : ContentAdSDKManager() {

    private var mainHomeFragment: MainHomeFragment? = null

    var mid: String? = null
    var contentId: String? = null

    private var customLitePlayerFragment: CustomLitePlayerFragment? = null

    fun init(
        application: Application?,
        oaid: String?,
        mid: String?,
        contentId: String?,
        dnt: Int?
    ) {
        CustomBasicApplication.instance = application

        PreferencesUtils.putString(PreferencesKey.OAID, oaid)

        QCiVoiceSdk.get().init(application, oaid, mid, dnt ?: 0)

        this.mid = mid
        this.contentId = contentId

        EventBus.getDefault().register(this)
    }

    fun addOnContentInfo(
        activity: Activity?,
        isAgainLoad: Boolean?,
        listener: OnContentAndroidSdkInitListener?
    ) {
        if (null != activity) {
            CustomBasicApplication.addActivity(activity)
        }

        if (isAgainLoad == true) {
            mainHomeFragment = MainHomeFragment()
        } else if (null == mainHomeFragment) {
            mainHomeFragment = MainHomeFragment()
        }
        listener?.onInit(this, mainHomeFragment)
    }

    fun openHaoHaiPlayerView(appCompatActivity: AppCompatActivity?, response: GetListResponse?) {
        if (null == customLitePlayerFragment) {
            customLitePlayerFragment = CustomLitePlayerFragment(appCompatActivity)
            customLitePlayerFragment?.showNowDialog()
        } else {
            customLitePlayerFragment?.start()
        }
        customLitePlayerFragment?.initIndexApi(response)
    }

    override fun onResume() {

    }

    override fun onPause() {

    }

    override fun destroy() {
        firstAdDestroy()

        Constant.response = null
        Constant.responses = null

        CustomDownCountUtils.instance.cancel()

        EventBus.getDefault().unregister(this)
    }

    //获取播放列表
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: GetProgramListResponseEvent?) {
        customLitePlayerFragment?.initPlayerEvent(event)
    }

    //获取播放列表
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: GetProgramListParamEvent?) {
        if (Constant.HEADER_LINES_TAG != Constant.response?.album_type) {

            val albumId = event?.album_id ?: 0
            val pageSize = event?.page_size ?: 10
            val page = event?.page ?: 1
            val playerStatus = event?.playerStatus ?: 0
            AudioPlayerService.start(
                CustomBasicApplication.instance,
                albumId,
                pageSize,
                page,
                playerStatus
            )
        }
    }

    fun firstAdDestroy() {
        mainHomeFragment?.onAdDestroy()
    }

    override fun startPlayAd() {

    }

    override fun stopPlayerAd() {
        mainHomeFragment?.onAdDestroy()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

    }
}