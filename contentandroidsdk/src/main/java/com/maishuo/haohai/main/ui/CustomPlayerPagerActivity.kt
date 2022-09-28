package com.maishuo.haohai.main.ui

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.Gravity
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.corpize.sdk.ivoice.QCiVoiceSdk
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.Player.REPEAT_MODE_OFF
import com.google.android.exoplayer2.Player.REPEAT_MODE_ONE
import com.gyf.immersionbar.ImmersionBar
import com.maishuo.haohai.R
import com.maishuo.haohai.api.bean.GetListResponsePackBean
import com.maishuo.haohai.api.response.GetListResponse
import com.maishuo.haohai.audio.AudioPlayerManager
import com.maishuo.haohai.audio.PlayerSpeedEnum
import com.maishuo.haohai.common.Constant
import com.maishuo.haohai.databinding.ActivityPlayerPagerLayoutBinding
import com.maishuo.haohai.listener.OnDialogCallBackListener
import com.maishuo.haohai.main.adapter.CustomPlayerCatalogueChapterAdapter
import com.maishuo.haohai.main.adapter.CustomPlayerCatalogueChapterContentAdapter
import com.maishuo.haohai.main.adapter.PlayerOtherRecAdapter
import com.maishuo.haohai.main.event.GetProgramListParamEvent
import com.maishuo.haohai.main.event.GetProgramListResponseEvent
import com.maishuo.haohai.main.event.SynchronizationCurrentChildPositionEvent
import com.maishuo.haohai.main.viewmodel.CustomPlayerPagerViewModel
import com.maishuo.haohai.mediabrowser.MediaBrowserManager
import com.maishuo.haohai.person.service.DownloadCallBack
import com.maishuo.haohai.person.service.DownloadService
import com.maishuo.haohai.utils.BlurTransformation
import com.maishuo.haohai.utils.CustomDownCountUtils
import com.maishuo.haohai.utils.DialogUtils
import com.maishuo.haohai.utils.permission.PermissionUtil
import com.maishuo.haohai.widgets.control.OnControlViewClickListener
import com.qichuang.commonlibs.basic.CustomBaseActivity
import com.qichuang.commonlibs.common.PreferencesKey
import com.qichuang.commonlibs.utils.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.math.BigDecimal

/**
 * author : xpSun
 * date : 11/8/21
 * description :单独的播放首页
 */
