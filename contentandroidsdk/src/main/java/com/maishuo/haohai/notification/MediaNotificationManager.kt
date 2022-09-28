package com.maishuo.haohai.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.media.MediaDescription
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSession
import android.media.session.PlaybackState
import android.os.Build
import androidx.annotation.RequiresApi
import com.maishuo.haohai.R
import com.maishuo.haohai.audio.AudioPlayerManager
import com.maishuo.haohai.main.service.AudioPlayerService
import com.maishuo.haohai.utils.ResourceUtil

/**
 * author : xpSun
 * date : 2022/1/24
 * description :
 */
class MediaNotificationManager constructor(private val service: AudioPlayerService?) :
    BroadcastReceiver() {

    companion object {
        private const val ACTION_PAUSE = "com.haohai.content.sdk.music.pause"
        private const val ACTION_PLAY = "com.haohai.content.sdk.music.play"
        private const val ACTION_PREV = "com.haohai.content.sdk.music.prev"
        private const val ACTION_NEXT = "com.haohai.content.sdk.music.next"
        private const val ACTION_STOP_CASTING = "com.haohai.content.sdk.music.stop_cast"


        private const val CHANNEL_ID = "com.haohai.content.sdk.MUSIC_CHANNEL_ID"
        private const val NOTIFICATION_ID = 4120
        private const val REQUEST_CODE = 1000
    }

    private var notificationManager: NotificationManager? = null

    private var sessionToken: MediaSession.Token? = null
    private var mediaController: MediaController? = null
    private var transportControls: MediaController.TransportControls? = null

    private var mStarted: Boolean = false
    private var notificationColor: Int? = null

    // 暂停
    private var mPauseIntent: PendingIntent? = null

    // 播放
    private var mPlayIntent: PendingIntent? = null

    // 上一个
    private var mPreviousIntent: PendingIntent? = null

    // 下一个
    private var mNextIntent: PendingIntent? = null

    private var mediaMetadata: MediaMetadata? = null
    private var playbackState: PlaybackState? = null

    init {
        initWidgets()
    }

    private fun initWidgets() {
        getMediaControllerBySessionToken()

        notificationManager =
            service?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?

        notificationColor = ResourceUtil.getThemeColor(service, Color.TRANSPARENT, Color.DKGRAY)

        val packageName = service?.packageName
        mPauseIntent = PendingIntent.getBroadcast(
            service,
            REQUEST_CODE,
            Intent(ACTION_PAUSE)
                .setPackage(packageName),
            PendingIntent.FLAG_CANCEL_CURRENT
        )

        mPlayIntent = PendingIntent.getBroadcast(
            service,
            REQUEST_CODE,
            Intent(ACTION_PLAY)
                .setPackage(packageName),
            PendingIntent.FLAG_CANCEL_CURRENT
        )

        mPreviousIntent = PendingIntent.getBroadcast(
            service,
            REQUEST_CODE,
            Intent(ACTION_PREV)
                .setPackage(packageName),
            PendingIntent.FLAG_CANCEL_CURRENT
        )

        mNextIntent = PendingIntent.getBroadcast(
            service,
            REQUEST_CODE,
            Intent(ACTION_NEXT)
                .setPackage(packageName),
            PendingIntent.FLAG_CANCEL_CURRENT
        )

        try {
            notificationManager?.cancel(NOTIFICATION_ID)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun refreshNotification(): Notification? {
        return try {
            createNotification()
        } catch (e: Exception) {
            null
        }
    }

    fun startNotification() {
        if (!mStarted) {
            mediaMetadata = mediaController?.metadata
            playbackState = mediaController?.playbackState

            val notification = createNotification()
            setIcon(mediaMetadata)
            if (null != notification) {
                mediaController?.registerCallback(mediaControllerCallback)
                val filter = IntentFilter()
                filter.addAction(ACTION_NEXT)
                filter.addAction(ACTION_PAUSE)
                filter.addAction(ACTION_PLAY)
                filter.addAction(ACTION_PREV)
                filter.addAction(ACTION_STOP_CASTING)
                service?.registerReceiver(this, filter)

                service?.startForeground(
                    NOTIFICATION_ID,
                    notification
                )
                mStarted = true
            }
        }
    }

    fun stopNotification() {
        if (mStarted) {
            mStarted = false
            mediaController?.unregisterCallback(mediaControllerCallback)
            try {
                notificationManager?.cancel(NOTIFICATION_ID)
                service?.unregisterReceiver(this)
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
            service?.stopForeground(true)
        }
    }

    private fun createNotification(): Notification? {
        // 创建通知栏
        if (mediaMetadata == null) {
            return null
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel()
        }

        if (null == service?.baseContext) {
            return null
        }

        val notificationBuilder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(service.baseContext!!, CHANNEL_ID)
        } else {
            Notification.Builder(service)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationBuilder.setChannelId(CHANNEL_ID)
        }

        // 上一曲
        notificationBuilder.addAction(
            R.mipmap.notification_player_up,
            service.getString(R.string.label_previous), mPreviousIntent
        )

        // 添加 暂停 播放
        addPlayPauseAction(notificationBuilder)
        // 下一曲
        notificationBuilder.addAction(
            R.mipmap.notification_player_down,
            service.getString(R.string.label_next), mNextIntent
        )
        val description: MediaDescription? = mediaMetadata?.description
        var art: Bitmap? = null //通知栏封面图
        if (description?.iconUri != null) {
            val artUrl = description.iconUri.toString()
            art = AlbumArtCache.getInstance().getBigImage(artUrl)
            if (null == art) {
                art = BitmapFactory.decodeResource(
                    service.resources,
                    R.mipmap.ic_launcher
                )
            }
        }

        val notificationColor = ResourceUtil.getThemeColor(service, Color.TRANSPARENT, Color.DKGRAY)
        notificationBuilder
            .setStyle(
                Notification.MediaStyle()
                    .setShowActionsInCompactView(
                        0, 1, 2
                    )
                    .setMediaSession(sessionToken)
            )
            .setColor(notificationColor)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setVisibility(Notification.VISIBILITY_PUBLIC)
            .setShowWhen(false)
            .setContentIntent(createContentIntent())
            .setContentTitle(description?.title)
            .setContentText(description?.subtitle)
            .setLargeIcon(art)
            .setOngoing(true)
        return notificationBuilder.build()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel() {
        if (notificationManager?.getNotificationChannel(CHANNEL_ID) == null) {
            val notificationChannel = NotificationChannel(
                CHANNEL_ID,
                "UAMP_Channel_ID",
                NotificationManager.IMPORTANCE_LOW
            )
            notificationChannel.description = "Channel ID for UAMP"
            notificationManager?.createNotificationChannel(notificationChannel)
        }
    }

    private fun getMediaControllerBySessionToken() {
        val mediaSessionToken = service?.sessionToken

        if (null == sessionToken && null != mediaSessionToken ||
            null != sessionToken && sessionToken != mediaSessionToken
        ) {

            if (null != mediaController) {
                mediaController?.unregisterCallback(mediaControllerCallback)
            }

            sessionToken = mediaSessionToken

            if (null != sessionToken && null != service?.baseContext) {
                mediaController = MediaController(service.baseContext, sessionToken!!)
                transportControls = mediaController?.transportControls

                if (mStarted) {
                    mediaController?.registerCallback(mediaControllerCallback)
                }
            }
        }
    }

    //添加暂停、播放
    private fun addPlayPauseAction(builder: Notification.Builder) {
        val label: String?
        val icon: Int?
        val intent: PendingIntent?
        if (AudioPlayerManager.getInstance().isPlaying) {
            label = service?.getString(R.string.label_pause) ?: ""
            icon = R.mipmap.notification_pause_icon
            intent = mPauseIntent
        } else {
            label = service?.getString(R.string.label_play) ?: ""
            icon = R.mipmap.notification_play_icon
            intent = mPlayIntent
        }
        builder.addAction(Notification.Action(icon, label, intent))
    }

    private fun createContentIntent(): PendingIntent? {
        return try {
            val intent: Intent? =
                service?.packageManager?.getLaunchIntentForPackage(service.packageName ?: "")
                    ?.setPackage(null)
                    ?.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
            PendingIntent.getActivity(
                service,
                REQUEST_CODE,
                intent,
                0
            )
        } catch (e: java.lang.Exception) {
            null
        }
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        when (intent?.action) {
            ACTION_PAUSE -> {
                transportControls?.pause()
            }
            ACTION_PLAY -> {
                transportControls?.play()
            }
            ACTION_NEXT -> {
                transportControls?.skipToNext()
            }
            ACTION_PREV -> {
                transportControls?.skipToPrevious()
            }
            ACTION_STOP_CASTING -> {
                val i = Intent(context, AudioPlayerService::class.java)
                i.action = AudioPlayerService.ACTION_CMD
                i.putExtra(AudioPlayerService.CMD_NAME, AudioPlayerService.CMD_STOP_CASTING)
                context?.stopService(i)
            }
        }
    }

    private val mediaControllerCallback = object : MediaController.Callback() {
        override fun onPlaybackStateChanged(state: PlaybackState?) {
            playbackState = state

            if (state?.state == PlaybackState.STATE_STOPPED ||
                state?.state == PlaybackState.STATE_NONE
            ) {
                stopNotification()
            } else {
                val notification: Notification? = createNotification()
                if (notification != null) {
                    notificationManager?.notify(NOTIFICATION_ID, notification)
                }
            }
        }

        override fun onMetadataChanged(metadata: MediaMetadata?) {
            mediaMetadata = metadata
            val notification: Notification? = createNotification()
            setIcon(metadata)
            if (notification != null) {
                notificationManager?.notify(NOTIFICATION_ID, notification)
            }
        }

        override fun onSessionDestroyed() {
            super.onSessionDestroyed()
            try {
                getMediaControllerBySessionToken()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun setIcon(metadata: MediaMetadata?) {
        if (metadata == null || metadata.description.iconUri == null) return
        val description = metadata.description
        val artUrl = description.iconUri.toString()
        fetchBitmapFromURLAsync(artUrl)
    }

    private fun fetchBitmapFromURLAsync(bitmapUrl: String?) {
        AlbumArtCache.getInstance().fetch(bitmapUrl, object : AlbumArtCache.FetchListener() {
            override fun onFetched(artUrl: String, bitmap: Bitmap?, icon: Bitmap?) {
                if (mediaMetadata != null &&
                    mediaMetadata?.description?.iconUri != null &&
                    mediaMetadata?.description?.iconUri.toString() == artUrl
                ) {
                    val notification = createNotification()
                    if (null != notification) {
                        notificationManager?.notify(NOTIFICATION_ID, notification)
                    }
                }
            }
        })
    }
}