package com.maishuo.haohai.main.viewmodel

import android.graphics.Color
import android.graphics.Typeface
import android.text.TextUtils
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import com.corpize.sdk.ivoice.AdRollAttr
import com.corpize.sdk.ivoice.QCiVoiceSdk
import com.corpize.sdk.ivoice.admanager.QcAdManager
import com.corpize.sdk.ivoice.bean.response.AdAudioBean
import com.corpize.sdk.ivoice.listener.QcFirstVoiceAdViewListener
import com.corpize.sdk.ivoice.listener.QcRollAdViewListener
import com.maishuo.haohai.api.bean.CustomLiveDataBean
import com.maishuo.haohai.api.response.GetListResponse
import com.maishuo.haohai.api.response.InitIndexResponse
import com.maishuo.haohai.api.response.KeepResponse
import com.maishuo.haohai.api.retrofit.ApiService
import com.maishuo.haohai.common.Constant
import com.maishuo.haohai.common.ContentAndroidSDK
import com.maishuo.haohai.listener.OnHomeFetchAdListener
import com.maishuo.haohai.main.lite.CustomLitePlayerFragment
import com.maishuo.haohai.person.service.DownloadCallBack
import com.maishuo.haohai.person.service.DownloadService
import com.qichuang.commonlibs.basic.BaseViewModel
import com.qichuang.commonlibs.common.PreferencesKey
import com.qichuang.commonlibs.download.DownloadFileUtil
import com.qichuang.commonlibs.utils.*
import com.qichuang.retrofitlibs.retrofit.CommonObserver
import com.qichuang.roomlib.RoomManager
import com.qichuang.roomlib.entity.ListenCommonEntity
import com.qichuang.roomlib.entity.SendExposureEntity
import retrofit2.Response
import java.io.File

/**
 * author : xpSun
 * date : 11/16/21
 * description :
 */
