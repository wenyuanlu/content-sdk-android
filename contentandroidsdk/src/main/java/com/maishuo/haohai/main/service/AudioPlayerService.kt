package com.maishuo.haohai.main.service

import android.content.Context
import android.content.Intent
import android.media.MediaMetadata
import android.media.browse.MediaBrowser
import android.media.session.MediaSession
import android.media.session.PlaybackState
import android.os.Bundle
import android.service.media.MediaBrowserService
import android.text.TextUtils
import com.google.android.exoplayer2.Player
import com.maishuo.haohai.api.response.GetProgramListResponse
import com.maishuo.haohai.api.retrofit.ApiService
import com.maishuo.haohai.audio.AudioPlayerManager
import com.maishuo.haohai.common.Constant
import com.maishuo.haohai.common.ContentAndroidSDK
import com.maishuo.haohai.common.PlayerContentExposureEnum
import com.maishuo.haohai.main.event.GetProgramListResponseEvent
import com.maishuo.haohai.mediabrowser.MediaBrowserManager
import com.maishuo.haohai.mediabrowser.MediaSessionCallbackManager
import com.maishuo.haohai.notification.MediaNotificationManager
import com.qichuang.commonlibs.basic.CustomBasicApplication
import com.qichuang.commonlibs.common.PreferencesKey
import com.qichuang.commonlibs.utils.LoggerUtils
import com.qichuang.commonlibs.utils.PreferencesUtils
import com.qichuang.retrofitlibs.retrofit.CommonObserver
import com.qichuang.roomlib.RoomManager
import org.greenrobot.eventbus.EventBus
import retrofit2.Response


/**
 * author : xpSun
 * date : 12/6/21
 * description :
 */
class AudioPlayerService : MediaBrowserService() {

    companion object {
        fun start(
            context: Context?,
            album_id: Int?,
            page_size: Int?,
            page: Int?,
            playerStatus: Int?
        ) {
            val intent = Intent(context, AudioPlayerService::class.java)
            intent.putExtra(ALBUM_ID_TAG, album_id)
            intent.putExtra(PAGE_SIZE_TAG, page_size)
            intent.putExtra(PAGE_TAG, page)
            intent.putExtra(PLAYER_STATUS_TAG, playerStatus)
            context?.startService(intent)
        }

        fun stop(context: Context?) {
            val intent = Intent(context, AudioPlayerService::class.java)
            context?.stopService(intent)
        }

        private const val ALBUM_ID_TAG: String = "album_id_tag"
        private const val PAGE_SIZE_TAG: String = "page_size_tag"
        private const val PAGE_TAG: String = "PAGE_TAG"
        private const val PLAYER_STATUS_TAG: String = "player_status_tag"

        private const val MEDIA_ID_EMPTY_ROOT = "__EMPTY_ROOT__"
        private const val MEDIA_ID_ROOT = "__ROOT__"

        private const val TAG = "AudioPlayerService"

        //action
        const val ACTION_CMD = "com.haohai.content.sdk.music.ACTION_CMD"

        // key
        const val CMD_NAME = "CMD_NAME"
        const val CMD_PAUSE = "CMD_PAUSE"
        const val CMD_STOP_CASTING = "CMD_STOP_CASTING"
    }

    private var albumId: Int? = null
    private var pageSize: Int? = null
    private var page: Int? = null
    private var playerStatus: Int? = null

    private var mediaSession: MediaSession? = null
    private var sessionCallBack: MediaSessionCallbackManager? = null
    private var mediaNotificationManager: MediaNotificationManager? = null

    private var audioPlayerManager: AudioPlayerManager? = null

    private var periodIndex: Int? = null

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

