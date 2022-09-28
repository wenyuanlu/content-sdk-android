package com.maishuo.haohai.main.ui

import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.view.View
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.corpize.sdk.ivoice.QCiVoiceSdk
import com.corpize.sdk.ivoice.admanager.QcAdManager
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.upstream.HttpDataSource
import com.kcrason.dynamicpagerindicatorlibrary.DynamicPagerIndicator
import com.maishuo.haohai.R
import com.maishuo.haohai.api.bean.CustomLiveDataBean
import com.maishuo.haohai.api.bean.GetListResponsePackBean
import com.maishuo.haohai.api.bean.KeepResponseEvent
import com.maishuo.haohai.api.response.GetListResponse
import com.maishuo.haohai.api.response.InitIndexResponse
import com.maishuo.haohai.audio.AudioPlayerManager
import com.maishuo.haohai.common.CommonMenuEnum
import com.maishuo.haohai.common.Constant
import com.maishuo.haohai.common.PlayerContentExposureEnum
import com.maishuo.haohai.databinding.FragmentMainHomeLayoutBinding
import com.maishuo.haohai.main.adapter.CommonViewPagerAdapter
import com.maishuo.haohai.main.event.GetProgramListResponseEvent
import com.maishuo.haohai.main.event.OnAdProgressEvent
import com.maishuo.haohai.main.event.SynchronizationCurrentChildPositionEvent
import com.maishuo.haohai.main.service.AudioPlayerService
import com.maishuo.haohai.main.viewmodel.HomeMainViewModel
import com.maishuo.haohai.mediabrowser.MediaBrowserManager
import com.maishuo.haohai.person.service.DownloadService
import com.maishuo.haohai.person.ui.DownloadActivity
import com.maishuo.haohai.person.ui.MyFavoriteActivity
import com.maishuo.haohai.person.ui.SearchActivity
import com.maishuo.haohai.person.viewmodel.PlayHistoryViewModel
import com.maishuo.haohai.receiver.LockReceiver
import com.maishuo.haohai.receiver.NetWorkConnectedChangerReceiver
import com.maishuo.haohai.utils.CustomDownCountUtils
import com.maishuo.haohai.utils.MediaPlayerUtils
import com.maishuo.haohai.utils.Utils
import com.qichuang.commonlibs.basic.CustomBaseFragment
import com.qichuang.commonlibs.common.PreferencesKey
import com.qichuang.commonlibs.utils.*
import com.qichuang.retrofitlibs.retrofit.CommonResCodeEnum
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.math.BigDecimal
import kotlin.math.ceil

/**
 * author : xpSun
 * date : 11/8/21
 * description :
 */
class MainHomeFragment : CustomBaseFragment<FragmentMainHomeLayoutBinding>() {
    private val tabMenus: MutableList<String> = mutableListOf()
    private val fragments: MutableList<Fragment> = mutableListOf()
    private var adapter: CommonViewPagerAdapter? = null
    private var viewModel: HomeMainViewModel? = null
    private var playHistoryViewModel: PlayHistoryViewModel? = null

    private var homeMainVoiceNovelFragment: HomeMainVoiceNovelFragment? = null
    private var homeMainVoiceHeadlinesFragment: HomeMainVoiceHeadlinesFragment? = null
    private var homeMainVoiceCurriculumFragment: HomeMainVoiceCurriculumFragment? = null

    private var netWorkConnectedChangerReceiver: NetWorkConnectedChangerReceiver? = null
    private var audioPlayerManager: AudioPlayerManager? = null
    private var currentChildPosition: Int? = null
        set(value) {
            field = value
            EventBus.getDefault().post(SynchronizationCurrentChildPositionEvent(value?.dec()))
        }
    private var currentPlayerPosition: Int? = null
    private var firstAdManager: QcAdManager? = null
    private var periodIndex: Int? = null

    private var lockReceiver: LockReceiver? = null

    //是否已经曝光过开始
    private var isSendStartExposure: Boolean = false

