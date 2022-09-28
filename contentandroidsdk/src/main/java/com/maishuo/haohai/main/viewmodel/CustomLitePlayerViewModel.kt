package com.maishuo.haohai.main.viewmodel

import android.app.Dialog
import android.content.Intent
import android.text.TextUtils
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.corpize.sdk.ivoice.QCiVoiceSdk
import com.corpize.sdk.ivoice.QcCustomTemplateAttr
import com.corpize.sdk.ivoice.admanager.QcAdManager
import com.corpize.sdk.ivoice.listener.QcCustomTemplateListener
import com.google.android.exoplayer2.MediaItem
import com.maishuo.haohai.api.bean.DialogBottomMoreBean
import com.maishuo.haohai.api.response.GetListResponse
import com.maishuo.haohai.api.retrofit.ApiService
import com.maishuo.haohai.audio.AudioPlayerManager
import com.maishuo.haohai.audio.PlayerSpeedEnum
import com.maishuo.haohai.common.Constant
import com.maishuo.haohai.listener.OnCommonBottomDialogItemClickListener
import com.maishuo.haohai.main.event.GetProgramListResponseEvent
import com.maishuo.haohai.main.lite.CustomLitePlayerBottomDialog
import com.maishuo.haohai.main.lite.CustomLitePlayerFragment
import com.maishuo.haohai.person.ui.PlayHistoryActivity
import com.maishuo.haohai.utils.CustomDownCountUtils
import com.maishuo.haohai.utils.DialogUtils
import com.maishuo.haohai.utils.MediaPlayerUtils
import com.qichuang.commonlibs.basic.BaseViewModel
import com.qichuang.commonlibs.common.PreferencesKey
import com.qichuang.commonlibs.utils.CustomPreferencesUtils
import com.qichuang.commonlibs.utils.LoggerUtils
import com.qichuang.commonlibs.utils.NetUtils
import com.qichuang.commonlibs.utils.PreferencesUtils
import com.qichuang.retrofitlibs.retrofit.CommonObserver
import java.math.BigDecimal

/**
 * author : xpSun
 * date : 2022/3/14
 * description :
 */