    override fun onCreate() {
        super.onCreate()

        mediaSession = MediaSession(this, TAG)
        sessionCallBack = MediaSessionCallbackManager(object :
            MediaSessionCallbackManager.MetadataUpdateListener {
            override fun onMetadataChanged(metadata: MediaMetadata?) {
                mediaSession?.setMetadata(metadata)
            }

            override fun onQueueUpdated(
                title: String?,
                newQueue: MutableList<MediaSession.QueueItem>?
            ) {
                mediaSession?.setQueueTitle(title)
                mediaSession?.setQueue(newQueue)
            }

            override fun onPlayerStatusChanger(status: Int) {
                if (MediaSessionCallbackManager.ON_PLAYER_STATUS_PLAY == status) {
                    mediaNotificationManager?.startNotification()
                } else {
                    mediaNotificationManager?.refreshNotification()
                }
            }

            override fun onPlaybackStateUpdated(newState: PlaybackState?) {
                mediaSession?.setPlaybackState(newState)
            }
        })
        mediaSession?.setCallback(sessionCallBack)
        sessionToken = mediaSession?.sessionToken

        mediaNotificationManager = MediaNotificationManager(this)

        audioPlayerManager = AudioPlayerManager.getInstance()
        audioPlayerManager?.addPlayerEventListenerList(object :
            AudioPlayerManager.OnPlayerEventListener {
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
                sendPlayerExposure(progress)
            }
        })
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
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
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
        contentCtrace(event)
    }

    private fun initAudioNextPlayer(newPosition: Int?) {
        if (0 > newPosition ?: 0) {
            return
        }

        if (Constant.HEADER_LINES_TAG != Constant.response?.album_type) {
            if (null == Constant.currentChildInPlayerListPosition) {
                if (!Constant.responses.isNullOrEmpty()) {
                    for (i in Constant.responses!!.indices) {
                        val item = Constant.responses!![i]
                        if (item.program_id.toString() == audioPlayerManager?.currentMediaItemId) {
                            Constant.currentChildInPlayerListPosition = i
                            break
                        }
                    }
                }
            }

            if (Constant.currentChildInPlayerListPosition == newPosition) {
                return
            }
            Constant.currentChildInPlayerListPosition = newPosition

            val playerListSize = Constant.responses?.size ?: 0
            if ((newPosition ?: 0) >= playerListSize - 1) {//下一首
                Constant.currentChildPosition = Constant.currentChildPosition?.inc()

                val albumId = Constant.response?.album_id ?: 0
                val pageSize = 10
                val page = Constant.currentChildPosition ?: 0
                val playerStatus = Constant.PLAYER_STATUS_TAG_3
                fetchProgramList(albumId, pageSize, page, playerStatus)
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (null != intent) {
            val action: String? = intent.action
            val command: String? = intent.getStringExtra(CMD_NAME)
            if (ACTION_CMD == action) {
                // 暂停命令
                if (CMD_PAUSE == command) {
                    pause()
                }
            }

            if (intent.hasExtra(ALBUM_ID_TAG)) {
                albumId = intent.getIntExtra(ALBUM_ID_TAG, 0)
            }

            if (intent.hasExtra(PAGE_SIZE_TAG)) {
                pageSize = intent.getIntExtra(PAGE_SIZE_TAG, 0)
            }

            if (intent.hasExtra(PAGE_TAG)) {
                page = intent.getIntExtra(PAGE_TAG, 0)
            }

            if (intent.hasExtra(PLAYER_STATUS_TAG)) {
                playerStatus = intent.getIntExtra(PLAYER_STATUS_TAG, 0)
            }

            fetchProgramList(albumId, pageSize, page, playerStatus)
        }
        return START_NOT_STICKY
    }

    private fun player() {
        AudioPlayerManager.getInstance().start()
    }

    private fun pause() {
        AudioPlayerManager.getInstance().pause()
    }

    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?
    ): BrowserRoot {
        if (clientPackageName != packageName) {
            return BrowserRoot(MEDIA_ID_EMPTY_ROOT, null)
        }

        return BrowserRoot(MEDIA_ID_ROOT, null)
    }

    override fun onLoadChildren(
        parentId: String,
        result: Result<MutableList<MediaBrowser.MediaItem>>
    ) {
        if (MEDIA_ID_EMPTY_ROOT == parentId) {
            result.sendResult(mutableListOf())
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        AudioPlayerManager.getInstance().release()

        mediaNotificationManager?.stopNotification()
        mediaSession?.release()

        MediaBrowserManager.instance.disconnect()
    }

    //获取章节详情列表
    private fun fetchProgramList(
        album_id: Int?,
        page_size: Int?,
        page: Int?,
        playerStatus: Int?
    ) {
        ApiService.instance.getProgramList(album_id, page_size, page)
            .subscribe(object : CommonObserver<GetProgramListResponse>() {
                override fun onResponseSuccess(response: GetProgramListResponse?) {
                    val event = GetProgramListResponseEvent()
                    if (!response?.data.isNullOrEmpty()) {
                        for (item in response?.data!!.iterator()) {
                            CustomBasicApplication.instance?.let {
                                val entity = RoomManager.getInstance(it)
                                    .loadSingleListenCommon(item.program_id)
                                item.download_status =
                                    entity?.download_status ?: Constant.COMMON_DOWNLOAD_STATUS_1
                            }
                        }
                    }
                    event.data = response
                    event.playerStatus = playerStatus
                    EventBus.getDefault().post(event)
                }

                override fun onResponseError(message: String?, e: Throwable?, code: Int?) {
                    super.onResponseError(message, e, code)
                    EventBus.getDefault().post(GetProgramListResponseEvent())
                }
            })
    }

    private fun contentCtrace(event: String?) {
        if (TextUtils.isEmpty(event)) {
            return
        }

        val response = Constant.response
        val token = PreferencesUtils.getString(PreferencesKey.TOKEN)

        val chapterId = if (Constant.HEADER_LINES_TAG == response?.album_type) {
            response.album_lid ?: ""
        } else {
            response?.program_lid ?: ""
        }

        val mid = ContentAndroidSDK.mid

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
}