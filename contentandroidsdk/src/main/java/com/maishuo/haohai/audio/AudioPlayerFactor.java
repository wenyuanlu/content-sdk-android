package com.maishuo.haohai.audio;

import android.os.Handler;
import android.os.Looper;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.ui.PlayerControlView;
import com.google.android.exoplayer2.util.Assertions;
import com.google.android.exoplayer2.util.Util;

import java.util.Arrays;

/**
 * author : xpSun
 * date : 12/15/21
 * description :
 */
public class AudioPlayerFactor {

    private long currentWindowOffset;
    private final Timeline.Window window = new Timeline.Window();
    private final Timeline.Period period = new Timeline.Period();
    private long[] adGroupTimesMs = new long[0];
    private long currentPosition;
    private long currentBufferedPosition;
    private PlayerControlView.ProgressUpdateListener progressUpdateListener;
    private final Runnable updateProgressAction = () -> updateProgress(AudioPlayerManager.getInstance().getMediaPlayer());
    private static final int MAX_UPDATE_INTERVAL_MS = 1000;
    private final Handler handler = new Handler(Looper.getMainLooper());

    public void setProgressUpdateListener(PlayerControlView.ProgressUpdateListener progressUpdateListener) {
        this.progressUpdateListener = progressUpdateListener;
    }

    public void updateTimeline(Player player) {

        if (player == null) {
            return;
        }

        currentWindowOffset = 0;
        long durationUs = 0;
        int adGroupCount = 0;
        Timeline timeline = player.getCurrentTimeline();
        if (!timeline.isEmpty()) {
            int currentWindowIndex = player.getCurrentMediaItemIndex();
            for (int i = currentWindowIndex; currentWindowIndex >= i; i++) {
                if (i == currentWindowIndex) {
                    currentWindowOffset = Util.usToMs(durationUs);
                }
                timeline.getWindow(i, window);
                if (window.durationUs == C.TIME_UNSET) {
                    Assertions.checkState(true);
                    break;
                }
                for (int j = window.firstPeriodIndex; j <= window.lastPeriodIndex; j++) {
                    timeline.getPeriod(j, period);
                    int removedGroups = period.getRemovedAdGroupCount();
                    int totalGroups = period.getAdGroupCount();
                    for (int adGroupIndex = removedGroups; adGroupIndex < totalGroups; adGroupIndex++) {
                        long adGroupTimeInPeriodUs = period.getAdGroupTimeUs(adGroupIndex);
                        if (adGroupTimeInPeriodUs == C.TIME_END_OF_SOURCE) {
                            if (period.durationUs == C.TIME_UNSET) {
                                // Don't show ad markers for postrolls in periods with unknown duration.
                                continue;
                            }
                            adGroupTimeInPeriodUs = period.durationUs;
                        }
                        long adGroupTimeInWindowUs = adGroupTimeInPeriodUs + period.getPositionInWindowUs();
                        if (adGroupTimeInWindowUs >= 0) {
                            if (adGroupCount == adGroupTimesMs.length) {
                                int newLength = adGroupTimesMs.length == 0 ? 1 : adGroupTimesMs.length * 2;
                                adGroupTimesMs = Arrays.copyOf(adGroupTimesMs, newLength);
                            }
                            adGroupTimesMs[adGroupCount] = Util.usToMs(durationUs + adGroupTimeInWindowUs);
                            adGroupCount++;
                        }
                    }
                }
                durationUs += window.durationUs;
            }
        }

        updateProgress(player);
    }

    private void updateProgress(Player player) {
        long position = 0;
        long bufferedPosition = 0;
        if (player != null) {
            position = currentWindowOffset + player.getContentPosition();
            bufferedPosition = currentWindowOffset + player.getContentBufferedPosition();
        }
        boolean positionChanged = position != currentPosition;
        boolean bufferedPositionChanged = bufferedPosition != currentBufferedPosition;
        currentPosition = position;
        currentBufferedPosition = bufferedPosition;

        if (progressUpdateListener != null && (positionChanged || bufferedPositionChanged)) {
            progressUpdateListener.onProgressUpdate(position, bufferedPosition);
        }

        // Cancel any pending updates and schedule a new one if necessary.
        handler.removeCallbacks(updateProgressAction);
        int playbackState = player == null ? Player.STATE_IDLE : player.getPlaybackState();
        if (player != null && player.isPlaying()) {
            long mediaTimeDelayMs = MAX_UPDATE_INTERVAL_MS;

            // Limit delay to the start of the next full second to ensure position display is smooth.
            long mediaTimeUntilNextFullSecondMs = 1000 - position % 1000;
            mediaTimeDelayMs = Math.min(mediaTimeDelayMs, mediaTimeUntilNextFullSecondMs);

            // Calculate the delay until the next update in real time, taking playback speed into account.
            float playbackSpeed = player.getPlaybackParameters().speed;
            long delayMs =
                    playbackSpeed > 0 ? (long) (mediaTimeDelayMs / playbackSpeed) : MAX_UPDATE_INTERVAL_MS;

            // Constrain the delay to avoid too frequent / infrequent updates.
            delayMs = Util.constrainValue(delayMs, MAX_UPDATE_INTERVAL_MS, MAX_UPDATE_INTERVAL_MS);
            handler.postDelayed(updateProgressAction, delayMs);
        } else if (playbackState != Player.STATE_ENDED && playbackState != Player.STATE_IDLE) {
            handler.postDelayed(updateProgressAction, MAX_UPDATE_INTERVAL_MS);
        }
    }

    public void clearCallBacks() {
        handler.removeCallbacks(updateProgressAction);
    }
}
