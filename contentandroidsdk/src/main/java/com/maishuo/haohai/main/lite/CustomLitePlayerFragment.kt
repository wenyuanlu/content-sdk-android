package com.maishuo.haohai.main.lite

import android.content.DialogInterface
import android.text.TextUtils
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.corpize.sdk.ivoice.admanager.QcAdManager
import com.maishuo.haohai.R
import com.maishuo.haohai.api.bean.GetListResponsePackBean
import com.maishuo.haohai.api.response.GetListResponse
import com.maishuo.haohai.api.response.RecommendListResponse
import com.maishuo.haohai.api.retrofit.ApiService
import com.maishuo.haohai.audio.AudioPlayerManager
import com.maishuo.haohai.common.Constant
import com.maishuo.haohai.databinding.FragmentCustomLitePlayerLayoutBinding
import com.maishuo.haohai.main.adapter.CustomLitePlayerRecommendAdapter
import com.maishuo.haohai.main.event.CatalogueClickEvent
import com.maishuo.haohai.main.event.GetProgramListParamEvent
import com.maishuo.haohai.main.event.GetProgramListResponseEvent
import com.maishuo.haohai.main.event.RecommendClickEvent
import com.maishuo.haohai.main.viewmodel.CustomLitePlayerViewModel
import com.maishuo.haohai.mediabrowser.MediaBrowserManager
import com.maishuo.haohai.utils.CustomDownCountUtils
import com.maishuo.haohai.utils.Utils
import com.maishuo.haohai.widgets.CustomNestedScrollView
import com.maishuo.haohai.widgets.control.CustomPlayerControlView
import com.maishuo.haohai.widgets.control.OnControlViewClickListener
import com.qichuang.commonlibs.basic.BaseDialogFragment
import com.qichuang.commonlibs.utils.CustomPreferencesUtils
import com.qichuang.commonlibs.utils.GlideUtils
import com.qichuang.commonlibs.utils.ScreenUtils
import com.qichuang.retrofitlibs.retrofit.CommonObserver
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * author : xpSun
 * date : 2022/3/14
 * description :
 */
