package com.maishuo.haohai.main.viewmodel

import android.app.Dialog
import android.text.TextUtils
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.lifecycle.MutableLiveData
import com.corpize.sdk.ivoice.QCiVoiceSdk
import com.corpize.sdk.ivoice.QcCustomTemplateAttr
import com.corpize.sdk.ivoice.QcCustomTemplateAttr.CoverStyle
import com.corpize.sdk.ivoice.QcCustomTemplateAttr.IconStyle
import com.corpize.sdk.ivoice.admanager.QcAdManager
import com.corpize.sdk.ivoice.bean.response.AdAudioBean
import com.corpize.sdk.ivoice.listener.QcCustomTemplateListener
import com.google.android.exoplayer2.MediaItem
import com.maishuo.haohai.api.bean.CustomLiveDataBean
import com.maishuo.haohai.api.bean.DialogBottomMoreBean
import com.maishuo.haohai.api.response.GetListResponse
import com.maishuo.haohai.api.retrofit.ApiService
import com.maishuo.haohai.audio.AudioPlayerManager
import com.maishuo.haohai.audio.PlayerSpeedEnum
import com.maishuo.haohai.common.Constant
import com.maishuo.haohai.listener.OnCommonBottomDialogItemClickListener
import com.maishuo.haohai.main.event.OnAdProgressEvent
import com.maishuo.haohai.main.ui.CustomPlayerPagerActivity
import com.maishuo.haohai.main.ui.PlayerPagerListSelectorBottomDialog
import com.maishuo.haohai.person.service.DownloadService
import com.maishuo.haohai.utils.CustomDownCountUtils
import com.maishuo.haohai.utils.DialogUtils
import com.maishuo.haohai.utils.MediaPlayerUtils
import com.qichuang.commonlibs.basic.BaseViewModel
import com.qichuang.commonlibs.common.PreferencesKey
import com.qichuang.commonlibs.utils.*
import com.qichuang.retrofitlibs.retrofit.CommonObserver
import com.qichuang.roomlib.RoomManager
import com.qichuang.roomlib.entity.SendExposureEntity
import org.greenrobot.eventbus.EventBus

/**
 * author : xpSun
 * date : 11/8/21
 * description :
 */
