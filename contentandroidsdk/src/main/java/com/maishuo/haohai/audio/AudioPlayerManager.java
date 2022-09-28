package com.maishuo.haohai.audio;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;

import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.PlaybackException;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.RenderersFactory;
import com.google.android.exoplayer2.audio.AudioAttributes;
import com.google.android.exoplayer2.database.DatabaseProvider;
import com.google.android.exoplayer2.database.StandaloneDatabaseProvider;
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory;
import com.google.android.exoplayer2.source.MediaSourceFactory;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSource;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource;
import com.google.android.exoplayer2.upstream.HttpDataSource;
import com.google.android.exoplayer2.upstream.cache.Cache;
import com.google.android.exoplayer2.upstream.cache.CacheDataSource;
import com.google.android.exoplayer2.upstream.cache.NoOpCacheEvictor;
import com.google.android.exoplayer2.upstream.cache.SimpleCache;
import com.google.android.exoplayer2.util.EventLogger;
import com.qichuang.commonlibs.basic.CustomBasicApplication;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.math.BigDecimal;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.ArrayList;
import java.util.List;

@SuppressLint("StaticFieldLeak")
public class AudioPlayerManager {

    private static Context context;
    private ExoPlayer exoPlayer;
    private Player.Listener eventListener;

    //播放倍速
    private float currentPlayerSpeed = PlayerSpeedEnum.CUSTOM_PLAYER_SPEED_3.getValue();

    private static AudioPlayerManager instance;

    private static DatabaseProvider databaseProvider;
    private static File downloadDirectory;
    private static HttpDataSource.Factory httpDataSourceFactory;
    private static Cache downloadCache;

    private static final String DOWNLOAD_CONTENT_DIRECTORY = "downloads";

    public static final int CURRENT_PLAYER_STATUS_STOP = 0;
    public static final int CURRENT_PLAYER_STATUS_PLAYER = 1;
    public static final int CURRENT_PLAYER_STATUS_PAUSE = 2;
    public static final int CURRENT_PLAYER_STATUS_RELEASE = 3;

    private String currentMediaItemId = "";

    private AudioPlayerFactor audioPlayerFactor;

    private AudioPlayerManager(Context context) {
        AudioPlayerManager.context = context;
        //初始化创建的参数
        createPlayer();
        //监听
        initListener();
    }

    public static AudioPlayerManager getInstance() {
        if (null == instance) {
            instance = new AudioPlayerManager(CustomBasicApplication.INSTANCE.getInstance());
        }

        return instance;
    }

    //创建一个新的player
    private void createPlayer() {
        DefaultDataSource.Factory upstreamFactory =
                new DefaultDataSource.Factory(context, getHttpDataSourceFactory());
        CacheDataSource.Factory dataSourceFactory = buildReadOnlyCacheDataSource(upstreamFactory, getDownloadCache(context));
        RenderersFactory renderersFactory =
                buildRenderersFactory(context);
        MediaSourceFactory mediaSourceFactory =
                new DefaultMediaSourceFactory(dataSourceFactory);

        DefaultTrackSelector trackSelector = new DefaultTrackSelector(context);
        exoPlayer =
                new ExoPlayer.Builder(context, renderersFactory)
                        .setMediaSourceFactory(mediaSourceFactory)
                        .setTrackSelector(trackSelector)
                        .build();
        exoPlayer.addAnalyticsListener(new EventLogger(trackSelector));
        exoPlayer.setAudioAttributes(AudioAttributes.DEFAULT, true);

        audioPlayerFactor = new AudioPlayerFactor();
    }