class CustomLitePlayerViewModel constructor(private val fragment: CustomLitePlayerFragment?) :
    BaseViewModel() {

    var albumId: Int? = null
    var programId: Int? = null
    var authorName: String? = null
    var company: String? = null
    var albumType: Int? = null
    var getProgramListParamEvent: GetProgramListResponseEvent? = null

    private var adTitle: String? = null

    fun hide() {
        fragment?.hideFragment()
    }

    fun openHistory() {
        val activity = fragment?.activity
        if (activity is AppCompatActivity) {
            activity.startActivity(Intent(activity, PlayHistoryActivity::class.java))
        }
    }

    fun openCatalogue() {
        val activity = fragment?.activity
        if (activity is AppCompatActivity) {
            val dialog = CustomLitePlayerBottomDialog(activity)
            dialog.responses = getProgramListParamEvent
            dialog.showDialog()
        }
    }

    fun scrollToBottom() {
        fragment?.scrollToBottom()
    }

    //处理播放列表
    fun setAudioPlayerList(list: MutableList<GetListResponse>?, position: Int?) {
        val mediaItems: MutableList<MediaItem> = MediaPlayerUtils.responseToMedia(albumType, list)

        val audio = AudioPlayerManager.getInstance()
        audio?.setAudios(mediaItems, position ?: 0)
        audio?.start()

        putListenHistory()
    }

    //处理联网状态下的广告
    fun addConnectedNetWorkAd(
        adId: String?,
        listener: ((adManager: QcAdManager?, view: View?, title: String?) -> Unit)?
    ) {
        val progress = fetchAdPlayerProgress()
        QCiVoiceSdk.get().addCustomTemplateAd(
            fragment?.activity,
            fetchAdAttr(),
            adId,
            null,
            Constant.response?.company,
            progress,
            CustomTemplateAdListener(listener)
        )
    }

    private fun fetchAdPlayerProgress(): Int {
        val currentProgress = (CustomDownCountUtils.instance.currentProgress ?: 0) / 1000
        val isRunning = CustomDownCountUtils.instance.isRunning

        val progress = if (null == CustomDownCountUtils.instance.currentProgress
            || (0 >= currentProgress && !isRunning)
        ) {
            0
        } else {
            val mDuration = CustomDownCountUtils.duration ?: 0
            val mInteraction = CustomDownCountUtils.interaction ?: 0

            if (0 != mInteraction) {//此时已经播放结束品宣,进行互动音频的播放
                mDuration + currentProgress
            } else {
                if (0L == currentProgress && isRunning) {
                    mDuration
                } else {
                    currentProgress.toInt()
                }
            }
        }

        LoggerUtils.e("progress:${progress}")
        return progress.toInt()
    }


    private fun fetchAdAttr(): QcCustomTemplateAttr {
        val attr = QcCustomTemplateAttr()
        //设置封面属性,单位dp,设置MATCH_PARENT 则交由外部容器控制
        val coverStyle = QcCustomTemplateAttr.CoverStyle()
        coverStyle.width = RelativeLayout.LayoutParams.MATCH_PARENT
        coverStyle.height = RelativeLayout.LayoutParams.MATCH_PARENT
        coverStyle.radius = 20 //设置封面圆角,单位dp

        attr.coverStyle = coverStyle

        //设置广告icon,单位dp
        val iconStyle = QcCustomTemplateAttr.IconStyle()
        iconStyle.width = 30
        iconStyle.height = 30
        iconStyle.radius = 10 //设置icon圆角,单位dp

        iconStyle.layoutGravity = Gravity.BOTTOM //设置icon位置,具体参见Gravity方法

        iconStyle.isEnableMargin = true
        iconStyle.marginLeft = 15
        iconStyle.marginBottom = 13

        attr.iconStyle = iconStyle
        attr.isEnableSkip = true //是否启用右上角跳过,默认启用
        attr.isSkipAutoClose = false //设置倒计时结束后是否自动关闭广告,默认false
        attr.skipTipValue = "关闭" //设置右上角跳过文案,默认为跳过

        return attr
    }


    //自定义模板广告的回调
    private inner class CustomTemplateAdListener constructor(
        val listener: ((adManager: QcAdManager?, view: View?, title: String?) -> Unit)?
    ) : QcCustomTemplateListener {
        override fun onAdClick() {
            listener?.invoke(null, null, null)
        }

        override fun onAdCompletion() {
            listener?.invoke(null, null, null)
        }

        override fun onAdError(message: String?) {
            LoggerUtils.e("onAdError:${message}")
            listener?.invoke(null, null, null)
        }

        override fun onAdReceive(adManager: QcAdManager?, view: View?) {
            if (fragment?.activity?.isDestroyed == true) {
                listener?.invoke(null, null, null)
            } else {
                listener?.invoke(adManager, view, adTitle)
            }
        }

        override fun onAdExposure() {}

        override fun fetchMainTitle(title: String?) {
            adTitle = title
        }

        override fun onAdSkipClick() {
            listener?.invoke(null, null, null)
        }

        override fun onFetchAdContentView(
            adTipView: TextView?,//左上角 广告 标识
            skipLayout: LinearLayout?,//右上角跳过布局
            mainTitleView: TextView?,//下方主标题
            subtitleView: TextView?,//icon 右侧副标题
            understandDescView: TextView?//右下角了解详情
        ) {
            mainTitleView?.textSize = 14f
            subtitleView?.textSize = 12f

            subtitleView?.maxEms = 10
            subtitleView?.maxLines = 2
        }

        override fun onPlayCompletionListener() {

        }
    }


    fun checkPlayerPositionAd(progress: Double?): Boolean {
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

            if (Constant.onADShowStatus != Constant.ON_AD_SHOW_STATUS_0) {
                return false
            }

            if (Constant.HEADER_LINES_TAG == Constant.response?.album_type
                && Constant.response?.album_id == Constant.currentPlayerAdPositionId
            ) {
                return false
            }

            if (Constant.HEADER_LINES_TAG != Constant.response?.album_type
                && Constant.response?.program_id == Constant.currentPlayerAdPositionId
            ) {
                return false
            }

            if (Constant.HEADER_LINES_TAG == Constant.response?.album_type) {
                val linesAdInterval =
                    PreferencesUtils.getInt(PreferencesKey.TOUTIAO_PLAY_NUM_INTERVAL)
                if (0 != Constant.currentChildInPlayerListPosition
                    && (Constant.currentChildInPlayerListPosition ?: 0) % linesAdInterval.inc() != 0
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


    fun checkCurrentPlayerData(): Boolean {
        val currentPlayerGroupId = CustomPreferencesUtils.fetchCurrentPlayerGroupId()

        if (0 == currentPlayerGroupId) {
            return false
        }

        if (Constant.response?.album_id != currentPlayerGroupId) {
            return false
        }

        val mediaItemId = AudioPlayerManager.getInstance()?.currentMediaItemId
        if (TextUtils.isEmpty(mediaItemId)) {
            return false
        }

        if (Constant.HEADER_LINES_TAG != Constant.response?.album_type) {
            if (null != Constant.response?.program_id &&
                mediaItemId != Constant.response?.program_id.toString()
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

    //收听记录上报
    private fun putListenHistory() {
        if (!NetUtils.isNetworkConnected(fragment?.activity)) {
            return
        }

        if (Constant.HEADER_LINES_TAG == albumType) {
            ApiService.instance.putNewsListenHistory(albumId)
                .subscribe(object : CommonObserver<Any>() {
                    override fun onResponseSuccess(response: Any?) {}
                })
        } else {
            if (null == programId) {
                return
            }
            ApiService.instance.putListenHistory(albumId, programId)
                .subscribe(object : CommonObserver<Any>() {
                    override fun onResponseSuccess(response: Any?) {}

                    override fun onResponseError(message: String?, e: Throwable?, code: Int?) {}
                })
        }
    }

    //设置倍速
    fun setPlayerSpeed(speed: ((speed: String?) -> Unit)?) {
        val audioPlayerManager = AudioPlayerManager.getInstance()

        val speeds: MutableList<DialogBottomMoreBean> = mutableListOf()
        val speedArray = PlayerSpeedEnum.values()

        if (!speedArray.isNullOrEmpty()) {
            for (item in speedArray.iterator()) {
                val dialogBean = DialogBottomMoreBean()
                dialogBean.text = if (1.0f == item.value)
                    "正常"
                else
                    item.rewType
                dialogBean.isSelect = audioPlayerManager.currentPlayerSpeed == item.value
                speeds.add(dialogBean)
            }
        }

        fragment?.activity?.let { fragmentIt ->
            DialogUtils.showBottomMoreDialog(
                fragmentIt as AppCompatActivity,
                speeds,
                object : OnCommonBottomDialogItemClickListener {
                    override fun onItemClick(itemView: View?, position: Int, dialog: Dialog?) {
                        audioPlayerManager.setPlayerSpeed(speedArray[position].value)
                        speed?.invoke(speeds[position].text ?: "")
                        dialog?.dismiss()
                    }
                })
        }
    }
}