class CustomPlayerPagerActivity : CustomBaseActivity<ActivityPlayerPagerLayoutBinding>(),
    DownloadCallBack {

    companion object {
        private const val COMMON_PARAM_TAG: String = "common_param_tag"
        private const val COMMON_PARAM_LIST_TAG: String = "common_param_list_tag"
        private const val COMMON_CHANNEL_TYPE_TAG: String = "common_channel_type_tag"

        private const val ON_AD_SHOW_STATUS_0: Int = 0
        private const val ON_AD_SHOW_STATUS_1: Int = 1
        private const val ON_AD_SHOW_STATUS_2: Int = 2

        private const val USER_CLICK_EVENT_0: Int = 0
        private const val USER_CLICK_EVENT_1: Int = 1
        private const val USER_CLICK_EVENT_2: Int = 2

        fun start(
            activity: Activity?,
            channelType: Int?,
            response: GetListResponse?,
            responses: GetListResponsePackBean? = null
        ) {
            val intent = Intent(activity, CustomPlayerPagerActivity::class.java)
            intent.putExtra(COMMON_CHANNEL_TYPE_TAG, channelType)
            if (null != response) {
                intent.putExtra(COMMON_PARAM_TAG, response)
            }
            if (null != responses) {
                intent.putExtra(COMMON_PARAM_LIST_TAG, responses)
            }
            activity?.startActivity(intent)
            activity?.overridePendingTransition(
                R.anim.left_in_activity,
                R.anim.left_out_activity
            )
        }
    }

    //view model
    private var viewModel: CustomPlayerPagerViewModel? = null

    //左侧菜单导航栏
    private var leftChapterAdapterCustom: CustomPlayerCatalogueChapterAdapter? = null

    //左侧菜单内容栏
    private var leftChapterContentAdapterCustom: CustomPlayerCatalogueChapterContentAdapter? = null

    //当前需要播放的实体
    var apiResponse: GetListResponse? = null
        set(value) {
            field = value
            Constant.response = value
        }

    //音频播放器
    private var audioPlayerManager: AudioPlayerManager? = null

    //推荐适配器
    private var otherRecAdapter: PlayerOtherRecAdapter? = null

    //播放列表
    private var apiResponseList: MutableList<GetListResponse>? = null

    //当前子章节区间下标 从0 开始
    private var currentChildPosition: Int = 0

    //当前子章节位于列表的下标 从0 开始
    private var currentChildInPlayerListPosition: Int = 0

    //从哪里进入到播放界面
    private var channelType: Int? = null

    //是否是第一次播放
    private var isFirstPlayer: Boolean? = null

    //当前广告展示的状态,0.未展示过,1.正在展示,2.展示结束(已展示过)
    private var onADShowStatus: Int = ON_AD_SHOW_STATUS_0

    //播放回调
    private var onPlayerEventListener: AudioPlayerManager.OnPlayerEventListener? = null

    private val currentList: MutableList<GetListResponse> = mutableListOf()

    //1.点击上一曲,2点击下一曲
    private var userClickEvent: Int = USER_CLICK_EVENT_0

    override fun initWidgets() {
        initEvent()
        initIntent()
        initDataResponse()
        initViewWidgets()
        initCurrentPlayerWidget(apiResponse)
        initIndexApi()
        initOtherRec()
    }

    override fun initWidgetsEvent() {
        initObserve()
        initViewsClick()
        initAdapterClick()
        initControlClick()
        initMediaPlayerEvent()
    }

    private fun initEvent() {
        EventBus.getDefault().register(this)
        ImmersionBar.with(this).init()
        Constant.isOpenPlayerPager = true
    }

    private fun initIntent() {
        if (intent.hasExtra(COMMON_CHANNEL_TYPE_TAG)) {
            channelType = intent.getIntExtra(COMMON_CHANNEL_TYPE_TAG, 0)
        }

        if (intent.hasExtra(COMMON_PARAM_TAG)) {
            apiResponse = intent.getParcelableExtra(COMMON_PARAM_TAG)
        }

        if (intent.hasExtra(COMMON_PARAM_LIST_TAG)) {
            vb?.playerPagerDrawerLayout?.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
            val bean = intent.getParcelableExtra<GetListResponsePackBean>(COMMON_PARAM_LIST_TAG)

            apiResponseList = bean?.datas
        }
    }

    private fun initDataResponse() {
        if (null == apiResponse) {
            if (!apiResponseList.isNullOrEmpty()) {
                val mediaPlayerItemId = AudioPlayerManager.getInstance().currentMediaItemId
                for (i in apiResponseList!!.indices) {
                    val item = apiResponseList!![i]
                    if (Constant.HEADER_LINES_TAG == channelType) {
                        if (item.album_id?.toString() == mediaPlayerItemId) {
                            apiResponse = item
                            currentChildInPlayerListPosition = i
                            break
                        }
                    } else {
                        if (item.program_id?.toString() == mediaPlayerItemId) {
                            apiResponse = item
                            currentChildInPlayerListPosition = i
                            break
                        }
                    }
                }

                if (null == apiResponse) {
                    apiResponse = apiResponseList!![0]
                }
            }
        } else if (!apiResponseList.isNullOrEmpty()) {
            for (i in apiResponseList!!.indices) {
                val item = apiResponseList!![i]
                if (Constant.HEADER_LINES_TAG == channelType) {
                    if (item.album_id ?: 0 == apiResponse?.album_id ?: 0) {
                        currentChildInPlayerListPosition = i
                        break
                    }
                } else {
                    if (item.program_id ?: 0 == apiResponse?.program_id ?: 0) {
                        currentChildInPlayerListPosition = i
                        break
                    }
                }
            }
        }
    }

    //初始化界面数据
    private fun initViewWidgets() {
        audioPlayerManager = AudioPlayerManager.getInstance()
        viewModel = CustomPlayerPagerViewModel(this)
        vb?.viewModel = viewModel
        vb?.let {
//            it.playerTopCollection.visibility =
//                if (Constant.HEADER_LINES_TAG == apiResponse?.album_type) {
//                    View.GONE
//                } else {
//                    View.VISIBLE
//                }

            val titleLayoutParams = it.playerTitleLayout.layoutParams
            if (titleLayoutParams is RelativeLayout.LayoutParams) {
                val topMargin = ScreenUtils.getStatusBarHeight(this)
                titleLayoutParams.topMargin = topMargin
                it.playerTitleLayout.layoutParams = titleLayoutParams
            }

            otherRecAdapter = PlayerOtherRecAdapter()
            it.playerRecRecycler.layoutManager = GridLayoutManager(this, 4)
            it.playerRecRecycler.isEnableEmptyView = false
            it.playerRecRecycler.adapter = otherRecAdapter

            it.playerContentControl.let { controlIt ->
                controlIt.player = audioPlayerManager?.mediaPlayer
                when (audioPlayerManager?.repeatMode) {
                    REPEAT_MODE_OFF -> {
                        controlIt.getPlayerModelView()
                            ?.setImageResource(R.mipmap.player_model_3_icon)
                    }
                    REPEAT_MODE_ONE -> {
                        controlIt.getPlayerModelView()
                            ?.setImageResource(R.mipmap.player_model_1_icon)
                    }
                    else -> {

                    }
                }
            }
        }

        //初始化倍速显示
        val currentSpeed = audioPlayerManager?.currentPlayerSpeed
        val speedArray = PlayerSpeedEnum.values()

        if (!speedArray.isNullOrEmpty()) {
            for (item in speedArray.iterator()) {
                if (currentSpeed == item.value) {
                    val tvShow = vb?.playerContentControl?.getPlayerSpeedView()
                    tvShow?.text = item.rewType
                    break
                }
            }
        }

        if (Constant.HEADER_LINES_TAG != apiResponse?.album_type) {
            initLeftMenu()
        } else {
            viewModel?.company = apiResponse?.company.toString()
        }
    }

    private fun initIndexApi(isNetWorkLoad: Boolean? = false) {
        when {
            Constant.HEADER_LINES_TAG != apiResponse?.album_type -> {
                if (isNetWorkLoad != true && !NetUtils.isNetworkConnected(this)) {
                    initAudioPlayerList(apiResponseList, currentChildInPlayerListPosition)

                    //离线获取本地数据
                    vb?.playerLeftMenuInclude?.let {
                        it.leftMenuChapterRecyclerView.visibility = View.GONE
                        leftChapterContentAdapterCustom?.setNewInstance(apiResponseList)
                    }
                    return
                }

                val event = GetProgramListParamEvent()
                event.album_id = apiResponse?.album_id
                event.page_size = 10
                event.page = currentChildPosition.inc()
                event.playerStatus = Constant.PLAYER_STATUS_TAG_1
                EventBus.getDefault().post(event)
            }
            else -> {
                MediaBrowserManager.instance.initPlayerList(
                    apiResponseList,
                    currentChildInPlayerListPosition
                )

                initAudioPlayerList(
                    apiResponseList,
                    currentChildInPlayerListPosition
                )
            }
        }
    }

    private fun initOtherRec() {
        if (!NetUtils.isNetworkConnected(this)) {
            return
        }

        viewModel?.setCommonTransformation()
    }

    private fun initObserve() {
        viewModel?.otherRecLiveData?.observe(this) {
            otherRecAdapter?.setNewInstance(it.data)
        }

//        viewModel?.keepLiveData?.observe(this) {
//            if (Constant.LIVE_DATA_STATUS_SUCCESS == it.status) {
//                val data = it.data
//                vb?.playerTopCollection?.isChecked = 1 == data?.status ?: 0
//
//                apiResponse?.keep_status = data?.status
//
//                val event = KeepResponseEvent()
//                event.channelType = apiResponse?.album_type
//                event.album_id = apiResponse?.album_id
//                event.status = data?.status
//                EventBus.getDefault().post(event)
//            }else {
//                vb?.playerTopCollection?.isChecked = 1 == apiResponse?.keep_status ?: 0
//            }
//        }
    }

    private fun initViewsClick() {
        //屏蔽左侧菜单点击事件,处理事件穿透
        vb?.playerLeftMenuInclude?.let {
            it.leftMenuRootView.setOnClickListener {}
            it.leftMenuEmptyView.setOnClickListener {}

            it.leftMenuRootView.setOnTouchListener { _, _ -> true }
            it.leftMenuEmptyView.setOnTouchListener { _, _ -> true }

            //右侧无数据时展示的empty view 需要重新拉取侧拉目录及推荐
            it.leftMenuEmptyViewInclude
                .commonEmptyRefreshView
                .setOnClickListener {
                    initIndexApi(true)
                    initOtherRec()
                }
        }

        vb?.playerLeftMenuInclude?.leftMenuDownload?.let { tvShowIt ->
            tvShowIt.setOnClickListener {
                if (leftChapterContentAdapterCustom?.data.isNullOrEmpty()) {
                    return@setOnClickListener
                }
                handleDownload(-1)
            }
        }
    }

    private fun initAdapterClick() {
        leftChapterAdapterCustom?.setOnItemClickListener { _, _, position ->
            leftChapterAdapterCustom?.selectorPosition = position

            currentChildPosition = position
            val event = GetProgramListParamEvent()
            event.album_id = apiResponse?.album_id
            event.page_size = 10
            event.page = currentChildPosition.inc()
            event.playerStatus = Constant.PLAYER_STATUS_TAG_0
            EventBus.getDefault().post(event)
        }

        leftChapterContentAdapterCustom?.setOnItemClickListener { adapter, _, position ->
            if (!adapter.data.isNullOrEmpty()) {
                onADShowStatus = ON_AD_SHOW_STATUS_0

                val item = adapter.data[position]
                currentChildInPlayerListPosition = position

                apiResponseList = leftChapterContentAdapterCustom?.data
                initAudioPlayerList(apiResponseList, position)

                if (item is GetListResponse) {
                    leftChapterContentAdapterCustom?.currentMediaItemId = item.program_au.toString()
                    initCurrentPlayerWidget(item)
                    initOtherRec()
                }

                if (position == apiResponseList?.size?.dec() &&
                    currentChildPosition < (leftChapterAdapterCustom?.data?.size ?: 0).dec()
                ) {
                    val event = GetProgramListParamEvent()
                    event.album_id = apiResponse?.album_id
                    event.page_size = 10
                    currentChildPosition = currentChildPosition.inc()
                    event.page = currentChildPosition.inc()
                    event.playerStatus = Constant.PLAYER_STATUS_TAG_3
                    EventBus.getDefault().post(event)
                }
            }
            closeLeftMenu()
        }

//        leftChapterContentAdapterCustom?.addChildClickViewIds(R.id.player_left_menu_download_status)
//        leftChapterContentAdapterCustom?.setOnItemChildClickListener { adapter, _, position ->
//            if (adapter.data.isNullOrEmpty()) {
//                return@setOnItemChildClickListener
//            }
//            handleDownload(position)
//        }

        otherRecAdapter?.setOnItemClickListener { _, _, position ->
            if (onADShowStatus == ON_AD_SHOW_STATUS_1) {
                return@setOnItemClickListener
            }

            onADShowStatus = ON_AD_SHOW_STATUS_0

            if (!otherRecAdapter?.data.isNullOrEmpty()) {
                val item = otherRecAdapter?.data!![position]
                leftChapterAdapterCustom?.selectorPosition = 0
                leftChapterContentAdapterCustom?.currentMediaItemId = ""

                apiResponse = item
                if (Constant.HEADER_LINES_TAG == apiResponse?.album_type) {
                    var selectorPosition = -1
                    if (!apiResponseList.isNullOrEmpty()) {
                        for (i in apiResponseList!!.indices) {
                            if (item.album_id?.toString() == audioPlayerManager?.currentMediaItemId) {
                                selectorPosition = i
                                break
                            }
                        }
                    }
                    initCurrentPlayerWidget(item)

                    var isExistencePosition = -1
                    if (-1 == selectorPosition) {
                        for (i in apiResponseList!!.indices) {
                            val responseItem = apiResponseList!![i]
                            if (Constant.HEADER_LINES_TAG == item.album_type) {
                                if (item.album_lid == responseItem.album_lid) {
                                    isExistencePosition = i
                                    break
                                }
                            } else {
                                if (item.program_lid == responseItem.program_lid) {
                                    isExistencePosition = i
                                    break
                                }
                            }
                        }

                        if (-1 == isExistencePosition) {
                            apiResponseList?.add(item)
                        }
                    }

                    Constant.responses = apiResponseList

                    viewModel?.setAudioPlayerList(
                        apiResponseList,
                        if (-1 == isExistencePosition) {
                            currentChildPosition = (apiResponseList?.size ?: 0).dec()
                            currentChildPosition
                        } else {
                            currentChildPosition = isExistencePosition
                            currentChildPosition
                        }
                    )

                    MediaBrowserManager.instance.initPlayerList(
                        apiResponseList,
                        currentChildInPlayerListPosition
                    )
                } else {
                    currentChildPosition = 0
                    currentChildInPlayerListPosition = 0
                    initIndexApi()
                }
            }
        }
    }

    private fun initControlClick() {
        vb?.playerContentControl?.let { controlIt ->
            controlIt.onControlViewClickListener =
                object : OnControlViewClickListener {
                    override fun onPlayerModelClick(view: ImageView?) {
                        if (audioPlayerManager?.repeatMode == REPEAT_MODE_OFF) {
                            audioPlayerManager?.repeatMode = REPEAT_MODE_ONE
                            view?.setImageResource(R.mipmap.player_model_1_icon)
                            ToastUtil.showToast("单曲循环")
                        } else {
                            audioPlayerManager?.repeatMode = REPEAT_MODE_OFF
                            view?.setImageResource(R.mipmap.player_model_3_icon)
                            ToastUtil.showToast("顺序播放")
                        }
                    }

                    override fun onPlayerListClick(view: ImageView?) {
                        viewModel?.openPlayerList()
                    }

                    override fun onPlayerSpeedClick(view: TextView?) {
                        viewModel?.setPlayerSpeed { speedIt ->
                            view?.text = speedIt
                        }
                    }

                    override fun onPlayerPrevClick(view: ImageButton?) {
                        if (0 >= currentChildPosition || 0 != currentChildInPlayerListPosition) {
                            return
                        }

                        userClickEvent = USER_CLICK_EVENT_1

                        val event = GetProgramListParamEvent()
                        event.album_id = apiResponse?.album_id
                        event.page_size = 10
                        event.page = currentChildPosition
                        event.playerStatus = Constant.PLAYER_STATUS_TAG_2
                        EventBus.getDefault().post(event)
                    }

                    override fun onPlayerNextClick(view: ImageButton?) {
                        userClickEvent = USER_CLICK_EVENT_2
                    }
                }
        }
    }

    private fun initMediaPlayerEvent() {
        onPlayerEventListener = object :
            AudioPlayerManager.OnPlayerEventListener {
            override fun onPositionDiscontinuity(
                oldPosition: Player.PositionInfo?,
                newPosition: Player.PositionInfo?,
                reason: Int
            ) {
                LoggerUtils.e("reason:${reason}")

                if (reason == Player.DISCONTINUITY_REASON_SEEK_ADJUSTMENT ||
                    reason == Player.DISCONTINUITY_REASON_AUTO_TRANSITION
                ) {
                    discontinuityEnable(newPosition)
                }
            }

            override fun onPlayerCurrentPosition(
                position: Long?,
                duration: Long,
                progress: Double?
            ) {
                initWidgetAd(progress)
            }
        }
        audioPlayerManager?.addPlayerEventListenerList(onPlayerEventListener)
    }

    private fun checkPlayerPositionAd(progress: Double?): Boolean {
        try {
            val showAdPosition =
                PreferencesUtils.getInt(PreferencesKey.CURRENT_CONTENT_SHOW_AD_POSITION)
            val showAdPositionValue = BigDecimal(showAdPosition)
                .divide(
                    BigDecimal("100"),
                    3,
                    BigDecimal.ROUND_HALF_UP
                )

            if (!checkCurrentPlayerData()) {
                return false
            }

            if (showAdPositionValue.toDouble() > (progress ?: 0.0)) {
                return false
            }

            if (onADShowStatus != ON_AD_SHOW_STATUS_0) {
                return false
            }

            if (Constant.HEADER_LINES_TAG == apiResponse?.album_type
                && apiResponse?.album_id == Constant.currentPlayerAdPositionId
            ) {
                return false
            }

            if (Constant.HEADER_LINES_TAG != apiResponse?.album_type
                && apiResponse?.program_id == Constant.currentPlayerAdPositionId
            ) {
                return false
            }

            if (Constant.HEADER_LINES_TAG == apiResponse?.album_type) {
                val linesAdInterval =
                    PreferencesUtils.getInt(PreferencesKey.TOUTIAO_PLAY_NUM_INTERVAL)
                if (0 != currentChildInPlayerListPosition
                    && currentChildInPlayerListPosition % linesAdInterval.inc() != 0
                ) {
                    return false
                }
            }

            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    private fun initWidgetAd(progress: Double?) {
        try {
            if (checkPlayerPositionAd(progress)) {
                if (Constant.HEADER_LINES_TAG == apiResponse?.album_type) {
                    viewModel?.dismissNewList()
                } else {
                    vb?.playerPagerDrawerLayout?.closeDrawers()
                }

                viewModel?.addWidgetsAd(apiResponse?.ad_json) { adManager, view, title ->
                    vb?.playerContentAdLayout?.removeAllViews()
                    if (null != view) {
                        //更新广告播放状态
                        onADShowStatus = ON_AD_SHOW_STATUS_1
                        vb?.playerContentControl?.isDisableEvent = true
                        audioPlayerManager?.pause()
                        adManager?.startPlayAd()
                        vb?.playerContentCover?.visibility = View.GONE
                        vb?.playerContentAdLayout?.let {
                            it.visibility = View.VISIBLE
                            it.addView(view)
                        }
                        vb?.playerContentChapter?.text = title
                    } else {
                        //标记全局该条内容已播放过广告
                        if (Constant.HEADER_LINES_TAG == apiResponse?.album_type) {
                            Constant.currentPlayerAdPositionId = apiResponse?.album_id
                        } else {
                            Constant.currentPlayerAdPositionId = apiResponse?.program_id
                        }

                        //广告结束,停止倒计时
                        CustomDownCountUtils.instance.cancel()

                        onADShowStatus = ON_AD_SHOW_STATUS_2
                        vb?.playerContentControl?.isDisableEvent = false
                        adManager?.destroy()
                        audioPlayerManager?.start()
                        vb?.playerContentCover?.visibility = View.VISIBLE
                        vb?.playerContentAdLayout?.visibility = View.GONE

                        if (null != Constant.response?.program_au) {
                            vb?.playerContentChapter?.text =
                                String.format(
                                    "第%s%s %s",
                                    apiResponse?.program_au ?: "",
                                    when (apiResponse?.album_type) {
                                        Constant.VOICE_NOVEL_TAG -> {
                                            "章"
                                        }
                                        Constant.VOICE_CURRICULUM_TAG -> {
                                            "讲"
                                        }
                                        else -> {
                                            ""
                                        }
                                    },
                                    apiResponse?.program_name ?: ""
                                )
                        } else {
                            vb?.playerContentChapter?.text = ""
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun discontinuityEnable(
        newPosition: Player.PositionInfo?
    ) {
        val newPos = newPosition?.periodIndex ?: 0
        if (0 > newPos || currentChildInPlayerListPosition == newPos) {
            return
        }

        try {
            if (USER_CLICK_EVENT_2 == userClickEvent
                && newPos >= 10
                && !currentList.isNullOrEmpty()
            ) {
                userClickEvent = USER_CLICK_EVENT_0

                currentChildInPlayerListPosition = newPos - 10
                initAudioPlayerList(currentList, currentChildInPlayerListPosition)

                apiResponseList?.clear()
                for (item in currentList.iterator()) {
                    apiResponseList?.add(item)
                }
                currentList.clear()
                currentChildPosition = currentChildPosition.inc()
                leftChapterContentAdapterCustom?.setNewInstance(apiResponseList)
            } else {
                currentChildInPlayerListPosition = newPos
            }

            onADShowStatus = ON_AD_SHOW_STATUS_0
            changerPlayerWidgets(currentChildInPlayerListPosition)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    //更新上一首下一首时章节的内容
    private fun changerPlayerWidgets(currentPosition: Int?) {
        var currentChatPlayerPosition = currentPosition
        if (currentChatPlayerPosition ?: 0 <= 0) {
            currentChatPlayerPosition = 0
        }

        val list = apiResponseList
        if (list.isNullOrEmpty()) {
            return
        }

        if (currentChatPlayerPosition ?: 0 >= list.size) {
            return
        }

        apiResponse = list[currentChatPlayerPosition ?: 0]
        initCurrentPlayerWidget(apiResponse)
        toMainActivityResponse(apiResponse)
    }

    private fun initCurrentPlayerWidget(item: GetListResponse?) {
        vb?.let {
            it.response = item
            apiResponse = item
            vb?.playerLeftMenuInclude?.response = apiResponse

            viewModel?.albumId = apiResponse?.album_id
            viewModel?.authorName = apiResponse?.author_name
            viewModel?.programId = apiResponse?.program_id
            viewModel?.albumType = apiResponse?.album_type

            //封面图
            if (!isDestroyed) {
                val url = item?.album_cover ?: ""
                it.playerContentCover.setRadius(20)

                GlideUtils.loadImage(
                    this,
                    url,
                    it.playerContentCover
                )

                Glide.with(this)
                    .load(url)
                    .apply(
                        RequestOptions.bitmapTransform(
                            BlurTransformation(
                                this,
                                radius = 25,
                                sampling = 10
                            )
                        )
                    )
                    .into(it.playerBackIvShow)

                //左侧菜单控件赋值
                it.playerLeftMenuInclude.let { includeIt ->
                    GlideUtils.loadRoundedCorners(
                        this,
                        apiResponse?.album_cover,
                        20,
                        includeIt.leftMenuCover
                    )

                    includeIt.leftMenuTotal.text = if (!NetUtils.isNetworkConnected(this)) {
                        if (apiResponse?.update_status == 2) {
                            "完结"
                        } else {
                            "连载中"
                        }
                    } else {
                        String.format(
                            "%s %s%s讲", if (apiResponse?.update_status == 2) {
                                "完结"
                            } else {
                                "连载中"
                            },
                            if (apiResponse?.update_status == 2) {
                                "共"
                            } else {
                                "已连载"
                            },
                            apiResponse?.program_num ?: 0
                        )
                    }
                }
            }

            if (null != item?.program_au) {
                it.playerContentChapter.text =
                    String.format(
                        "第%s%s %s",
                        item.program_au ?: "",
                        when (apiResponse?.album_type) {
                            Constant.VOICE_NOVEL_TAG -> {
                                "章"
                            }
                            Constant.VOICE_CURRICULUM_TAG -> {
                                "讲"
                            }
                            else -> {
                                ""
                            }
                        },
                        item.program_name ?: ""
                    )
            } else {
                it.playerContentChapter.text = ""
            }
//            it.playerTopCollection.isChecked = item?.keep_status ?: 0 == 1
        }
    }

    private fun checkCurrentPlayerData(): Boolean {
        val currentPlayerGroupId = CustomPreferencesUtils.fetchCurrentPlayerGroupId()

        if (0 == currentPlayerGroupId) {
            return false
        }

        if (apiResponse?.album_id != currentPlayerGroupId) {
            return false
        }

        val mediaItemId = audioPlayerManager?.currentMediaItemId
        if (TextUtils.isEmpty(mediaItemId)) {
            return false
        }

        if (Constant.HEADER_LINES_TAG != apiResponse?.album_type) {
            if (null != apiResponse?.program_id &&
                mediaItemId != apiResponse?.program_id.toString()
            ) {
                return false
            } else if (null != Constant.response?.program_id &&
                mediaItemId != Constant.response?.program_id.toString()
            ) {
                return false
            }
        }

        return true
    }

    /**
     * 处理播放列表
     *
     * @param list 需要播放的列表
     *
     * @param position 从第几个开始播放
     */
    private fun initAudioPlayerList(list: MutableList<GetListResponse>?, position: Int?) {
        if (!checkCurrentPlayerData()) {
            CustomDownCountUtils.instance.cancel()
        }

        var currentPosition = position
        if (isFirstPlayer != true) {
            isFirstPlayer = true

            //判断是否是同一作品,如果是则继续播放,
            //不是则判断是否有播放列表,没有则清空当前播放列表
            if (checkCurrentPlayerData()) {
                initCurrentPlayerWidget(apiResponse)
                return
            } else {
                audioPlayerManager?.stop()
                if (list.isNullOrEmpty()) {
                    audioPlayerManager?.mediaPlayer?.clearMediaItems()
                } else {
                    initDataResponse()
                    currentPosition = currentChildInPlayerListPosition
                }
            }
        }

        if (list.isNullOrEmpty()) {
            return
        }

        viewModel?.setAudioPlayerList(list, currentPosition)

        val response = list[position ?: 0]
        initCurrentPlayerWidget(response)
        toMainActivityResponse(response)
    }

    //通知main activity 刷新当前正在播放的response
    private fun toMainActivityResponse(response: GetListResponse?) {
        CustomPreferencesUtils.putCurrentPlayerGroupId(response?.album_id)
        EventBus.getDefault().post(response)
    }

    //初始化左侧侧拉菜单
    @SuppressLint("ClickableViewAccessibility", "RtlHardcoded")
    private fun initLeftMenu() {
        //启用左侧菜单
        vb?.playerPagerDrawerLayout?.setDrawerLockMode(
            DrawerLayout.LOCK_MODE_LOCKED_CLOSED,
            Gravity.LEFT
        )

        vb?.playerLeftMenuInclude?.let {
            it.leftMenuChapterRecyclerView.layoutManager =
                LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
            leftChapterAdapterCustom = CustomPlayerCatalogueChapterAdapter()
            it.leftMenuChapterRecyclerView.isEnableEmptyView = false
            it.leftMenuChapterRecyclerView.adapter = leftChapterAdapterCustom

            it.leftMenuChapterContentRecycler.layoutManager = LinearLayoutManager(this)
            leftChapterContentAdapterCustom = CustomPlayerCatalogueChapterContentAdapter()
            it.leftMenuChapterContentRecycler.adapter = leftChapterContentAdapterCustom

            it.leftMenuChapterContentRecycler.setOnRefreshClickListener {
                currentChildPosition = 0
                val event = GetProgramListParamEvent()
                event.album_id = apiResponse?.album_id
                event.page_size = 10
                event.page = currentChildPosition.inc()
                event.playerStatus = Constant.PLAYER_STATUS_TAG_0
                EventBus.getDefault().post(event)
            }
        }
    }

    @SuppressLint("RtlHardcoded")
    fun openPlayerListWidgets() {
        if (Constant.HEADER_LINES_TAG == apiResponse?.album_type) {
            viewModel?.openNewsList(apiResponseList, {
                onADShowStatus = ON_AD_SHOW_STATUS_0

                initAudioPlayerList(apiResponseList, it)
                if (!apiResponseList.isNullOrEmpty()) {
                    apiResponse = apiResponseList!![it]
                    initCurrentPlayerWidget(apiResponse)
                    initOtherRec()
                }
            }, {
                apiResponseList = it

                if (!apiResponseList.isNullOrEmpty()) {
                    val mediaItemId = audioPlayerManager?.currentMediaItemId

                    for (item in apiResponseList!!.iterator()) {
                        if (mediaItemId == item.album_id?.toString()) {
                            apiResponse = item
                            initCurrentPlayerWidget(apiResponse)
                            break
                        }
                    }
                }

                val bean = GetListResponsePackBean()
                bean.datas = apiResponseList
                EventBus.getDefault().post(bean)
            })
        } else {
            vb?.playerPagerDrawerLayout?.openDrawer(Gravity.LEFT)

            //设置左侧当前正在播放的icon
            leftChapterContentAdapterCustom?.currentMediaItemId = audioPlayerManager?.currentMediaItemId

            if (currentChildInPlayerListPosition >= 10 && !currentList.isNullOrEmpty()) {
                leftChapterAdapterCustom?.selectorPosition = currentChildPosition.inc()
                leftChapterContentAdapterCustom?.setNewInstance(currentList)
            } else {
                leftChapterAdapterCustom?.selectorPosition = currentChildPosition
            }
        }
    }

    private fun closeLeftMenu() {
        vb?.playerPagerDrawerLayout?.closeDrawers()
    }

    override fun initStatusBar(): Boolean {
        return false
    }

    override fun startActivityForResult(intent: Intent, requestCode: Int, options: Bundle?) {
        super.startActivityForResult(intent, requestCode, options)
        overridePendingTransition(R.anim.actionsheet_in, R.anim.actionsheet_out)
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.actionsheet_in, R.anim.actionsheet_out)
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

    override fun onDestroy() {
        super.onDestroy()
        try {
            if (onPlayerEventListener != null) {
                audioPlayerManager?.removePlayerEventListener(onPlayerEventListener)
            }

//            //如果退出当前界面时正在播放广告,则停止广告,继续播放内容音频
//            if (onADShowStatus == ON_AD_SHOW_STATUS_1) {
//                audioPlayerManager?.start()
//            }

            //释放内存
//            QCiVoiceSdk.get().onDestroy()

            vb?.playerContentAdLayout?.removeAllViews()
            vb?.playerContentAdLayout?.visibility = View.GONE
            vb?.playerContentCover?.visibility = View.VISIBLE
            EventBus.getDefault().unregister(this)
            Constant.isOpenPlayerPager = false
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    //获取播放列表
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: GetProgramListResponseEvent?) {
        initPlayerEvent(event)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: SynchronizationCurrentChildPositionEvent?) {
        currentChildPosition = event?.currentChildPosition ?: 0
    }

    private fun initPlayerEvent(event: GetProgramListResponseEvent?) {
        if (event?.data?.section_number_list.isNullOrEmpty()) {
            vb?.playerLeftMenuInclude?.let {
                it.leftMenuEmptyView.visibility = View.VISIBLE
                it.leftMenuRootView.visibility = View.GONE
            }
            return
        } else {
            vb?.playerLeftMenuInclude?.let {
                it.leftMenuEmptyView.visibility = View.GONE
                it.leftMenuRootView.visibility = View.VISIBLE
            }
        }

        vb?.playerLeftMenuInclude?.leftMenuChapterRecyclerView?.visibility = View.VISIBLE
        leftChapterAdapterCustom?.setNewInstance(event?.data?.section_number_list)

        //0.默认,1.自动播放,2.上一页,3.滑动到特定位置需要自动加载下一页
        when (event?.playerStatus) {
            Constant.PLAYER_STATUS_TAG_0 -> {
                leftChapterContentAdapterCustom?.setNewInstance(event.data?.data)
            }
            Constant.PLAYER_STATUS_TAG_1 -> {
                apiResponseList = event.data?.data
                initAudioPlayerList(apiResponseList, currentChildInPlayerListPosition)
                leftChapterContentAdapterCustom?.setNewInstance(apiResponseList)
            }
            Constant.PLAYER_STATUS_TAG_2 -> {
                val list = event.data?.data
                if (!list.isNullOrEmpty()) {
                    //保存当前列表
                    currentList.clear()
                    if (!apiResponseList.isNullOrEmpty()) {
                        for (item in apiResponseList!!.iterator()) {
                            currentList.add(item)
                        }
                    }

                    apiResponseList?.clear()

                    for (item in list.iterator()) {
                        apiResponseList?.add(item)
                    }

                    //把当前播放列表插入
                    apiResponseList?.addAll(currentList)
                    currentChildInPlayerListPosition = list.size - 1
                    initAudioPlayerList(apiResponseList, currentChildInPlayerListPosition)
                    leftChapterContentAdapterCustom?.setNewInstance(list)
                    currentChildPosition = currentChildPosition.dec()
                    leftChapterAdapterCustom?.selectorPosition = currentChildPosition
                }
            }
            Constant.PLAYER_STATUS_TAG_3 -> {
                val list = event.data?.data
                if (!list.isNullOrEmpty()) {
//                    apiResponseList?.addAll(list)
                    leftChapterContentAdapterCustom?.setNewInstance(list)
                    leftChapterAdapterCustom?.selectorPosition = currentChildPosition.dec()
                }
            }
        }
    }

    /**
     * 下载逻辑：非wifi提醒
     */
    private fun handleDownload(position: Int) {
        val permissionList = PermissionUtil.checkMorePermissions(
            this, arrayOf(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        )

        if (!permissionList.isNullOrEmpty()) {
            ToastUtil.showToast("您未开启读写权限哦！")
            return
        }

        if (!NetUtils.isNetworkConnected(this)) {
            ToastUtil.showToast("操作失败")
            return
        }

        if (NetUtils.isWifiConnect(this) || DownloadService.authorized) {
            if (position == -1) {
                downloadAll()
            } else {
                downloadSingle(position, true)
            }
        } else {
            DialogUtils.showCommonDialog(
                this,
                "",
                "当前非WIFI网络，请问是否确认下载？",
                true,
                object : OnDialogCallBackListener {
                    override fun onConfirm(content: String?) {
                        DownloadService.authorized = true
                        if (position == -1) {
                            downloadAll()
                        } else {
                            downloadSingle(position, true)
                        }
                    }

                    override fun onCancel() {}
                })
        }
    }

    /**
     * 下载全部
     */
    private fun downloadAll() {
        val list = leftChapterContentAdapterCustom?.data
        if (!list.isNullOrEmpty()) {
            var hint = ""
            var returnHint = ""
            for (position in list.indices) {
                returnHint = downloadSingle(position, false)
                if (returnHint.isNotEmpty()) {
                    hint = "已加入下载队列"
                }
            }
            ToastUtil.showToast(if (hint.isEmpty()) "当前没有可下载的内容" else hint)
        }
    }

    /**
     * 下载单个
     */
    private fun downloadSingle(position: Int, isSingle: Boolean): String {
        var hint = ""
        val list = leftChapterContentAdapterCustom?.data
        if (!list.isNullOrEmpty()) {
            val item = list[position]
            if (item.download_status == Constant.COMMON_DOWNLOAD_STATUS_1) {
                val urls = arrayListOf(item.mp3_url.toString(), item.album_cover.toString())
                val fileNames = arrayListOf("voice.mp3", "cover.jpg")
                val alias = item.program_id.toString()
                DownloadService.downloadCallBack = this
                DownloadService.contentMap?.put(alias, item)
                DownloadService.start(urls, fileNames, alias)
                //刷新下载图标显示状态
                item.download_status = Constant.COMMON_DOWNLOAD_STATUS_2
                leftChapterContentAdapterCustom?.notifyItemChanged(position)

                if (isSingle) {
                    ToastUtil.showToast("正在下载")
                }
                hint = "已加入下载队列"
            }
        }
        return hint
    }

    /**
     * 下载成功回调
     */
    override fun downloadComplete(alias: String?) {
        val list = leftChapterContentAdapterCustom?.data
        if (!list.isNullOrEmpty()) {
            for (i in list.indices) {
                if (list[i].program_id.toString() == alias) {
                    list[i].download_status = Constant.COMMON_DOWNLOAD_STATUS_3
                    leftChapterContentAdapterCustom?.notifyItemChanged(i)
                    break
                }

            }
        }
    }

    /**
     * 下载失败回调
     */
    override fun downloadFail(alias: String?) {
        val list = leftChapterContentAdapterCustom?.data
        if (!list.isNullOrEmpty()) {
            for (i in list.indices) {
                if (list[i].program_id.toString() == alias) {
                    //刷新下载图标显示状态
                    list[i].download_status = Constant.COMMON_DOWNLOAD_STATUS_1
                    leftChapterContentAdapterCustom?.notifyItemChanged(i)
                    break
                }
            }
        }
    }
}