    public static synchronized HttpDataSource.Factory getHttpDataSourceFactory() {
        if (httpDataSourceFactory == null) {
            CookieManager cookieManager = new CookieManager();
            cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ORIGINAL_SERVER);
            CookieHandler.setDefault(cookieManager);
            httpDataSourceFactory = new DefaultHttpDataSource.Factory();
        }
        return httpDataSourceFactory;
    }

    private static CacheDataSource.Factory buildReadOnlyCacheDataSource(
            DataSource.Factory upstreamFactory, Cache cache) {
        return new CacheDataSource.Factory()
                .setCache(cache)
                .setUpstreamDataSourceFactory(upstreamFactory)
                .setCacheWriteDataSinkFactory(null)
                .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR);
    }

    private static synchronized Cache getDownloadCache(Context context) {
        if (downloadCache == null) {
            File downloadContentDirectory =
                    new File(getDownloadDirectory(context), DOWNLOAD_CONTENT_DIRECTORY);
            downloadCache =
                    new SimpleCache(
                            downloadContentDirectory, new NoOpCacheEvictor(), getDatabaseProvider(context));
        }
        return downloadCache;
    }

    private static synchronized File getDownloadDirectory(Context context) {
        if (downloadDirectory == null) {
            downloadDirectory = context.getExternalFilesDir(null);
            if (downloadDirectory == null) {
                downloadDirectory = context.getFilesDir();
            }
        }
        return downloadDirectory;
    }

    private static synchronized DatabaseProvider getDatabaseProvider(Context context) {
        if (databaseProvider == null) {
            databaseProvider = new StandaloneDatabaseProvider(context);
        }
        return databaseProvider;
    }

    public static RenderersFactory buildRenderersFactory(
            Context context) {
        @DefaultRenderersFactory.ExtensionRendererMode
        int extensionRendererMode =
                DefaultRenderersFactory.EXTENSION_RENDERER_MODE_OFF;
        return new DefaultRenderersFactory(context.getApplicationContext())
                .setExtensionRendererMode(extensionRendererMode);
    }

    public ExoPlayer getMediaPlayer() {
        return exoPlayer;
    }

    public void setRepeatMode(@Player.RepeatMode int repeatMode) {
        if (exoPlayer != null) {
            exoPlayer.setRepeatMode(repeatMode);
        }
    }

    public int getRepeatMode() {
        return exoPlayer.getRepeatMode();
    }

    public void setAudios(List<MediaItem> audios) {
        if (null == audios || audios.isEmpty() || null == exoPlayer) {
            return;
        }

        exoPlayer.clearMediaItems();
        exoPlayer.setMediaItems(audios, 0, 0);
    }

    public void addAudios(List<MediaItem> audios) {
        if (null == audios || audios.isEmpty() || null == exoPlayer) {
            return;
        }

        exoPlayer.addMediaItems(audios);
    }

    public void setAudios(List<MediaItem> audios, int startPosition) {
        setAudios(audios);
        seekTo(startPosition, 0);
    }

    public void seekTo(int mediaItemIndex, long positionMs) {
        if (exoPlayer != null) {
            exoPlayer.seekTo(mediaItemIndex, positionMs);
            exoPlayer.prepare();
        }
    }

    public void removeAudio(int position) {
        if (exoPlayer != null) {
            exoPlayer.removeMediaItem(position);
        }
    }

    public void seekToPrevious() {
        if (null == exoPlayer) {
            return;
        }

        if (hasPreviousMediaItem()) {
            exoPlayer.seekToPrevious();
        }

        if(!isPlaying()){
            start();
        }

        if(!playerEventListenerList.isEmpty()){
            for(OnPlayerEventListener item :playerEventListenerList){
                if (item != null) {
                    item.onPlayerPrevious();
                }
            }
        }
    }

    public void seekToNext() {
        if (null == exoPlayer) {
            return;
        }

        if (hasNextMediaItem()) {
            exoPlayer.seekToNext();
        }

        if(!isPlaying()){
            start();
        }

        if(!playerEventListenerList.isEmpty()){
            for(OnPlayerEventListener item :playerEventListenerList){
                if (item != null) {
                    item.onPlayerNext();
                }
            }
        }
    }

    public boolean hasNextMediaItem() {
        if (null == exoPlayer) {
            return false;
        }
        return exoPlayer.hasNextMediaItem();
    }


    public boolean hasPreviousMediaItem() {
        if (null == exoPlayer) {
            return false;
        }
        return exoPlayer.hasPreviousMediaItem();
    }

    public String getCurrentMediaItemId() {
        if (null != exoPlayer && null != exoPlayer.getCurrentMediaItem()) {
            if (!TextUtils.isEmpty(exoPlayer.getCurrentMediaItem().mediaId)) {
                currentMediaItemId = exoPlayer.getCurrentMediaItem().mediaId;
            }
            return currentMediaItemId;
        }
        return "";
    }

    //设置播放 网络url
    public void setAudioUrl(String audioUrl) {
        if (TextUtils.isEmpty(audioUrl) || null == exoPlayer) {
            return;
        }

        try {
            MediaItem mediaItem = MediaItem.fromUri(audioUrl);
            exoPlayer.setMediaItem(mediaItem);
            exoPlayer.prepare();
            exoPlayer.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 调整音量
     *
     * @param volume 0-100
     */
    public void setVolume(float volume) {
        float value = Math.abs(volume / 100);
        if (exoPlayer != null) {
            exoPlayer.setVolume(value);
        }
    }

    //播放
    public void start() {
        try {
            if (!isPlaying()) {
                exoPlayer.prepare();
                exoPlayer.play();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //暂停
    public void pause() {
        try {
            if (isPlaying()) {
                exoPlayer.pause();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //停止播放
    public void stop() {
        try {
            if (exoPlayer != null && isPlaying()) {
                exoPlayer.stop();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //释放资源
    public void release() {
        try {
            stop();
            if (exoPlayer != null) {
                exoPlayer.release();

                playerEventListenerList.clear();

                if (null != eventListener) {
                    exoPlayer.removeListener(eventListener);
                }
            }

            instance = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //播放or暂停,返回是否暂停
    public void playOrPause() {
        if (!isPlaying()) {
            start();
        } else {
            pause();
        }
    }

    public void seekTo(Long positionMs) {
        exoPlayer.seekTo(positionMs);
    }

    public long getDuration() {
        if (exoPlayer == null) {
            return 0;
        }
        return exoPlayer.getDuration();
    }

    //判断是否是播放状态
    public boolean isPlaying() {
        int playbackState = exoPlayer.getPlaybackState();
        return playbackState == ExoPlayer.STATE_READY && exoPlayer.getPlayWhenReady();
    }

    //监听回调
    private void initListener() {
        eventListener = new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int playbackState) {
                switch (playbackState) {
                    //玩家没有任何媒体可以播放
                    case Player.STATE_IDLE:
                    case Player.STATE_BUFFERING:
                        break;
                    case Player.STATE_READY:
                        if (!playerEventListenerList.isEmpty()) {
                            for (OnPlayerEventListener listener : playerEventListenerList) {
                                if (null != listener) {
                                    listener.onReady();
                                }
                            }
                        }
                        break;
                    case Player.STATE_ENDED:
                        if (!playerEventListenerList.isEmpty()) {
                            for (OnPlayerEventListener listener : playerEventListenerList) {
                                if (null != listener) {
                                    listener.onEnd();
                                }
                            }
                        }
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onRepeatModeChanged(int repeatMode) {

            }

            @Override
            public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {

            }

            @Override
            public void onPlayerError(@NotNull PlaybackException error) {
                if (!playerEventListenerList.isEmpty()) {
                    for (OnPlayerEventListener listener : playerEventListenerList) {
                        if (null != listener) {
                            listener.onError(error, error.getMessage());
                        }
                    }
                }
            }

            @Override
            public void onIsPlayingChanged(boolean isPlaying) {
                try {
                    if (!isPlaying) {
                        audioPlayerFactor.clearCallBacks();
                    } else {
                        audioPlayerFactor.updateTimeline(exoPlayer);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (!playerEventListenerList.isEmpty()) {
                    for (OnPlayerEventListener listener : playerEventListenerList) {
                        if (null != listener) {
                            listener.onPlayerStatus(
                                    isPlaying ? CURRENT_PLAYER_STATUS_PLAYER : CURRENT_PLAYER_STATUS_PAUSE);
                        }
                    }
                }
            }

            @Override
            public void onPositionDiscontinuity(
                    @NotNull Player.PositionInfo oldPosition,
                    @NotNull Player.PositionInfo newPosition,
                    int reason
            ) {
                if (!playerEventListenerList.isEmpty()) {
                    for (OnPlayerEventListener listener : playerEventListenerList) {
                        if (null != listener) {
                            listener.onPositionDiscontinuity(oldPosition, newPosition, reason);
                        }
                    }
                }
            }
        };
        exoPlayer.addListener(eventListener);

        audioPlayerFactor.setProgressUpdateListener((position, bufferedPosition) -> initProgressUpdateEvent(position));
    }

    private void initProgressUpdateEvent(long position) {
        try {
            if (!playerEventListenerList.isEmpty()) {
                long duration = exoPlayer.getDuration();
                BigDecimal positionBD = new BigDecimal(position);
                BigDecimal durationBD = new BigDecimal(duration);
                BigDecimal progressBD = positionBD.divide(durationBD, 4, BigDecimal.ROUND_HALF_UP);
                double progress = progressBD.doubleValue();

                for (OnPlayerEventListener listener : playerEventListenerList) {
                    if (null != listener) {
                        listener.onPlayerCurrentPosition(position, duration, progress);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void clearCallBacks() {
        audioPlayerFactor.clearCallBacks();
    }

    //回调
    public interface OnPlayerEventListener {
        default void onReady() {

        }

        default void onEnd() {

        }

        default void onError(PlaybackException exception, String msg) {

        }

        default void onPlayerStatus(int status) {

        }

        default void onPositionDiscontinuity(Player.PositionInfo oldPosition, Player.PositionInfo newPosition, int reason) {

        }

        default void onPlayerCurrentPosition(Long position, Long duration, Double progress) {

        }

        default void onPlayerPrevious(){

        }

        default void onPlayerNext(){

        }
    }

    private final List<OnPlayerEventListener> playerEventListenerList = new ArrayList<>();

    public void addPlayerEventListenerList(OnPlayerEventListener defaultEventListener) {
        this.playerEventListenerList.add(defaultEventListener);
    }

    public void removePlayerEventListener(OnPlayerEventListener onPlayerEventListener) {
        try {
            if (!playerEventListenerList.isEmpty()) {
                playerEventListenerList.remove(onPlayerEventListener);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void cleanDefaultEventListenerList() {
        playerEventListenerList.clear();
    }

    //设置播放倍速
    public void setPlayerSpeed(float speed) {
        currentPlayerSpeed = speed;
        exoPlayer.setPlaybackParameters(new PlaybackParameters(speed));
    }

    public float getCurrentPlayerSpeed() {
        return currentPlayerSpeed;
    }
}