class CustomPlayerPagerViewModel constructor(private val activity: CustomPlayerPagerActivity?) :
    BaseViewModel() {
    val otherRecLiveData: MutableLiveData<CustomLiveDataBean<MutableList<GetListResponse>>> =
        MutableLiveData()
//    val keepLiveData: MutableLiveData<CustomLiveDataBean<KeepResponse>> = MutableLiveData()

    var albumId: Int? = null
    var programId: Int? = null
    var authorName: String? = null
    var company: String? = null
    var albumType: Int? = null

    private var otherRecCurrentPosition: Int = 0
    private var adTitle: String? = null
    private var payerPagerListSelectorBottomDialog: PlayerPagerListSelectorBottomDialog? = null

    private var duration: Int? = null
    private var interaction: Int? = null

    //处理播放列表
    fun setAudioPlayerList(list: MutableList<GetListResponse>?, position: Int?) {
        val mediaItems: MutableList<MediaItem> = MediaPlayerUtils.responseToMedia(albumType, list)

        val audio = AudioPlayerManager.getInstance()
        audio?.setAudios(mediaItems, position ?: 0)
        audio?.start()

        putListenHistory()
    }

    fun removeMediaPlayer(position: Int?) {
        AudioPlayerManager.getInstance().removeAudio(position ?: 0)
    }

    //添加广告
    fun addWidgetsAd(
        adAudioBean: String?,
        listener: ((adManager: QcAdManager?, view: View?, title: String?) -> Unit)?
    ) {
        val enableAd = CustomPreferencesUtils.fetchEnableAd()
        if (0 == enableAd || activity?.isDestroyed == true) {
            listener?.invoke(null, null, null)
            return
        }

        val adId = PreferencesUtils.getString(PreferencesKey.COMMON_TEMPLATE_AD_TAG)

        val adJson = CustomDownCountUtils.adJson
        if (!TextUtils.isEmpty(adJson)) {//续播广告
            initConnectedFailAd(adJson, adId, listener)
            return
        }

        if (NetUtils.isNetworkConnected(activity)) {
            initConnectedNetWorkAd(adId, listener)
        } else {
            initConnectedFailAd(adAudioBean, adId, listener)
        }
    }

    //处理未联网状态下的广告
    private fun initConnectedFailAd(
        adAudioBeanJson: String?,
        adId: String?,
        listener: ((adManager: QcAdManager?, view: View?, title: String?) -> Unit)?
    ) {
        if (TextUtils.isEmpty(adAudioBeanJson)) {
            listener?.invoke(null, null, null)
            return
        }

        val adAudioBean = GsonUtils
            .fetchGson()
            .fromJson(adAudioBeanJson, AdAudioBean::class.java)

        if (null == adAudioBean) {
            listener?.invoke(null, null, null)
            return
        }

        val progress = fetchAdPlayerProgress()

        //未联网状态下去除互动
        if (!NetUtils.isNetworkConnected(activity)) {
            adAudioBean.interactive = null
        }

        QCiVoiceSdk.get()
            .addCustomTemplateOfflineAd(
                activity,
                fetchAdAttr(),
                adId,
                null,
                adAudioBean,
                Constant.response?.company,
                progress,
                CustomTemplateAdListener(listener)
            )
    }

    //处理联网状态下的广告
    private fun initConnectedNetWorkAd(
        adId: String?,
        listener: ((adManager: QcAdManager?, view: View?, title: String?) -> Unit)?
    ) {
        val progress = fetchAdPlayerProgress()
        QCiVoiceSdk.get().addCustomTemplateAd(
            activity,
            fetchAdAttr(),
            adId,
            null,
            Constant.response?.company,
            progress,
            CustomTemplateAdListener(listener)
        )
    }

    private fun fetchAdPlayerProgress(): Int {
        val currentProgress = (CustomDownCountUtils.instance.currentProgress ?: 0) / 1000
        val isRunning = CustomDownCountUtils.instance.isRunning

        val progress = if (null == CustomDownCountUtils.instance.currentProgress
            || (0 >= currentProgress && !isRunning)
        ) {
            0
        } else {
            val mDuration = CustomDownCountUtils.duration ?: 0
            val mInteraction = CustomDownCountUtils.interaction ?: 0

            if (0 != mInteraction) {//此时已经播放结束品宣,进行互动音频的播放
                mDuration + currentProgress
            } else {
                if (0L == currentProgress && isRunning) {
                    mDuration
                } else {
                    currentProgress.toInt()
                }
            }
        }

        LoggerUtils.e("progress:${progress}")
        return progress.toInt()
    }

    //自定义模板广告的回调
    private inner class CustomTemplateAdListener constructor(
        val listener: ((adManager: QcAdManager?, view: View?, title: String?) -> Unit)?
    ) : QcCustomTemplateListener {
        override fun onAdClick() {
            listener?.invoke(null, null, null)
            interaction = 0
        }

        override fun onAdCompletion() {
            listener?.invoke(null, null, null)
            interaction = 0
        }

        override fun onAdError(message: String?) {
            LoggerUtils.e("onAdError:${message}")
            listener?.invoke(null, null, null)
            interaction = 0
        }

        override fun onAdReceive(adManager: QcAdManager?, view: View?) {
            if (activity?.isDestroyed == true) {
                listener?.invoke(null, null, null)
                interaction = 0
            } else {
                listener?.invoke(adManager, view, adTitle)
            }

            if (!CustomDownCountUtils.instance.isRunning) {
                val event = OnAdProgressEvent()
                event.progress = duration ?: 0
                event.interaction = interaction ?: 0
                event.isCompletion = false
                EventBus.getDefault().post(event)
            }
        }

        override fun onAdExposure() {}

        override fun fetchMainTitle(title: String?) {
            adTitle = title
        }

        override fun onAdSkipClick() {
            listener?.invoke(null, null, null)
            interaction = 0
        }

        override fun onFetchAdContentView(
            adTipView: TextView?,//左上角 广告 标识
            skipLayout: LinearLayout?,//右上角跳过布局
            mainTitleView: TextView?,//下方主标题
            subtitleView: TextView?,//icon 右侧副标题
            understandDescView: TextView?//右下角了解详情
        ) {
            mainTitleView?.textSize = 14f
            subtitleView?.textSize = 12f

            subtitleView?.maxEms = 10
            subtitleView?.maxLines = 2
        }

        override fun onFetchApiResponse(adAudioBean: AdAudioBean?) {
            activity?.let {
                val adAlias = if (it.apiResponse != null) {
                    if (it.apiResponse?.album_type == 2) {
                        it.apiResponse?.album_id.toString()
                    } else {
                        it.apiResponse?.program_id.toString()
                    }
                } else {
                    ""
                }
                if(adAlias != ""){
                    val urls =
                        arrayListOf(adAudioBean?.audiourl.toString(), adAudioBean?.firstimg.toString())
                    val fileNames = arrayListOf("ad_voice.mp3", "ad_cover.jpg")
                    LogUtils.LOGE("开始下载播放广告：$adAlias")
                    DownloadService.adMap?.put(adAlias, adAudioBean!!)
                    DownloadService.start(urls, fileNames, adAlias)
                }
            }

            duration = adAudioBean?.duration
            CustomDownCountUtils.duration = duration
            CustomDownCountUtils.adJson = GsonUtils.toJson(adAudioBean)
        }

        override fun onFetchAdsSendShowExposure(url: String?) {
            super.onFetchAdsSendShowExposure(url)
            if (!NetUtils.isNetworkConnected(activity)) {
                activity?.let {
                    val entity = SendExposureEntity()
                    entity.voice_type = 2
                    entity.url = url
                    RoomManager.getInstance(activity).insertSendExposure(entity)
                }
            }
        }

        override fun onFetchInteractionTimer(mInteraction: Int) {
            interaction = mInteraction
            CustomDownCountUtils.interaction = mInteraction

            val event = OnAdProgressEvent()
            event.progress = duration ?: 0
            event.interaction = interaction ?: 0
            event.isCompletion = false
            EventBus.getDefault().post(event)
        }

        override fun onPlayCompletionListener() {
            val event = OnAdProgressEvent()
            event.progress = duration ?: 0
            event.interaction = interaction ?: 0
            event.isCompletion = true
            EventBus.getDefault().post(event)
        }
    }

    private fun fetchAdAttr(): QcCustomTemplateAttr {
        val attr = QcCustomTemplateAttr()
        //设置封面属性,单位dp,设置MATCH_PARENT 则交由外部容器控制
        val coverStyle = CoverStyle()
        coverStyle.width = RelativeLayout.LayoutParams.MATCH_PARENT
        coverStyle.height = RelativeLayout.LayoutParams.MATCH_PARENT
        coverStyle.radius = 20 //设置封面圆角,单位dp

        attr.coverStyle = coverStyle

        //设置广告icon,单位dp
        val iconStyle = IconStyle()
        iconStyle.width = 30
        iconStyle.height = 30
        iconStyle.radius = 10 //设置icon圆角,单位dp

        iconStyle.layoutGravity = Gravity.BOTTOM //设置icon位置,具体参见Gravity方法

        iconStyle.isEnableMargin = true
        iconStyle.marginLeft = 15
        iconStyle.marginBottom = 13

        attr.iconStyle = iconStyle
        attr.isEnableSkip = true //是否启用右上角跳过,默认启用
        attr.isSkipAutoClose = false //设置倒计时结束后是否自动关闭广告,默认false
        attr.skipTipValue = "关闭" //设置右上角跳过文案,默认为跳过

        return attr
    }

    //界面消失
    fun pagerDismiss() {
        activity?.finish()
    }

    //设置倍速
    fun setPlayerSpeed(speed: ((speed: String?) -> Unit)?) {
        val audioPlayerManager = AudioPlayerManager.getInstance()

        val speeds: MutableList<DialogBottomMoreBean> = mutableListOf()
        val speedArray = PlayerSpeedEnum.values()

        if (!speedArray.isNullOrEmpty()) {
            for (item in speedArray.iterator()) {
                val dialogBean = DialogBottomMoreBean()
                dialogBean.text = if (1.0f == item.value)
                    "正常"
                else
                    item.rewType
                dialogBean.isSelect = audioPlayerManager.currentPlayerSpeed == item.value
                speeds.add(dialogBean)
            }
        }

        DialogUtils.showBottomMoreDialog(
            activity,
            speeds,
            object : OnCommonBottomDialogItemClickListener {
                override fun onItemClick(itemView: View?, position: Int, dialog: Dialog?) {
                    audioPlayerManager.setPlayerSpeed(speedArray[position].value)
                    speed?.invoke(speeds[position].text ?: "")
                    dialog?.dismiss()
                }
            })
    }

    //播放列表
    fun openPlayerList() {
        activity?.openPlayerListWidgets()
    }

    //打开新闻的列表
    fun openNewsList(
        responses: MutableList<GetListResponse>?,
        listener: ((position: Int) -> Unit)?,
        onChangerPlayerListListener: ((responses: MutableList<GetListResponse>) -> Unit)?
    ) {
        payerPagerListSelectorBottomDialog = PlayerPagerListSelectorBottomDialog(activity)
        payerPagerListSelectorBottomDialog?.responses = responses
        payerPagerListSelectorBottomDialog?.setOnItemClickListener(listener)
        payerPagerListSelectorBottomDialog?.setChangerPlayerListener(onChangerPlayerListListener)
        payerPagerListSelectorBottomDialog?.showDialog()
    }

    fun dismissNewList() {
        payerPagerListSelectorBottomDialog?.dismiss()
    }

//    //收藏
//    fun setCollection() {
//        ApiService.instance.keep(albumId, programId)
//            .subscribe(object : CommonObserver<KeepResponse>() {
//                override fun onResponseSuccess(response: KeepResponse?) {
//                    val liveData: CustomLiveDataBean<KeepResponse> = CustomLiveDataBean()
//                    liveData.status = Constant.LIVE_DATA_STATUS_SUCCESS
//                    liveData.data = response
//                    keepLiveData.postValue(liveData)
//                }
//
//                override fun onResponseError(message: String?, e: Throwable?, code: Int?) {
//                    super.onResponseError(message, e, code)
//                    val liveData: CustomLiveDataBean<KeepResponse> = CustomLiveDataBean()
//                    liveData.status = Constant.LIVE_DATA_STATUS_FAIL
//                    keepLiveData.postValue(liveData)
//                }
//            })
//    }

    fun setCommonTransformation() {
        if (!NetUtils.isNetworkConnected(activity)) {
            ToastUtil.showToast("当前无网络连接")
            return
        }

        if (Constant.HEADER_LINES_TAG != albumType) {
            setTransformation()
        } else {
            setNewsTransformation()
        }
    }

    //换一批
    private fun setTransformation() {
        if (otherRecCurrentPosition >= 12) {
            otherRecCurrentPosition = 0
        }
        otherRecCurrentPosition = otherRecCurrentPosition.inc()
        ApiService.instance.otherRec(authorName, albumType, albumId, otherRecCurrentPosition)
            .subscribe(object : CommonObserver<MutableList<GetListResponse>>() {
                override fun onResponseSuccess(response: MutableList<GetListResponse>?) {
                    val liveData: CustomLiveDataBean<MutableList<GetListResponse>> =
                        CustomLiveDataBean()
                    liveData.data = response
                    liveData.status = Constant.LIVE_DATA_STATUS_SUCCESS
                    otherRecLiveData.postValue(liveData)
                }

                override fun onResponseError(message: String?, e: Throwable?, code: Int?) {
                    super.onResponseError(message, e, code)
                    val liveData: CustomLiveDataBean<MutableList<GetListResponse>> =
                        CustomLiveDataBean()
                    liveData.status = Constant.LIVE_DATA_STATUS_FAIL
                    otherRecLiveData.postValue(liveData)
                }
            })
    }

    //换一批
    private fun setNewsTransformation() {
        if (otherRecCurrentPosition >= 12) {
            otherRecCurrentPosition = 0
        }
        otherRecCurrentPosition = otherRecCurrentPosition.inc()
        ApiService.instance.newsOtherRec(company, otherRecCurrentPosition)
            .subscribe(object : CommonObserver<MutableList<GetListResponse>>() {
                override fun onResponseSuccess(response: MutableList<GetListResponse>?) {
                    val liveData: CustomLiveDataBean<MutableList<GetListResponse>> =
                        CustomLiveDataBean()
                    liveData.data = response
                    liveData.status = Constant.LIVE_DATA_STATUS_SUCCESS
                    otherRecLiveData.postValue(liveData)
                }

                override fun onResponseError(message: String?, e: Throwable?, code: Int?) {
                    super.onResponseError(message, e, code)
                    val liveData: CustomLiveDataBean<MutableList<GetListResponse>> =
                        CustomLiveDataBean()
                    liveData.status = Constant.LIVE_DATA_STATUS_FAIL
                    otherRecLiveData.postValue(liveData)
                }
            })
    }

    //收听记录上报
    private fun putListenHistory() {
        if (!NetUtils.isNetworkConnected(activity)) {
            return
        }

        if (Constant.HEADER_LINES_TAG == albumType) {
            ApiService.instance.putNewsListenHistory(albumId)
                .subscribe(object : CommonObserver<Any>() {
                    override fun onResponseSuccess(response: Any?) {}
                })
        } else {
            if (null == programId) {
                return
            }
            ApiService.instance.putListenHistory(albumId, programId)
                .subscribe(object : CommonObserver<Any>() {
                    override fun onResponseSuccess(response: Any?) {}

                    override fun onResponseError(message: String?, e: Throwable?, code: Int?) {}
                })
        }
    }

}