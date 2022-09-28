package com.maishuo.haohai.mediabrowser

import android.content.ComponentName
import android.content.Context
import android.media.browse.MediaBrowser
import android.media.session.MediaController
import android.media.session.MediaSession
import android.media.session.PlaybackState
import android.os.Bundle
import com.maishuo.haohai.api.response.GetListResponse
import com.maishuo.haohai.audio.AudioPlayerManager
import com.maishuo.haohai.main.service.AudioPlayerService
import com.maishuo.haohai.utils.MusicConvertUtil
import java.lang.ref.WeakReference

/**
 * author : xpSun
 * date : 2022/1/24
 * description :
 */
class MediaBrowserManager private constructor() {

    companion object {
        val instance: MediaBrowserManager by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            MediaBrowserManager()
        }
    }

    private var audioPlayerManager: AudioPlayerManager? = null
    private var activityWeakReference: WeakReference<Context>? = null
    private var mediaBrowser: MediaBrowser? = null
    private var mediaController: MediaController? = null
    private var transportControls: MediaController.TransportControls? = null

    private val connectionCallback = object : MediaBrowser.ConnectionCallback() {
        override fun onConnected() {
            super.onConnected()
            connectToSession(mediaBrowser?.sessionToken)
        }
    }

    private val mediaControllerCallback = object : MediaController.Callback() {
        override fun onPlaybackStateChanged(state: PlaybackState?) {
            super.onPlaybackStateChanged(state)

            when (state?.actions) {
                PlaybackState.ACTION_PLAY -> {
                    audioPlayerManager?.start()
                }
                PlaybackState.ACTION_PAUSE -> {
                    audioPlayerManager?.pause()
                }
                PlaybackState.ACTION_STOP -> {
                    audioPlayerManager?.stop()
                }
            }
        }
    }

    init {
        audioPlayerManager = AudioPlayerManager.getInstance()
    }

    fun builder(context: Context?): MediaBrowserManager {
        context ?: return instance
        activityWeakReference = WeakReference(context)

        val componentName = ComponentName(context, AudioPlayerService::class.java)
        mediaBrowser = MediaBrowser(context, componentName, connectionCallback, null)
        return instance
    }

    fun connect(): MediaBrowserManager {
        try {
            if (mediaBrowser?.isConnected != true) {
                mediaBrowser?.connect()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return instance
    }

    fun disconnect(): MediaBrowserManager {
        if (mediaBrowser?.isConnected == true) {
            mediaBrowser?.disconnect()
        }
        return instance
    }

    private fun connectToSession(token: MediaSession.Token?) {
        token ?: return
        val activity = activityWeakReference?.get() ?: return

        mediaController = MediaController(activity, token)
        mediaController?.registerCallback(mediaControllerCallback)
        transportControls = mediaController?.transportControls
    }

    fun initPlayerList(playerList: MutableList<GetListResponse>?, position: Int?) {
        if (playerList.isNullOrEmpty()) {
            return
        }

        val bundle = Bundle()
        val list = MusicConvertUtil.convertToMediaMetadataList(playerList)
        bundle.putParcelableArrayList(MediaSessionCallbackManager.KEY_MUSIC_QUEUE, list)
        bundle.putInt(MediaSessionCallbackManager.KEY_MUSIC_QUEUE_PLAY_INDEX, position ?: 0)
        transportControls?.sendCustomAction(
            MediaSessionCallbackManager.CUSTOM_ACTION_MUSIC_PLAY_QUNEN,
            bundle
        )
    }

    fun updatePlayerList(playerList: MutableList<GetListResponse>?, position: Int?) {
        if (playerList.isNullOrEmpty()) {
            return
        }

        val bundle = Bundle()
        val list = MusicConvertUtil.convertToMediaMetadataList(playerList)
        bundle.putParcelableArrayList(MediaSessionCallbackManager.KEY_MUSIC_QUEUE, list)
        bundle.putInt(MediaSessionCallbackManager.KEY_MUSIC_QUEUE_PLAY_INDEX, position ?: 0)
        transportControls?.sendCustomAction(
            MediaSessionCallbackManager.CUSTOM_ACTION_MUSIC_UPDATE_QUNEN,
            bundle
        )
    }

    fun player() {
        transportControls?.play()
    }

    fun pause() {
        transportControls?.pause()
    }

    fun stop() {
        transportControls?.stop()
    }

    fun skipToPrevious() {
        transportControls?.skipToPrevious()
    }

    fun skipToNext() {
        transportControls?.skipToNext()
    }

    fun seekTo(position: Long?) {
        transportControls?.seekTo(position ?: 0L)
    }
}