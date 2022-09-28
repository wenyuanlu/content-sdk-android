package com.maishuo.haohai.mediabrowser

import android.media.MediaDescription
import android.media.MediaMetadata
import android.media.session.MediaSession
import android.media.session.PlaybackState
import android.os.Bundle
import android.text.TextUtils
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Player
import com.maishuo.haohai.api.response.GetListResponse
import com.maishuo.haohai.audio.AudioPlayerManager
import com.maishuo.haohai.common.Constant
import com.maishuo.haohai.utils.QueueUtil

/**
 * author : xpSun
 * date : 2022/1/24
 * description :
 */
class MediaSessionCallbackManager constructor(private val onUpdateListener: MetadataUpdateListener?) :
    MediaSession.Callback() {

    companion object {
        /**
         * action
         */
        // 播放音频列表
        const val CUSTOM_ACTION_MUSIC_PLAY_QUNEN = "com.haohai.content.sdk.music.MUSIC_QUEUE_PLAY"

        // 更新队列
        const val CUSTOM_ACTION_MUSIC_UPDATE_QUNEN =
            "com.haohai.content.sdk.music.MUSIC_QUEUE_UPDATE"

        // 重置队列
        const val CUSTOM_ACTION_MUSIC_QUEUE_RESET = "com.haohai.content.sdk.music.MUSIC_QUEUE_RESET"

        //key
        // 音频队列数据
        const val KEY_MUSIC_QUEUE = "com.haohai.content.sdk.music.KEY_MUSIC_QUEUE"

        // 音频队列的title数据
        const val KEY_MUSIC_QUEUE_TITLE = "com.haohai.content.sdk.music.KEY_MUSIC_QUEUE_TITLE"

        // 播放index，小于0表示不播
        const val KEY_MUSIC_QUEUE_PLAY_INDEX =
            "com.haohai.content.sdk.music.KEY_MUSIC_QUEUE_PLAY_INDEX"

        const val ON_PLAYER_STATUS_PLAY: Int = 1
        const val ON_PLAYER_STATUS_STOP: Int = 2
        const val ON_PLAYER_STATUS_PAUSE: Int = 3
    }

    private var currentPosition: Long? = null

    init {
        AudioPlayerManager.getInstance()
            .addPlayerEventListenerList(object : AudioPlayerManager.OnPlayerEventListener {
                override fun onError(exception: PlaybackException?, msg: String?) {
                    val stateBuilder = PlaybackState.Builder()
                    val state = PlaybackState.STATE_ERROR
                    val speed = AudioPlayerManager.getInstance().currentPlayerSpeed
                    stateBuilder.setState(state, 0, speed)
                    onUpdateListener?.onPlaybackStateUpdated(stateBuilder.build())
                }

                override fun onPlayerStatus(status: Int) {
                    if (AudioPlayerManager.CURRENT_PLAYER_STATUS_PLAYER == status) {
                        updatePlayingStatus()
                    } else {
                        updatePauseStatus()
                    }
                }

                override fun onPlayerCurrentPosition(
                    position: Long?,
                    duration: Long?,
                    progress: Double?
                ) {
                    currentPosition = position
                }

                override fun onPlayerPrevious() {
                    val currentPosition = getCurrentPlayerPosition()
                    MediaBrowserManager.instance.updatePlayerList(
                        Constant.responses,
                        currentPosition.dec()
                    )
                }

                override fun onPlayerNext() {
                    val currentPosition = getCurrentPlayerPosition()
                    MediaBrowserManager.instance.updatePlayerList(
                        Constant.responses,
                        currentPosition.inc()
                    )
                }

                override fun onPositionDiscontinuity(
                    oldPosition: Player.PositionInfo?,
                    newPosition: Player.PositionInfo?,
                    reason: Int
                ) {
                    if (reason == Player.DISCONTINUITY_REASON_SEEK_ADJUSTMENT ||
                        reason == Player.DISCONTINUITY_REASON_AUTO_TRANSITION
                    ) {
                        val currentPlayerPosition = getCurrentPlayerPosition()
                        MediaBrowserManager.instance.updatePlayerList(
                            Constant.responses,
                            currentPlayerPosition
                        )

                        val stateBuilder = PlaybackState.Builder()
                        val isPlaying = AudioPlayerManager.getInstance().isPlaying
                        val state =
                            if (isPlaying) PlaybackState.STATE_PLAYING else PlaybackState.STATE_PAUSED
                        val speed = AudioPlayerManager.getInstance().currentPlayerSpeed

                        currentPosition = 0
                        stateBuilder.setState(state, currentPosition ?: 0L, speed)
                        onUpdateListener?.onPlaybackStateUpdated(stateBuilder.build())
                    }
                }
            })
    }

    private fun getCurrentPlayerPosition(): Int {
        var currentPosition = 0
        val response = Constant.response
        val responseList: List<GetListResponse>? = Constant.responses

        if (null != response && !responseList.isNullOrEmpty()) {
            for (i in responseList.indices) {
                if (Constant.HEADER_LINES_TAG == responseList[i].album_type) {
                    if (TextUtils.equals(
                            response.album_lid,
                            responseList[i].album_lid
                        )
                    ) {
                        currentPosition = i
                        break
                    }
                } else {
                    if (TextUtils.equals(
                            response.program_lid,
                            responseList[i].program_lid
                        )
                    ) {
                        currentPosition = i
                        break
                    }
                }
            }
        }

        return currentPosition
    }

    private fun updatePlayingStatus() {
        val stateBuilder = PlaybackState.Builder()
        val state = PlaybackState.STATE_PLAYING
        val speed = AudioPlayerManager.getInstance().currentPlayerSpeed
        stateBuilder.setState(state, currentPosition ?: 0L, speed)
        onUpdateListener?.onPlaybackStateUpdated(stateBuilder.build())
    }

    private fun updatePauseStatus() {
        val stateBuilder = PlaybackState.Builder()
        val state = PlaybackState.STATE_PAUSED
        val speed = AudioPlayerManager.getInstance().currentPlayerSpeed
        stateBuilder.setState(state, currentPosition ?: 0L, speed)
        onUpdateListener?.onPlaybackStateUpdated(stateBuilder.build())
    }

    interface MetadataUpdateListener {
        // 数据变化
        fun onMetadataChanged(metadata: MediaMetadata?)

        // 队列更新
        fun onQueueUpdated(title: String?, newQueue: MutableList<MediaSession.QueueItem>?)

        //播放状态改变
        fun onPlayerStatusChanger(status: Int)

        fun onPlaybackStateUpdated(newState: PlaybackState?)
    }

    override fun onPlay() {
        super.onPlay()

        changerPlayerStatus()
    }

    override fun onStop() {
        super.onStop()

        AudioPlayerManager.getInstance().stop()

        onUpdateListener?.onPlayerStatusChanger(ON_PLAYER_STATUS_STOP)
    }

    override fun onPause() {
        super.onPause()

        changerPlayerStatus()
    }

    private fun changerPlayerStatus() {
        if (!AudioPlayerManager.getInstance().isPlaying) {
            AudioPlayerManager.getInstance().start()
            onUpdateListener?.onPlayerStatusChanger(ON_PLAYER_STATUS_PLAY)
        } else {
            AudioPlayerManager.getInstance().pause()
            onUpdateListener?.onPlayerStatusChanger(ON_PLAYER_STATUS_PAUSE)
        }
    }

    override fun onSkipToNext() {
        super.onSkipToNext()

        AudioPlayerManager.getInstance().seekToNext()

        updatePlayingStatus()
    }

    override fun onSkipToPrevious() {
        super.onSkipToPrevious()

        AudioPlayerManager.getInstance().seekToPrevious()

        updatePlayingStatus()
    }

    override fun onSeekTo(pos: Long) {
        super.onSeekTo(pos)

        AudioPlayerManager.getInstance().seekTo(pos)
    }

    override fun onCustomAction(action: String, extras: Bundle?) {
        super.onCustomAction(action, extras)

        when (action) {
            CUSTOM_ACTION_MUSIC_PLAY_QUNEN -> {
                playMusicQueue(extras)
            }
            CUSTOM_ACTION_MUSIC_UPDATE_QUNEN -> {
                updateMusicQueue(extras)
            }
            CUSTOM_ACTION_MUSIC_QUEUE_RESET -> {
                handleResetPlayerQueue(extras)
            }
        }
    }

    /**
     * 播放音频列表
     *
     * @param extras
     */
    private fun playMusicQueue(extras: Bundle?) {
        if (extras == null) {
            return
        }
        extras.classLoader = MediaDescription::class.java.classLoader
        // 列表数据
        val list: List<MediaMetadata> =
            extras.getParcelableArrayList(KEY_MUSIC_QUEUE)
                ?: return
        // 标题
        val title = extras.getString(
            KEY_MUSIC_QUEUE_TITLE,
            "new queue"
        )
        // 播放的index
        val index = extras.getInt(
            KEY_MUSIC_QUEUE_PLAY_INDEX,
            -1
        )

        val queueItemList: MutableList<MediaSession.QueueItem>? =
            QueueUtil.convertToQueue(list)
        onUpdateListener?.onQueueUpdated(title, queueItemList)

        val item = list[index]
        onUpdateListener?.onMetadataChanged(item)

        onUpdateListener?.onPlayerStatusChanger(ON_PLAYER_STATUS_PLAY)
    }

    /**
     * 更新播放队列
     *
     * @param extras
     */
    private fun updateMusicQueue(extras: Bundle?) {
        //
        if (extras == null) {
            return
        }
        extras.classLoader = MediaDescription::class.java.classLoader
        // 列表数据
        val list: MutableList<MediaMetadata> =
            extras.getParcelableArrayList(KEY_MUSIC_QUEUE)
                ?: return
        // 标题
        val title = extras.getString(
            KEY_MUSIC_QUEUE_TITLE,
            "new queue"
        )
        // 播放的index
        val index = extras.getInt(
            KEY_MUSIC_QUEUE_PLAY_INDEX,
            -1
        )

        val item = list[index]
        onUpdateListener?.onMetadataChanged(item)
    }

    /**
     * 重置播放队列
     */
    private fun handleResetPlayerQueue(extras: Bundle?) {
        if (extras == null) {
            return
        }
        extras.classLoader = MediaDescription::class.java.classLoader
        // 获取音频队列数据
        val list: MutableList<MediaMetadata> =
            extras.getParcelableArrayList(KEY_MUSIC_QUEUE)
                ?: return
        // 队列标题
        val title = extras.getString(
            KEY_MUSIC_QUEUE_TITLE,
            "new queue"
        )
        // 播放的index
        val index = extras.getInt(
            KEY_MUSIC_QUEUE_PLAY_INDEX,
            -1
        )

        val queueItemList: MutableList<MediaSession.QueueItem>? =
            QueueUtil.convertToQueue(list)
        onUpdateListener?.onQueueUpdated(title, queueItemList)
    }
}