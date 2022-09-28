package com.maishuo.haohai.widgets.control

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageView
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.PlayerControlView
import com.maishuo.haohai.R
import com.maishuo.haohai.audio.AudioPlayerManager
import com.maishuo.haohai.utils.Utils
import com.qichuang.commonlibs.utils.GlideUtils
import com.qichuang.commonlibs.utils.ToastUtil

/**
 * author : xpSun
 * date : 11/17/21
 * description :
 */
class CustomPlayerControlView @JvmOverloads constructor(context: Context, attrs: AttributeSet?) :
    PlayerControlView(context, attrs, 0) {

    companion object {
        const val PLAYER_STYLE_MODULE_0 = 0
        const val PLAYER_STYLE_MODULE_1 = 1
    }

    private var playerList: ImageView? = null
    private var playerSpeed: TextView? = null
    private var playerPrev: ImageButton? = null
    private var playerNext: ImageButton? = null
    private var playerControlView: ImageButton? = null

    var onControlViewClickListener: OnControlViewClickListener? = null
    var isDisableEvent: Boolean? = null

    private var topLeftImage: AppCompatImageView? = null

    private var progressLayout: LinearLayout? = null
    private var controlViewLayout: LinearLayout? = null

    init {
        showTimeoutMs = 0
    }

    override fun setPlayer(player: Player?) {
        super.setPlayer(player)

        initWidgets()
        initWidgetEvent()
    }

    private fun initWidgets() {
        progressLayout = findViewById(R.id.exp_play_progress_layout)
        controlViewLayout = findViewById(R.id.exp_play_control_layout)

        playerList = findViewById(R.id.exo_player_list)
        playerSpeed = findViewById(R.id.exo_player_speed)
        playerPrev = findViewById(R.id.exo_prev)
        playerNext = findViewById(R.id.exo_next)
        playerControlView = findViewById(R.id.exo_play_control)
        topLeftImage = findViewById(R.id.exo_play_control_lite_top_image)

        setPlayerStyleModel(PLAYER_STYLE_MODULE_0)

        initCurrentPlayerStatus()
    }

    private fun initWidgetEvent() {
        playerList?.setOnClickListener {
            if (commonControlEventCheck()) {
                return@setOnClickListener
            }

            onControlViewClickListener?.onPlayerListClick(playerList)
        }

        playerSpeed?.setOnClickListener {
            if (commonControlEventCheck()) {
                return@setOnClickListener
            }

            onControlViewClickListener?.onPlayerSpeedClick(playerSpeed)
        }

        playerPrev?.setOnClickListener {
            if (commonControlEventCheck()) {
                return@setOnClickListener
            }

            if (player?.hasPreviousMediaItem() == true) {
                AudioPlayerManager.getInstance().seekToPrevious()
            } else {
                ToastUtil.showToast("已经是第一首了")
            }

            onControlViewClickListener?.onPlayerPrevClick(playerPrev)
        }

        playerNext?.setOnClickListener {
            if (commonControlEventCheck()) {
                return@setOnClickListener
            }

            if (player?.hasNextMediaItem() == true) {
                AudioPlayerManager.getInstance().seekToNext()
            } else {
                ToastUtil.showToast("已经到最后了")
            }

            onControlViewClickListener?.onPlayerNextClick(playerNext)
        }

        playerControlView?.setOnClickListener {
            if (isDisableEvent == true) {
                return@setOnClickListener
            }

            val isPlaying = player?.isPlaying
            if (isPlaying == true) {
                player?.pause()
            } else {
                if (player?.playbackState == Player.STATE_ENDED
                    && player?.hasNextMediaItem() == false
                ) {
                    player?.seekTo(0)
                }

                player?.prepare()
                player?.play()
            }

            onControlViewClickListener?.onPlayerControlViewClick(playerControlView)
        }

        player?.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                super.onIsPlayingChanged(isPlaying)

                initCurrentPlayerStatus()
            }
        })
    }

    private fun initCurrentPlayerStatus() {
        val isPlaying = player?.isPlaying
        if (isPlaying == true) {
            playerControlView?.setImageResource(R.mipmap.player_pause_icon)
        } else {
            playerControlView?.setImageResource(R.mipmap.player_start_icon)
        }
    }

    private fun commonControlEventCheck(): Boolean {
        if (isDisableEvent == true) {
            return true
        }

        if (!Utils.isFastClick()) {
            return true
        }

        return false
    }

    fun getPlayerModelView(): ImageView? {
        return null
    }

    fun getPlayerSpeedView(): TextView? {
        return playerSpeed
    }

    fun setPlayerStyleModel(model: Int?) {
        if (PLAYER_STYLE_MODULE_0 == model) {
            progressLayout?.apply {
                val mLayoutParams = layoutParams
                if (mLayoutParams is RelativeLayout.LayoutParams) {
                    mLayoutParams.topMargin = Utils.dpToPx(10)
                }
                layoutParams = mLayoutParams
            }
            controlViewLayout?.apply {
                val mLayoutParams = layoutParams
                if (mLayoutParams is RelativeLayout.LayoutParams) {
                    mLayoutParams.topMargin = Utils.dpToPx(40)
                }
                layoutParams = mLayoutParams
            }
            topLeftImage?.visibility = View.GONE
        } else {
            progressLayout?.apply {
                val mLayoutParams = layoutParams
                if (mLayoutParams is RelativeLayout.LayoutParams) {
                    mLayoutParams.topMargin = Utils.dpToPx(80)
                }
                layoutParams = mLayoutParams
            }
            controlViewLayout?.apply {
                val mLayoutParams = layoutParams
                if (mLayoutParams is RelativeLayout.LayoutParams) {
                    mLayoutParams.topMargin = Utils.dpToPx(10)
                }
                layoutParams = mLayoutParams
            }
            topLeftImage?.visibility = View.VISIBLE
        }
    }

    fun loadLiteTopControlImage(appCompatActivity: AppCompatActivity?, url: String?) {
        appCompatActivity?.let {
            GlideUtils.loadImage(it, url, topLeftImage!!)
        }
    }
}