    //是否已经曝光过25%
    private var isSendFirstQuartileExposure: Boolean = false

    //是否已经曝光过50%
    private var isSendMidpointExposure: Boolean = false

    //是否已经曝光过75%
    private var isSendThirdQuartileExposure: Boolean = false

    //是否已经曝光过100%
    private var isSendCompleteExposure: Boolean = false

    //播放10秒标识
    private var playSecond = 0

    override fun initWidgets() {
        initReceiver()

        var appCompatActivity: AppCompatActivity? = null
        if (activity is AppCompatActivity) {
            appCompatActivity = activity as AppCompatActivity
        }
        viewModel = HomeMainViewModel(appCompatActivity)
        playHistoryViewModel = ViewModelProvider(this).get(PlayHistoryViewModel::class.java)

        //初始化媒体播放器
        audioPlayerManager = AudioPlayerManager.getInstance()
        //每次启动都需要重置该参数
        CustomPreferencesUtils.putCurrentPlayerGroupId(0)

        initObserve()

        MediaBrowserManager.instance.builder(activity)
    }

    private fun initObserve() {
        viewModel?.let {
            it.initIndexLiveData.observe(this, this::initIndex)

            if (NetUtils.isNetworkConnected(context)) {
                it.initIndex()
            } else {
                initIndexFail()
            }
        }
    }

    private fun initReceiver() {
        EventBus.getDefault().register(this)
//        registerLockReceiver()

        netWorkConnectedChangerReceiver = NetWorkConnectedChangerReceiver()
        val filter = IntentFilter()
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION)
        context?.registerReceiver(netWorkConnectedChangerReceiver, filter)

