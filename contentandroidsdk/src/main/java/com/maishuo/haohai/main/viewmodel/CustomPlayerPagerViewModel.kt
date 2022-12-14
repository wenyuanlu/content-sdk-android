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

    //??????????????????
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

    //????????????
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
        if (!TextUtils.isEmpty(adJson)) {//????????????
            initConnectedFailAd(adJson, adId, listener)
            return
        }

        if (NetUtils.isNetworkConnected(activity)) {
            initConnectedNetWorkAd(adId, listener)
        } else {
            initConnectedFailAd(adAudioBean, adId, listener)
        }
    }

    //?????????????????????????????????
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

        //??????????????????????????????
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

    //??????????????????????????????
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

            if (0 != mInteraction) {//??????????????????????????????,???????????????????????????
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

    //??????????????????????????????
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
            adTipView: TextView?,//????????? ?????? ??????
            skipLayout: LinearLayout?,//?????????????????????
            mainTitleView: TextView?,//???????????????
            subtitleView: TextView?,//icon ???????????????
            understandDescView: TextView?//?????????????????????
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
                    LogUtils.LOGE("???????????????????????????$adAlias")
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
        //??????????????????,??????dp,??????MATCH_PARENT ???????????????????????????
        val coverStyle = CoverStyle()
        coverStyle.width = RelativeLayout.LayoutParams.MATCH_PARENT
        coverStyle.height = RelativeLayout.LayoutParams.MATCH_PARENT
        coverStyle.radius = 20 //??????????????????,??????dp

        attr.coverStyle = coverStyle

        //????????????icon,??????dp
        val iconStyle = IconStyle()
        iconStyle.width = 30
        iconStyle.height = 30
        iconStyle.radius = 10 //??????icon??????,??????dp

        iconStyle.layoutGravity = Gravity.BOTTOM //??????icon??????,????????????Gravity??????

        iconStyle.isEnableMargin = true
        iconStyle.marginLeft = 15
        iconStyle.marginBottom = 13

        attr.iconStyle = iconStyle
        attr.isEnableSkip = true //???????????????????????????,????????????
        attr.isSkipAutoClose = false //????????????????????????????????????????????????,??????false
        attr.skipTipValue = "??????" //???????????????????????????,???????????????

        return attr
    }

    //????????????
    fun pagerDismiss() {
        activity?.finish()
    }

    //????????????
    fun setPlayerSpeed(speed: ((speed: String?) -> Unit)?) {
        val audioPlayerManager = AudioPlayerManager.getInstance()

        val speeds: MutableList<DialogBottomMoreBean> = mutableListOf()
        val speedArray = PlayerSpeedEnum.values()

        if (!speedArray.isNullOrEmpty()) {
            for (item in speedArray.iterator()) {
                val dialogBean = DialogBottomMoreBean()
                dialogBean.text = if (1.0f == item.value)
                    "??????"
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

    //????????????
    fun openPlayerList() {
        activity?.openPlayerListWidgets()
    }

    //?????????????????????
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

//    //??????
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
            ToastUtil.showToast("?????????????????????")
            return
        }

        if (Constant.HEADER_LINES_TAG != albumType) {
            setTransformation()
        } else {
            setNewsTransformation()
        }
    }

    //?????????
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

    //?????????
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

    //??????????????????
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