class CustomLitePlayerFragment @JvmOverloads constructor(appCompatActivity: AppCompatActivity? = null) :
    BaseDialogFragment(appCompatActivity) {

    private var vb: FragmentCustomLitePlayerLayoutBinding? = null
    private var viewModel: CustomLitePlayerViewModel? = null
    private var recommendAdapter: CustomLitePlayerRecommendAdapter? = null
    private var isHide: Boolean? = null

    private var apiResponse: GetListResponse? = null
        set(value) {
            field = value
            Constant.response = value
            initCurrentPlayerWidget(value)
            initRecommendList()
        }

    private var getProgramListParamEvent: GetProgramListResponseEvent? = null
        set(value) {
            field = value
            viewModel?.getProgramListParamEvent = value
        }

    //播放列表
    private var apiResponseList: MutableList<GetListResponse>? = null
        set(value) {
            field = value
            Constant.responses = value
        }

    //音频播放器
    private var audioPlayerManager: AudioPlayerManager? = null

    //从哪里进入到播放界面
    private var channelType: Int? = null

    //是否是第一次播放
    private var isFirstPlayer: Boolean? = null

    private val currentList: MutableList<GetListResponse> = mutableListOf()

    override fun fetchRootView(): View? {
        vb =
            FragmentCustomLitePlayerLayoutBinding.inflate(LayoutInflater.from(appCompatActivity))
        return vb?.root
    }

    override fun initWidgets() {
        initViewWidgets()
    }

    private fun initEvent() {
        Constant.isOpenPlayerPager = true
    }

    private fun initViewWidgets() {
        EventBus.getDefault().register(this)

        audioPlayerManager = AudioPlayerManager.getInstance()
        viewModel = CustomLitePlayerViewModel(this)
        vb?.viewmodel = viewModel
        recommendAdapter = CustomLitePlayerRecommendAdapter()

        onDismissListener = DialogInterface.OnDismissListener {

        }
    }

    override fun initWidgetsEvent() {
        vb?.viewLitePlayerIncludeLayout?.let {
            vb?.customLitePlayerControlView?.let { controlIt ->
                controlIt.onControlViewClickListener = object : OnControlViewClickListener {
                    override fun onPlayerListClick(view: ImageView?) {
                        viewModel?.openCatalogue()
                    }
                }

                controlIt.player = audioPlayerManager?.mediaPlayer
            }

            it.customLitePlayerNestedScrollView.setOnScrollChangeListener(
                CustomNestedScrollView.OnScrollChangeListener { _, _, t, _, _ ->
                    val value = it.customLitePlayerNestedScrollView.isScrollBottom
                    when {
                        0 == t -> {
                            vb?.customLitePlayerTitleLayout?.customLitePlayerTitleWorksName?.visibility =
                                View.GONE
                            vb?.customLitePlayerControlView?.apply {
                                visibility = View.VISIBLE
                                player = audioPlayerManager?.mediaPlayer
                                //处理上下滑动时进度条异常的问题
                                if (audioPlayerManager?.isPlaying == true) {
                                    audioPlayerManager?.pause()
                                    audioPlayerManager?.start()
                                }

                                val mLayoutParams = layoutParams
                                if (mLayoutParams is RelativeLayout.LayoutParams) {
                                    mLayoutParams.topMargin = Utils.dpToPx(400)
                                    mLayoutParams.leftMargin = Utils.dpToPx(20)
                                    mLayoutParams.rightMargin = Utils.dpToPx(20)
                                }
                                layoutParams = mLayoutParams
                                setPlayerStyleModel(CustomPlayerControlView.PLAYER_STYLE_MODULE_0)
                            }

                            vb?.customLitePlayerLiteControlBack?.visibility = View.GONE
                            vb?.viewLitePlayerTopControlFrameLayout?.visibility = View.GONE

                            vb?.customLitePlayerTitleLayout?.customLitePlayerTitleWorksName?.visibility =
                                View.GONE
                        }
                        value -> {
                            vb?.customLitePlayerTitleLayout?.customLitePlayerTitleWorksName?.visibility =
                                View.VISIBLE
                            vb?.customLitePlayerControlView?.apply {
                                visibility = View.VISIBLE
                                player = audioPlayerManager?.mediaPlayer
                                //处理上下滑动时进度条异常的问题
                                if (audioPlayerManager?.isPlaying == true) {
                                    audioPlayerManager?.pause()
                                    audioPlayerManager?.start()
                                }

                                val mLayoutParams = layoutParams
                                if (mLayoutParams is RelativeLayout.LayoutParams) {
                                    mLayoutParams.topMargin = Utils.dpToPx(40)
                                    mLayoutParams.leftMargin = 0
                                    mLayoutParams.rightMargin = 0
                                }
                                layoutParams = mLayoutParams
                                setPlayerStyleModel(CustomPlayerControlView.PLAYER_STYLE_MODULE_1)
                            }

                            vb?.customLitePlayerLiteControlBack?.visibility = View.VISIBLE
                            vb?.viewLitePlayerTopControlFrameLayout?.visibility = View.VISIBLE

                            vb?.customLitePlayerTitleLayout?.customLitePlayerTitleWorksName?.visibility =
                                View.VISIBLE
                        }
                        else -> {
                            vb?.customLitePlayerTitleLayout?.customLitePlayerTitleWorksName?.visibility =
                                View.GONE
                            vb?.customLitePlayerControlView?.apply {
                                visibility = View.GONE
                                player = audioPlayerManager?.mediaPlayer
                            }

                            vb?.customLitePlayerLiteControlBack?.visibility = View.GONE
                            vb?.viewLitePlayerTopControlFrameLayout?.visibility = View.GONE

                            vb?.customLitePlayerTitleLayout?.customLitePlayerTitleWorksName?.visibility =
                                View.GONE
                        }
                    }
                })

            val screenHeight =
                ScreenUtils.getRealyScreenHeight(appCompatActivity) - ScreenUtils.getStatusBarHeight(
                    appCompatActivity
                )
            it.customLitePlayerNestedChildView.setPadding(0, (screenHeight / 1.8).toInt(), 0, 0)

            it.customLitePlayerNestedRecyclerView.layoutManager =
                LinearLayoutManager(appCompatActivity)
            it.customLitePlayerNestedRecyclerView.adapter = recommendAdapter
        }

        recommendAdapter?.setOnChildClickListener {
            initIndexApi(it)
        }

        recommendAdapter?.addChildClickViewIds(R.id.lite_player_recommend_item_more)
        recommendAdapter?.setOnItemChildClickListener { _, view, position ->
            val item = recommendAdapter?.getItem(position)
            if (view.id == R.id.lite_player_recommend_item_more) {
                val bean = GetListResponsePackBean()
                bean.datas = item?.list
                CustomLiteOtherRecommendActivity.start(activity, bean)
            }
        }

        audioPlayerManager?.addPlayerEventListenerList(object :
            AudioPlayerManager.OnPlayerEventListener {
            override fun onPlayerCurrentPosition(
                position: Long?,
                duration: Long?,
                progress: Double?
            ) {
                if (viewModel?.checkPlayerPositionAd(progress) == true) {
                    val adid = Constant.COMMON_ADID
                    viewModel?.addConnectedNetWorkAd(adid) { adManager: QcAdManager?,
                                                             view: View?,
                                                             title: String? ->

                        vb?.customLitePlayerWorkAdLayout?.apply {
                            removeAllViews()

                            if (null != view) {
                                visibility = View.VISIBLE
                                vb?.customLitePlayerWorksName?.text = title ?: ""
                                vb?.customLitePlayerTitleLayout?.customLitePlayerTitleWorksName?.text =
                                    title ?: ""
                                adManager?.startPlayAd()
                            } else {
                                visibility = View.GONE
                                vb?.customLitePlayerWorksName?.text =
                                    Constant.response?.album_name ?: ""
                                vb?.customLitePlayerTitleLayout?.customLitePlayerTitleWorksName?.text =
                                    Constant.response?.album_name ?: ""
                            }
                        }
                    }
                }
            }
        })

        vb?.customLitePlayerControlView?.onControlViewClickListener =
            object : OnControlViewClickListener {
                override fun onPlayerSpeedClick(view: TextView?) {
                    viewModel?.setPlayerSpeed { speedIt ->
                        view?.text = speedIt
                    }
                }
            }
    }

    private fun initCurrentPlayerWidget(response: GetListResponse?) {
        if (null == appCompatActivity) {
            return
        }

        vb?.response = response

        viewModel?.albumId = apiResponse?.album_id
        viewModel?.authorName = apiResponse?.author_name
        viewModel?.programId = apiResponse?.program_id
        viewModel?.albumType = apiResponse?.album_type

        val url = response?.album_cover ?: ""
        vb?.let {
            GlideUtils.loadImage(
                appCompatActivity!!,
                url,
                it.customLitePlayerWorkBigCover
            )

            GlideUtils.initImageForBitmapTransform(
                appCompatActivity,
                url,
                it.customLitePlayerCoverBackground
            )
            GlideUtils.initImageForBitmapTransform(
                appCompatActivity,
                url,
                it.customLitePlayerLiteControlBack
            )
        }

        vb?.customLitePlayerControlView?.let {
            it.loadLiteTopControlImage(appCompatActivity, url)
        }
    }

    fun initIndexApi(response: GetListResponse?) {
        apiResponse = response
        initIndexApi()
    }

    private fun initIndexApi() {
        when {
            Constant.HEADER_LINES_TAG != apiResponse?.album_type -> {
                val event = GetProgramListParamEvent()
                event.album_id = apiResponse?.album_id
                event.page_size = 10
                event.page = (Constant.currentChildPosition ?: 0).inc()
                event.playerStatus = Constant.PLAYER_STATUS_TAG_1
                EventBus.getDefault().post(event)
            }
            else -> {
                MediaBrowserManager.instance.initPlayerList(
                    apiResponseList,
                    Constant.currentChildInPlayerListPosition
                )

                initAudioPlayerList(
                    apiResponseList,
                    Constant.currentChildInPlayerListPosition
                )
            }
        }
    }

    fun start() {
        if (isHide == true) {
            isHide = false
            dialog?.show()
        } else {
            if (!isAdded) {
                showNowDialog()
            }
        }
    }

    override fun onStart() {
        super.onStart()

        if (isHide == true) {
            onStop()
            isHide = false
        }

        initEvent()
    }

    override fun onResume() {
        super.onResume()
        initWidgetSize(
            Gravity.CENTER,
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
    }

    fun hideFragment() {
        isHide = true
        onStop()
    }

    fun scrollToBottom() {
        vb?.viewLitePlayerIncludeLayout?.let {
            it.customLitePlayerNestedScrollView.smoothScrollTo(0, 0)
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
                            Constant.currentChildInPlayerListPosition = i
                            break
                        }
                    } else {
                        if (item.program_id?.toString() == mediaPlayerItemId) {
                            apiResponse = item
                            Constant.currentChildInPlayerListPosition = i
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
                        Constant.currentChildInPlayerListPosition = i
                        break
                    }
                } else {
                    if (item.program_id ?: 0 == apiResponse?.program_id ?: 0) {
                        Constant.currentChildInPlayerListPosition = i
                        break
                    }
                }
            }
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
                    currentPosition = Constant.currentChildInPlayerListPosition
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

    fun initPlayerEvent(event: GetProgramListResponseEvent?) {
//        if (event?.data?.section_number_list.isNullOrEmpty()) {
//            vb?.playerLeftMenuInclude?.let {
//                it.leftMenuEmptyView.visibility = View.VISIBLE
//                it.leftMenuRootView.visibility = View.GONE
//            }
//            return
//        } else {
//            vb?.playerLeftMenuInclude?.let {
//                it.leftMenuEmptyView.visibility = View.GONE
//                it.leftMenuRootView.visibility = View.VISIBLE
//            }
//        }

//        vb?.playerLeftMenuInclude?.leftMenuChapterRecyclerView?.visibility = View.VISIBLE
//        leftChapterAdapter?.setNewInstance(event?.data?.section_number_list)

        getProgramListParamEvent = event

        //0.默认,1.自动播放,2.上一页,3.滑动到特定位置需要自动加载下一页
        when (event?.playerStatus) {
            Constant.PLAYER_STATUS_TAG_0 -> {
//                leftChapterContentAdapter?.setNewInstance(event.data?.data)
            }
            Constant.PLAYER_STATUS_TAG_1 -> {
                apiResponseList = event.data?.data
                initAudioPlayerList(apiResponseList, Constant.currentChildInPlayerListPosition)
//                leftChapterContentAdapter?.setNewInstance(apiResponseList)
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
                    Constant.currentChildInPlayerListPosition = list.size - 1
                    initAudioPlayerList(apiResponseList, Constant.currentChildInPlayerListPosition)
//                    leftChapterContentAdapter?.setNewInstance(list)
                    Constant.currentChildPosition = Constant.currentChildPosition?.dec()
//                    leftChapterAdapter?.selectorPosition = currentChildPosition
                }
            }
            Constant.PLAYER_STATUS_TAG_3 -> {
                val list = event.data?.data
                if (!list.isNullOrEmpty()) {
//                    apiResponseList?.addAll(list)
//                    leftChapterContentAdapter?.setNewInstance(list)
//                    leftChapterAdapter?.selectorPosition = currentChildPosition.dec()
                }
            }
        }
    }

    private fun initRecommendList() {
        ApiService.instance.recommendList(
            apiResponse?.album_type,
            apiResponse?.album_lid
        )
            .subscribe(object : CommonObserver<MutableList<RecommendListResponse>>() {
                override fun onResponseSuccess(response: MutableList<RecommendListResponse>?) {
                    recommendAdapter?.setNewInstance(response)
                }
            })
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: CatalogueClickEvent?) {
        Constant.onADShowStatus = Constant.ON_AD_SHOW_STATUS_0

        val item = event?.item
        val position = event?.position ?: 0
        Constant.currentChildInPlayerListPosition = position
        apiResponseList = event?.responses
        initAudioPlayerList(apiResponseList, event?.position)

        initCurrentPlayerWidget(item)
//        initOtherRec()

        if (position == apiResponseList?.size?.dec() &&
            Constant.currentChildPosition ?: 0 < (apiResponseList?.size ?: 0).dec()
        ) {
            val event = GetProgramListParamEvent()
            event.album_id = apiResponse?.album_id
            event.page_size = 10
            Constant.currentChildPosition = (Constant.currentChildPosition ?: 0).inc()
            event.page = (Constant.currentChildPosition ?: 0).inc()
            event.playerStatus = Constant.PLAYER_STATUS_TAG_3
            EventBus.getDefault().post(event)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: RecommendClickEvent?) {
        val item = event?.item
        initIndexApi(item)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        EventBus.getDefault().unregister(this)
    }
}