        var appCompatActivity: AppCompatActivity? = null
        if (activity is AppCompatActivity) {
            appCompatActivity = activity as AppCompatActivity
        }
        viewModel = HomeMainViewModel(appCompatActivity)
    }

    override fun initWidgetsEvent() {
        vb?.let {
            it.homeTitleRightSearch.setOnClickListener {
                startActivity(
                    Intent(
                        context,
                        SearchActivity::class.java
                    )
                )
            }
            it.homeTitleRightCollection.setOnClickListener {
                startActivity(
                    Intent(
                        context,
                        MyFavoriteActivity::class.java
                    )
                )
            }
            it.homeTitleRightDownload.setOnClickListener {
                startActivity(
                    Intent(
                        context,
                        DownloadActivity::class.java
                    )
                )
            }
            it.homeTitleTabLayout.setOnItemTabClickListener(object :
                DynamicPagerIndicator.OnItemTabClickListener {
                override fun onItemTabClick(position: Int) {
                    if (!Utils.isFastClick()) {//实现双击
                        LoggerUtils.e("homeTitleTabLayout:${position}")
                    }
                }
            })
            it.homePlayerStatusIcon.setOnClickListener {
                viewModel?.onItemClickClickListener(
                    Constant.response?.album_type,
                    Constant.response,
                    Constant.responses
                )
            }
        }

        audioPlayerManager?.addPlayerEventListenerList(
            object : AudioPlayerManager.OnPlayerEventListener {
                override fun onPlayerStatus(status: Int) {
                    when (status) {
                        AudioPlayerManager.CURRENT_PLAYER_STATUS_STOP,
                        AudioPlayerManager.CURRENT_PLAYER_STATUS_PAUSE,
                        AudioPlayerManager.CURRENT_PLAYER_STATUS_RELEASE -> {
                            vb?.homePlayerStatusIcon?.setImageResource(R.mipmap.player_start_icon)
                            stopAnimation()
                        }
                        AudioPlayerManager.CURRENT_PLAYER_STATUS_PLAYER -> {
                            vb?.homePlayerStatusIcon?.setImageResource(R.mipmap.player_pause_icon)
                            startAnimation()
                        }
                    }
                }

                override fun onPositionDiscontinuity(
                    oldPosition: Player.PositionInfo?,
                    newPosition: Player.PositionInfo?,
                    reason: Int
                ) {
                    if (reason == Player.DISCONTINUITY_REASON_SEEK_ADJUSTMENT ||
                        reason == Player.DISCONTINUITY_REASON_AUTO_TRANSITION
                    ) {
                        onPositionDiscontinuity(newPosition)
                        defaultExposureValue()
                    }
                }

                override fun onPlayerCurrentPosition(
                    position: Long?,
                    duration: Long?,
                    progress: Double?
                ) {
                    playerProgressOpenActivity(duration, progress)

                    sendPlayerExposure(progress)

                    //上报播放进度
                    sendPlayerReport(position)
                }

                override fun onError(error: PlaybackException?, msg: String?) {
                    super.onError(error, msg)

                    try {
                        if (!NetUtils.isNetworkConnected(context)) {
                            ToastUtil.showToast("您的网络好像不太给力,请稍后再试")
                            return
                        }

                        if (audioPlayerManager?.hasNextMediaItem() == true) {
                            ToastUtil.showToast("播放错误，自动播放下一个")
                            audioPlayerManager?.seekToNext()
                            audioPlayerManager?.start()
                        }

                        val cause = error?.cause
                        if (cause is HttpDataSource.InvalidResponseCodeException) {
                            LoggerUtils.e("error.errorCode${error.errorCode},message:${msg}")

                            if (cause.responseCode == CommonResCodeEnum.RES_CODE_404.recCode) {
                                viewModel?.putAberrant()
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            })
    }

    //重置曝光信息
    private fun defaultExposureValue() {
        isSendStartExposure = false
        isSendFirstQuartileExposure = false
        isSendMidpointExposure = false
        isSendThirdQuartileExposure = false
        isSendCompleteExposure = false
    }

    private fun sendPlayerExposure(progress: Double?) {
        val event = when {
            (progress ?: -1.0) in PlayerContentExposureEnum
                .EXPOSURE_START.startDeviation..PlayerContentExposureEnum
                .EXPOSURE_START.endDeviation
                    && !isSendStartExposure -> {
                isSendStartExposure = true
                PlayerContentExposureEnum.EXPOSURE_START.exposureValue
            }
            (progress ?: -1.0) in PlayerContentExposureEnum
                .EXPOSURE_FIRSTQUARTILE.startDeviation..PlayerContentExposureEnum
                .EXPOSURE_FIRSTQUARTILE.endDeviation
                    && !isSendFirstQuartileExposure -> {
                isSendFirstQuartileExposure = true
                PlayerContentExposureEnum.EXPOSURE_FIRSTQUARTILE.exposureValue
            }
            (progress ?: -1.0) in PlayerContentExposureEnum
                .EXPOSURE_MIDPOINT.startDeviation..PlayerContentExposureEnum
                .EXPOSURE_MIDPOINT.endDeviation
                    && !isSendMidpointExposure -> {
                isSendMidpointExposure = true
                PlayerContentExposureEnum.EXPOSURE_MIDPOINT.exposureValue
            }
            (progress ?: -1.0) in PlayerContentExposureEnum
                .EXPOSURE_THIRDQUARTILE.startDeviation..PlayerContentExposureEnum
                .EXPOSURE_THIRDQUARTILE.endDeviation
                    && !isSendThirdQuartileExposure -> {
                isSendThirdQuartileExposure = true
                PlayerContentExposureEnum.EXPOSURE_THIRDQUARTILE.exposureValue
            }
            (progress ?: -1.0) in PlayerContentExposureEnum
                .EXPOSURE_COMPLETE.startDeviation..PlayerContentExposureEnum
                .EXPOSURE_COMPLETE.endDeviation
                    && !isSendCompleteExposure -> {
                isSendCompleteExposure = true
                PlayerContentExposureEnum.EXPOSURE_COMPLETE.exposureValue
            }
            else -> {
                ""
            }
        }

        viewModel?.contentCtrace(event)
    }

    /**
     * 上报播放进度
     */
    private fun sendPlayerReport(position: Long?) {
        val second: Double = position?.toDouble()?.div(1000)!!
        playSecond++
        if (playSecond == 10) {
            playHistoryViewModel?.playReport(ceil(second).toInt())
            playSecond = 0
        }
    }

    private fun checkPlayerProgressOpenActivity(
        duration: Long?,
        progress: Double?
    ): Boolean {
        val showAdPosition =
            PreferencesUtils.getInt(PreferencesKey.CURRENT_CONTENT_SHOW_AD_POSITION)
        val showAdPositionValue = BigDecimal(showAdPosition)
            .divide(
                BigDecimal("100"),
                3,
                BigDecimal.ROUND_HALF_UP
            )

        val showAdEndPosition: Double = showAdPositionValue.toDouble()
        val showAdStartPosition: Double = if ((duration ?: 0) <= 100 * 1000) {
            showAdEndPosition - 0.05
        } else {
            showAdEndPosition - 0.01
        }

        if (Constant.response?.album_type == Constant.HEADER_LINES_TAG) {
            val linesAdInterval = PreferencesUtils.getInt(PreferencesKey.TOUTIAO_PLAY_NUM_INTERVAL)

            if (null == periodIndex && !Constant.responses.isNullOrEmpty()) {
                for (i in Constant.responses!!.indices) {
                    val item = Constant.responses!![i]

                    if (item.album_id == Constant.response?.album_id) {
                        periodIndex = i
                        break
                    }
                }
            }

            if (0 != periodIndex && (periodIndex ?: 0) % linesAdInterval != 0) {
                return false
            }
        }

        if ((progress ?: 0.0) !in showAdStartPosition..showAdEndPosition) {
            return false
        }

        if (Constant.isOpenPlayerPager == true) {
            return false
        }

        return true
    }

    private fun playerProgressOpenActivity(
        duration: Long?,
        progress: Double?
    ) {
        try {
            if (checkPlayerProgressOpenActivity(duration, progress)) {
                viewModel?.onItemClickClickListener(
                    Constant.response?.album_type,
                    Constant.response,
                    Constant.responses
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun startAnimation() {
        val animation = RotateAnimation(
            0f, 359f,
            Animation.RELATIVE_TO_SELF, 0.5f,
            Animation.RELATIVE_TO_SELF, 0.5f,
        )
        animation.duration = 15 * 1000L
        animation.repeatCount = Int.MAX_VALUE

        vb?.homePlayerStatus?.let {
            it.clearAnimation()
            it.startAnimation(animation)
            GlideUtils.initImageForCropTransform(context, Constant.response?.album_cover ?: "", it)
        }
    }

    private fun stopAnimation() {
        vb?.homePlayerStatus?.clearAnimation()
    }

    private fun onPositionDiscontinuity(
        newPosition: Player.PositionInfo?
    ) {
        try {
            if (!Constant.responses.isNullOrEmpty()) {
                var position = newPosition?.periodIndex ?: 0
                if (position > (Constant.responses?.size ?: 0).dec()) {
                    return
                }

                if (0 > position) position = 0

                periodIndex = position

                Constant.response = Constant.responses!![position]

                initAudioNextPlayer(position)
                startAnimation()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun initAudioNextPlayer(newPosition: Int?) {
        if (0 > newPosition ?: 0) {
            return
        }

        if (Constant.HEADER_LINES_TAG != Constant.response?.album_type) {
            if (null == currentPlayerPosition) {
                if (!Constant.responses.isNullOrEmpty()) {
                    for (i in Constant.responses!!.indices) {
                        val item = Constant.responses!![i]
                        if (item.program_id.toString() == audioPlayerManager?.currentMediaItemId) {
                            currentPlayerPosition = i
                            break
                        }
                    }
                }
            }

            if (currentPlayerPosition == newPosition) {
                return
            }
            currentPlayerPosition = newPosition

            val playerListSize = Constant.responses?.size ?: 0
            if ((newPosition ?: 0) >= playerListSize - 1) {//下一首
                currentChildPosition = currentChildPosition?.inc()

                val albumId = Constant.response?.album_id ?: 0
                val pageSize = 10
                val page = currentChildPosition ?: 0
                val playerStatus = Constant.PLAYER_STATUS_TAG_3
                AudioPlayerService.start(context, albumId, pageSize, page, playerStatus)
            }
        }

    }

    //处理初始化接口
    private fun initIndex(liveData: CustomLiveDataBean<InitIndexResponse>?) {
        if (liveData?.status == Constant.LIVE_DATA_STATUS_SUCCESS) {
            initIndexSuccess(liveData)
        } else {
            initIndexFail()
        }
    }

    private fun initIndexFail() {
        tabMenus.clear()
        fragments.clear()

        val menus = CommonMenuEnum.values()

        if (!menus.isNullOrEmpty()) {
            for (item in menus.iterator()) {
                tabMenus.add(item.title)
            }

            homeMainVoiceNovelFragment = HomeMainVoiceNovelFragment { responseIt ->
                Constant.response = responseIt
                vb?.homePlayerStatus?.let {
                    GlideUtils.initImageForCropTransform(
                        context,
                        responseIt?.album_cover ?: "",
                        it
                    )
                }
            }
            fragments.add(homeMainVoiceNovelFragment!!)

            homeMainVoiceHeadlinesFragment = HomeMainVoiceHeadlinesFragment { responseIt ->
                Constant.response = responseIt
                vb?.homePlayerStatus?.let {
                    GlideUtils.initImageForCropTransform(
                        context,
                        responseIt?.album_cover ?: "",
                        it
                    )
                }
            }
            fragments.add(homeMainVoiceHeadlinesFragment!!)

            homeMainVoiceCurriculumFragment = HomeMainVoiceCurriculumFragment { responseIt ->
                Constant.response = responseIt
                vb?.homePlayerStatus?.let {
                    GlideUtils.initImageForCropTransform(
                        context,
                        responseIt?.album_cover ?: "",
                        it
                    )
                }
            }
            fragments.add(homeMainVoiceCurriculumFragment!!)
        }

        viewPagerRefresh()

        addFirstAd()
    }

    private fun initIndexSuccess(liveData: CustomLiveDataBean<InitIndexResponse>?) {
        val data = liveData?.data
        val enableAd = data?.android_a_sw ?: 0
        CustomPreferencesUtils.putEnableAd(enableAd)

        val token = liveData?.data?.token
        PreferencesUtils.putString(PreferencesKey.TOKEN, token)

        tabMenus.clear()
        fragments.clear()

        if (!data?.nav_switch.isNullOrEmpty()) {
            for (item in data?.nav_switch!!.iterator()) {
                tabMenus.add(item.name ?: "")
                when (item.nav_data_view_type) {
                    Constant.VOICE_NOVEL_TAG -> {
                        homeMainVoiceNovelFragment = HomeMainVoiceNovelFragment { responseIt ->
                            Constant.response = responseIt
                            vb?.homePlayerStatus?.let {
                                GlideUtils.initImageForCropTransform(
                                    context,
                                    responseIt?.album_cover ?: "",
                                    it
                                )
                            }
                        }
                        fragments.add(homeMainVoiceNovelFragment!!)
                    }
                    Constant.HEADER_LINES_TAG -> {
                        homeMainVoiceHeadlinesFragment =
                            HomeMainVoiceHeadlinesFragment { responseIt ->
                                Constant.response = responseIt
                                vb?.homePlayerStatus?.let {
                                    GlideUtils.initImageForCropTransform(
                                        context,
                                        responseIt?.album_cover ?: "",
                                        it
                                    )
                                }
                            }
                        fragments.add(homeMainVoiceHeadlinesFragment!!)
                    }
                    Constant.VOICE_CURRICULUM_TAG -> {
                        homeMainVoiceCurriculumFragment =
                            HomeMainVoiceCurriculumFragment { responseIt ->
                                Constant.response = responseIt
                                vb?.homePlayerStatus?.let {
                                    GlideUtils.initImageForCropTransform(
                                        context,
                                        responseIt?.album_cover ?: "",
                                        it
                                    )
                                }
                            }
                        fragments.add(homeMainVoiceCurriculumFragment!!)
                    }
                }
            }

            if (!fragments.isNullOrEmpty()) {
                vb?.homeTitleViewpager?.currentItem = 0
            }

            PreferencesUtils.putInt(
                PreferencesKey.AD_INFORMATION_INTERVAL,
                data.ad_information_interval ?: 5
            )
            PreferencesUtils.putInt(
                PreferencesKey.AD_SEARCH_INTERVAL,
                data.ad_search_interval ?: 3
            )
            PreferencesUtils.putInt(
                PreferencesKey.TOUTIAO_PLAY_NUM_INTERVAL,
                data.toutiao_play_num_interval ?: 3
            )
            PreferencesUtils.putInt(
                PreferencesKey.TOUTIAO_POSITION_AD_INTERVAL,
                data.toutiao_position_ad_interval ?: 3
            )
            PreferencesUtils.putInt(
                PreferencesKey.CURRENT_CONTENT_SHOW_AD_POSITION,
                data.current_content_show_ad_position ?: 75
            )

            PreferencesUtils.putString(
                PreferencesKey.COMMON_FIRST_AD_TAG,
                data.a_list?.first_listen
            )
            PreferencesUtils.putString(
                PreferencesKey.COMMON_TEMPLATE_AD_TAG,
                data.a_list?.information_flow
            )

            viewPagerRefresh()
        }

        addFirstAd()
    }

    private fun viewPagerRefresh() {
        vb?.let {
            adapter =
                CommonViewPagerAdapter(childFragmentManager, fragments, tabMenus)
            it.homeTitleViewpager.adapter = adapter
            it.homeTitleTabLayout.viewPager = it.homeTitleViewpager
            it.homeTitleViewpager.offscreenPageLimit = tabMenus.size
        }
    }

    private fun addFirstAd() {
        val enableAd = CustomPreferencesUtils.fetchEnableAd()

        if (0 == enableAd) {
            return
        }

        viewModel?.addFirstAd { view, manager ->
            firstAdManager = manager
            vb?.mainHomeAdLayout?.removeAllViews()
            if (null == view) {
                vb?.mainHomeAdLayout?.visibility = View.GONE
            } else {
                vb?.mainHomeAdLayout?.visibility = View.VISIBLE
                vb?.mainHomeAdLayout?.addView(view)
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: KeepResponseEvent?) {
        when (event?.channelType) {
            Constant.VOICE_NOVEL_TAG -> {
                Constant.responses = initGetListResponseKeepStatus(
                    homeMainVoiceNovelFragment?.apiResponses,
                    event.album_id,
                    event.status
                )
                homeMainVoiceNovelFragment?.apiResponses = Constant.responses
            }
            Constant.HEADER_LINES_TAG -> {

            }
            Constant.VOICE_CURRICULUM_TAG -> {
                Constant.responses = initGetListResponseKeepStatus(
                    homeMainVoiceCurriculumFragment?.apiResponses,
                    event.album_id,
                    event.status
                )
                homeMainVoiceCurriculumFragment?.apiResponses = Constant.responses
            }
        }
    }

//    //获取播放列表
//    @Subscribe(threadMode = ThreadMode.MAIN)
//    fun onMessageEvent(event: GetProgramListParamEvent?) {
//        if (Constant.HEADER_LINES_TAG != Constant.response?.album_type) {
//            currentChildPosition = event?.page
//
//            val albumId = event?.album_id ?: 0
//            val pageSize = event?.page_size ?: 10
//            val page = event?.page ?: 1
//            val playerStatus = event?.playerStatus ?: 0
//            AudioPlayerService.start(context, albumId, pageSize, page, playerStatus)
//        }
//    }

    //获取播放列表
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: GetProgramListResponseEvent?) {
        //0.默认,1.自动播放,2.上一页,3.滑动到特定位置需要自动加载下一页
        when (event?.playerStatus) {
            Constant.PLAYER_STATUS_TAG_1 -> {
                Constant.responses = event.data?.data

                MediaBrowserManager.instance.initPlayerList(
                    Constant.responses,
                    currentChildPosition
                )
            }
            Constant.PLAYER_STATUS_TAG_2 -> {
                val list = event.data?.data
                if (!list.isNullOrEmpty()) {
                    Constant.responses = list
                }

                MediaBrowserManager.instance.initPlayerList(
                    Constant.responses,
                    currentChildPosition
                )
            }
            Constant.PLAYER_STATUS_TAG_3 -> {
                val list = event.data?.data
                if (!list.isNullOrEmpty()) {
                    Constant.responses?.addAll(list)
                    val mediaPlays = MediaPlayerUtils.responseToMedia(
                        Constant.response?.album_type,
                        list
                    )
                    audioPlayerManager?.addAudios(
                        mediaPlays
                    )
                }

                MediaBrowserManager.instance.initPlayerList(
                    Constant.responses,
                    currentPlayerPosition
                )
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: GetListResponse?) {
        Constant.response = event
        event?.album_id?.let { PreferencesUtils.putInt(PreferencesKey.ALBUM_ID, it) }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: GetListResponsePackBean?) {
        Constant.responses = event?.datas
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: OnAdProgressEvent?) {
        val progress = event?.progress ?: 0
        val interaction = event?.interaction ?: 0

        val millisInFuture =
            if (CustomDownCountUtils.instance.isRunning && event?.isCompletion != true) {
                if (0 != progress) {
                    progress
                } else {
                    CustomDownCountUtils.duration ?: 0
                }
            } else {
                progress + interaction
            }

        LoggerUtils.e("millisInFuture:${millisInFuture},progress:${progress},interaction:${interaction}")

        CustomDownCountUtils.instance.initDownCount(millisInFuture * 1000L)
    }

    private fun initGetListResponseKeepStatus(
        list: MutableList<GetListResponse>?,
        album_id: Int?,
        keepStatus: Int?
    ): MutableList<GetListResponse>? {
        if (list.isNullOrEmpty()) {
            return null
        }

        for (item in list.iterator()) {
            if (album_id == item.album_id) {
                item.keep_status = keepStatus
                break
            }
        }

        return list
    }

    override fun onStart() {
        super.onStart()
        MediaBrowserManager.instance.connect()
    }

    override fun onResume() {
        super.onResume()
        //必须调用
        QCiVoiceSdk.get().onResume()
    }

    override fun onPause() {
        super.onPause()
        //必须调用
        QCiVoiceSdk.get().onPause()
    }

    override fun onStop() {
        super.onStop()
//        MediaBrowserManager.instance.disconnect()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        //释放内存
        QCiVoiceSdk.get().onDestroy()

        EventBus.getDefault().unregister(this)

        if (null != netWorkConnectedChangerReceiver) {
            context?.unregisterReceiver(netWorkConnectedChangerReceiver)
        }

//        if (null != lockReceiver) {
//            context?.unregisterReceiver(lockReceiver)
//        }

        AudioPlayerService.stop(context)

        onAdDestroy()

        audioPlayerManager?.clearCallBacks()

        //停止下载服务
        DownloadService.stop()
    }

    fun onAdDestroy() {
        firstAdManager?.destroy()

        vb?.mainHomeAdLayout?.removeAllViews()
        vb?.mainHomeAdLayout?.visibility = View.GONE
    }

    /**
     * 注册锁屏广播
     */
    private fun registerLockReceiver() {
        try {
            lockReceiver = LockReceiver()
            val filter = IntentFilter()
            filter.addAction(Intent.ACTION_SCREEN_OFF)
            context?.registerReceiver(lockReceiver, filter)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }
}