class HomeMainViewModel constructor(val activity: AppCompatActivity?) : BaseViewModel(),
    DownloadCallBack {

    val initIndexLiveData: MutableLiveData<CustomLiveDataBean<InitIndexResponse>> =
        MutableLiveData()
    val fetchMainListLiveData: MutableLiveData<CustomLiveDataBean<MutableList<GetListResponse>>> =
        MutableLiveData()
    val keepLiveData: MutableLiveData<CustomLiveDataBean<KeepResponse>> = MutableLiveData()
    val initResponseAdLiveData: MutableLiveData<MutableList<GetListResponse>> = MutableLiveData()

    private val responseMaps: MutableMap<Int, MutableList<GetListResponse>> = mutableMapOf()
    private val catalogues: MutableList<GetListResponse> = mutableListOf()

    private var firstManager: QcAdManager? = null

    var adAudioBean: AdAudioBean? = null

    private var customLitePlayerFragment: CustomLitePlayerFragment? = null

    //离线状态获取数据库中已下载的数据
    fun fetchOffLineDatas(
        type: Int?,
        listener: ((responses: MutableList<GetListResponse>?) -> Unit)?
    ) {
        activity?.let {
            val entitys = RoomManager.getInstance(activity).loadListenCommonByAlbumType(type ?: 0)
            if (entitys.isNullOrEmpty()) {
                listener?.invoke(null)
                return
            }

            catalogues.clear()

            if (Constant.HEADER_LINES_TAG == type) {
                for (item in entitys.iterator()) {
                    val response = entityToResponse(item) ?: break
                    catalogues.add(response)
                }
            } else {
                for (item in entitys.iterator()) {
                    val albumId = item.album_id ?: break
                    val response = entityToResponse(item) ?: break
                    if (!responseMaps.containsKey(albumId)) {
                        val responses: MutableList<GetListResponse> = mutableListOf()
                        responses.add(response)
                        responseMaps[albumId] = responses
                    } else {
                        responseMaps[albumId]?.add(response)
                    }
                }

                if (!responseMaps.isNullOrEmpty()) {
                    for (item in responseMaps.iterator()) {
                        if (!item.value.isNullOrEmpty()) {
                            catalogues.add(item.value[0])
                        }
                    }
                }
            }

            listener?.invoke(catalogues)
        }
    }

    private fun entityToResponse(entity: ListenCommonEntity?): GetListResponse? {
        val jsonValue = GsonUtils.toJson(entity)
        return GsonUtils.fetchGson().fromJson(jsonValue, GetListResponse::class.java)
    }

    fun initResponseAd(apiResponses: MutableList<GetListResponse>?, adInterval: Int?) {
        val enableAd = CustomPreferencesUtils.fetchEnableAd()
        if (0 == enableAd) {
            initResponseAdLiveData.postValue(apiResponses)
            return
        }

        val responses: MutableList<GetListResponse> = mutableListOf()
        if (!apiResponses.isNullOrEmpty()) {
            for (i in apiResponses.indices) {
                if (0 != i && i % (adInterval ?: 0) == 0) {
                    val item = GetListResponse()
                    item.isAd = 1
                    responses.add(item)
                }
                responses.add(apiResponses[i])
            }

            initResponseAdLiveData.postValue(responses)
        }
    }

    fun initIndex() {
        ApiService.instance.initIndex(ContentAndroidSDK.mid, ContentAndroidSDK.contentId)
            .subscribe(object : CommonObserver<InitIndexResponse>(true) {
                override fun onResponseSuccess(response: InitIndexResponse?) {
                    PreferencesUtils.putString(
                        PreferencesKey.USER_AGREEMENT_URL,
                        response?.agreement_url
                    )
                    PreferencesUtils.putString(
                        PreferencesKey.PRIVACY_PROTECT_AGREEMENT_URL,
                        response?.personal_information_assistance_right
                    )
                    val liveDataBean = CustomLiveDataBean<InitIndexResponse>()
                    liveDataBean.data = response
                    liveDataBean.status = Constant.LIVE_DATA_STATUS_SUCCESS
                    initIndexLiveData.postValue(liveDataBean)
                }

                override fun onResponseError(message: String?, e: Throwable?, code: Int?) {
                    super.onResponseError(message, e, code)
                    val liveDataBean = CustomLiveDataBean<InitIndexResponse>()
                    liveDataBean.status = Constant.LIVE_DATA_STATUS_FAIL
                    initIndexLiveData.postValue(liveDataBean)
                }
            })
    }

    fun fetchMainList(
        album_type: Int?,
        program_name: String?,
        page_size: Int?,
        page: Int?
    ) {
        ApiService.instance.fetMainList(album_type, program_name, page_size, page)
            .subscribe(object : CommonObserver<MutableList<GetListResponse>>() {
                override fun onResponseSuccess(response: MutableList<GetListResponse>?) {
                    val liveDataBean = CustomLiveDataBean<MutableList<GetListResponse>>()
                    liveDataBean.data = response
                    liveDataBean.status = Constant.LIVE_DATA_STATUS_SUCCESS
                    fetchMainListLiveData.postValue(liveDataBean)
                }

                override fun onResponseError(message: String?, e: Throwable?, code: Int?) {
                    super.onResponseError(message, e, code)
                    val liveDataBean = CustomLiveDataBean<MutableList<GetListResponse>>()
                    liveDataBean.status = Constant.LIVE_DATA_STATUS_FAIL
                    fetchMainListLiveData.postValue(liveDataBean)
                }
            })
    }

    fun keep(album_id: Int?, program_id: Int?) {
        ApiService.instance.keep(album_id, program_id)
            .subscribe(object : CommonObserver<KeepResponse>() {
                override fun onResponseSuccess(response: KeepResponse?) {
                    val liveData: CustomLiveDataBean<KeepResponse> = CustomLiveDataBean()
                    liveData.status = Constant.LIVE_DATA_STATUS_SUCCESS
                    liveData.data = response
                    keepLiveData.postValue(liveData)
                }

                override fun onResponseError(message: String?, e: Throwable?, code: Int?) {
                    super.onResponseError(message, e, code)
                    val liveData: CustomLiveDataBean<KeepResponse> = CustomLiveDataBean()
                    liveData.status = Constant.LIVE_DATA_STATUS_FAIL
                    keepLiveData.postValue(liveData)
                }
            })
    }

    fun onItemClickClickListener(
        channelType: Int?,
        response: GetListResponse? = null,
        responses: MutableList<GetListResponse>? = null
    ) {
        try {
            if (null == response && responses.isNullOrEmpty()) {
                ToastUtil.showToast("暂无内容")
                return
            }

            ContentAndroidSDK.firstAdDestroy()

            if (!NetUtils.isNetworkConnected(activity) &&
                responseMaps.isNullOrEmpty()
            ) {
                fetchOffLineDatas(response?.album_type) {}
            }

            //response
            val data = if (null != Constant.response?.album_id &&
                Constant.response?.album_id == response?.album_id
            ) {
                Constant.response
            } else {
                response
            }

            //responses
            val datas = if (!NetUtils.isNetworkConnected(activity)) {
                if (Constant.HEADER_LINES_TAG == response?.album_type) {
                    catalogues
                } else {
                    if (responseMaps.containsKey(response?.album_id)) {
                        responseMaps[response?.album_id]
                    } else {
                        null
                    }
                }
            } else {
                fetchRemoveAdData(responses)
            }

//            CustomPlayerPagerActivity.start(
//                activity,
//                channelType,
//                response,
//                if (datas.isNullOrEmpty()) {
//                    null
//                } else {
//                    GetListResponsePackBean(datas)
//                }
//            )
            ContentAndroidSDK.openHaoHaiPlayerView(activity,response)

            Constant.responses = datas
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun fetchRemoveAdData(
        responses: MutableList<GetListResponse>?
    ): MutableList<GetListResponse>? {
        val datas: MutableList<GetListResponse> = mutableListOf()
        if (responses.isNullOrEmpty()) {
            return null
        }

        for (item in responses.iterator()) {
            if (TextUtils.isEmpty(item.mp3_url)) {
                continue
            }

            datas.add(item)
        }
        return datas
    }

    fun fetchHomeAd(listener: OnHomeFetchAdListener?) {
        val enableAd = CustomPreferencesUtils.fetchEnableAd()
        if (0 == enableAd) {
            listener?.onAdError()
            return
        }

        QCiVoiceSdk.get().createAdNative(activity)

        val adRollAttr = AdRollAttr()

        adRollAttr.adWidth = ViewGroup.LayoutParams.MATCH_PARENT
        adRollAttr.adHeight = ViewGroup.LayoutParams.WRAP_CONTENT
        adRollAttr.backgroundColor = Color.WHITE

        val imageStyle: AdRollAttr.ImageStyle = AdRollAttr.ImageStyle()
        imageStyle.width = 84
        imageStyle.height = 100
        imageStyle.marginLeft = 10
        imageStyle.marginTop = 10
        imageStyle.marginRight = 10
        imageStyle.marginBottom = 10
        adRollAttr.leftImageStyle = imageStyle

        val titleStyle: AdRollAttr.TitleStyle = AdRollAttr.TitleStyle()
        titleStyle.marginTop = 20
        titleStyle.marginRight = 10
        titleStyle.textColor = Color.parseColor("#333333")
        titleStyle.textSize = 16
        titleStyle.textStyle = Typeface.BOLD
        adRollAttr.titleStyle = titleStyle

        val descStyle: AdRollAttr.DescStyle = AdRollAttr.DescStyle()
        descStyle.textColor = Color.parseColor("#cccccc")
        descStyle.textSize = 12
        descStyle.marginLeft = 10
        adRollAttr.descStyle = descStyle

        val rightBottomIconStyle: AdRollAttr.RightBottomIconStyle =
            AdRollAttr.RightBottomIconStyle()
        rightBottomIconStyle.marginRight = 10
        rightBottomIconStyle.marginBottom = 10
        adRollAttr.rightBottomIconStyle = rightBottomIconStyle

        QCiVoiceSdk.get().addRollAd(
            activity,
            Constant.COMMON_ADID,
            adRollAttr,
            object : QcRollAdViewListener {
                override fun onAdClick() {
                    LoggerUtils.e("onAdClick")
                }

                override fun onAdCompletion() {
                    LoggerUtils.e("onAdCompletion")
                }

                override fun onAdError(message: String?) {
                    LoggerUtils.e("onAdError${message}")
                    listener?.onAdError()
                }

                override fun onAdReceive(manager: QcAdManager?, view: View?) {
                    LoggerUtils.e("onAdReceive")
                    listener?.onAdReceive(manager, view)
                }

                override fun onAdExposure() {
                    LoggerUtils.e("onAdExposure")
                }

                override fun onRollAdClickClose() {
                    LoggerUtils.e("onRollAdClickClose")
                    listener?.onRollAdClickClose()
                }

                override fun onRollAdDialogShow() {
                    LoggerUtils.e("onRollAdDialogShow")
                }

                override fun onRollAdDialogDismiss() {
                    LoggerUtils.e("onRollAdDialogDismiss")
                }

                override fun onRollVolumeChanger(status: Int) {
                    LoggerUtils.e("onRollVolumeChanger${status}")
                }
            })
    }

    fun addFirstAd(listener: ((view: View?, manager: QcAdManager?) -> Unit)?) {
        val enableAd = CustomPreferencesUtils.fetchEnableAd()
        if (0 == enableAd) {
            listener?.invoke(null, null)
            return
        }

//        val adId = if (BuildConfig.DEBUG) {
//            Constant.COMMON_FIRST_AD_ID
//        } else {
//            PreferencesUtils.getString(PreferencesKey.COMMON_FIRST_AD_TAG)
//        }

        val adId = PreferencesUtils.getString(PreferencesKey.COMMON_FIRST_AD_TAG)

        if (NetUtils.isNetworkConnected(activity)) {
            QCiVoiceSdk.get()
                .addFirstVoiceAd(
                    activity,
                    adId,
                    null,
                    CustomQcFirstVoiceAdViewListener(listener)
                )
        } else {
            val adJson = PreferencesUtils.getString(PreferencesKey.AD_JSON)

            if (TextUtils.isEmpty(adJson)) {
                listener?.invoke(null, null)
                return
            }

            val adAudioBean = GsonUtils.fetchGson().fromJson(adJson, AdAudioBean::class.java)

            if (null == adAudioBean) {
                listener?.invoke(null, null)
                return
            }

            adAudioBean.interactive = null
            QCiVoiceSdk.get().addFirstVoiceOfflineAd(
                activity,
                adId,
                null,
                adAudioBean,
                CustomQcFirstVoiceAdViewListener(listener)
            )
        }
    }

    private inner class CustomQcFirstVoiceAdViewListener constructor(
        val listener: ((view: View?, manager: QcAdManager?) -> Unit)?
    ) : QcFirstVoiceAdViewListener {
        override fun onAdClick() {
            LoggerUtils.e("onAdClick")
        }

        override fun onAdCompletion() {
            LoggerUtils.e("onAdCompletion")
        }

        override fun onAdError(message: String?) {
            LoggerUtils.e("onAdError:${message}")
            listener?.invoke(null, null)
            //如果进程杀死服务是不会停止的，防止这种情况需要在用到Download前初始化
            // 广告获取失败初始化，广告获取成功直接下载首听
            DownloadService.start()
        }

        override fun onFirstVoiceAdClose() {
            LoggerUtils.e("onFirstVoiceAdClose")
        }

        override fun onFirstVoiceAdCountDownCompletion() {
            LoggerUtils.e("onFirstVoiceAdCountDownCompletion")
        }

        override fun onAdExposure() {
            LoggerUtils.e("onAdExposure")
        }

        override fun onAdReceive(manager: QcAdManager?, view: View?) {
            LoggerUtils.e("onAdReceive")
            listener?.invoke(view, manager)
            firstManager = manager
            firstManager?.startPlayAd()
        }

        override fun onFetchApiResponse(adAudioBean: AdAudioBean?) {
            if (TextUtils.isEmpty(adAudioBean?.audiourl)) {
                return
            }

            this@HomeMainViewModel.adAudioBean = adAudioBean

            activity?.let {
                if (adAudioBean != null) {
                    if (!TextUtils.isEmpty(adAudioBean.logo)) {
                        val urls = arrayListOf(
                            adAudioBean.audiourl.toString(),
                            adAudioBean.logo.toString()
                        )
                        val fileNames = arrayListOf("first_voice.mp3", "logo.gif")
                        val alias = "first_voice/first_voice"
                        DownloadService.downloadCallBack = this@HomeMainViewModel
                        DownloadService.adMap?.put(alias, adAudioBean)
                        DownloadService.start(urls, fileNames, alias)
                    } else {
                        val urls = arrayListOf(adAudioBean.audiourl.toString())
                        val fileNames = arrayListOf("first_voice.mp3")
                        val alias = "first_voice/first_voice"
                        DownloadService.downloadCallBack = this@HomeMainViewModel
                        DownloadService.adMap?.put(alias, adAudioBean)
                        DownloadService.start(urls, fileNames, alias)
                    }
                }
            }
        }

        override fun onFetchAdsSendShowExposure(url: String?) {
            super.onFetchAdsSendShowExposure(url)
            activity?.let {
                val entity = SendExposureEntity()
                entity.voice_type = 1
                entity.url = url
                RoomManager.getInstance(activity).insertSendExposure(entity)
            }
        }
    }

    fun contentCtrace(event: String?) {
        if (TextUtils.isEmpty(event)) {
            return
        }

        LoggerUtils.e("event:${event}")

        val response = Constant.response
        val token = PreferencesUtils.getString(PreferencesKey.TOKEN)

        val chapterId = if (Constant.HEADER_LINES_TAG == response?.album_type) {
            response.album_lid ?: ""
        } else {
            response?.program_lid ?: ""
        }

        val mid = ContentAndroidSDK.mid
        if (!NetUtils.isNetworkConnected(activity)) {
            try {
                activity?.let {
                    val entity = SendExposureEntity()
                    entity.mid = mid
                    entity.voice_type = 3
                    entity.provider = response?.company
                    entity.uid = token
                    entity.type = response?.album_type
                    entity.chapterid = chapterId
                    entity.event = event
                    RoomManager.getInstance(activity)
                        .insertSendExposure(entity)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return
        }

        ApiService.instance.contentCtrace(
            mid,
            response?.company,
            token,
            response?.album_type,
            chapterId,
            event
        ).subscribe(object : CommonObserver<Response<Void>>(true) {
            override fun onResponseSuccess(response: Response<Void>?) {}
            override fun onResponseError(message: String?, e: Throwable?, code: Int?) {}
        })
    }

    override fun downloadFirstAdComplete(adBeanJson: String?) {
        super.downloadFirstAdComplete(adBeanJson)
        //删除上次下载的首听
        val oldAd = PreferencesUtils.getString(PreferencesKey.AD_JSON)
        if (!oldAd.isNullOrBlank()) {
            val adBean = GsonUtils.fetchGson().fromJson(oldAd, AdAudioBean::class.java)
            if (adBean != null && !adBean.audiourl.isNullOrBlank() && adBean.audiourl != null) {
                DownloadFileUtil().delete(File(adBean.audiourl).parent!!)
            }
        }
        PreferencesUtils.putString(PreferencesKey.AD_JSON, adBeanJson)
    }

    //异常上报(不删除只上报)
    fun putAberrant() {
        if (!NetUtils.isNetworkConnected(activity)) {
            return
        }

        ApiService.instance.putAberrant(
            Constant.response?.album_type,
            Constant.response?.album_id,
            Constant.response?.program_id
        )
            .subscribe(object : CommonObserver<Any>() {
                override fun onResponseSuccess(response: Any?) {}
            })
    }
}