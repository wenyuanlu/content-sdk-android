package com.qichuang.commonlibs.widgets.refresh

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.fragment.app.FragmentActivity
import com.maishuo.haohai.R
import com.qichuang.commonlibs.utils.LoggerUtils
import com.scwang.smart.drawable.ProgressDrawable
import com.scwang.smart.refresh.classics.ArrowDrawable
import com.scwang.smart.refresh.classics.ClassicsAbstract
import com.scwang.smart.refresh.layout.api.RefreshHeader
import com.scwang.smart.refresh.layout.api.RefreshLayout
import com.scwang.smart.refresh.layout.constant.RefreshState
import com.scwang.smart.refresh.layout.constant.SpinnerStyle
import com.scwang.smart.refresh.layout.util.SmartUtil
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

/**
 * 经典下拉头部
 *
 */
class CommonClassicsHeader @JvmOverloads constructor(context: Context?, attrs: AttributeSet? = null) :
        ClassicsAbstract<CommonClassicsHeader>(context, attrs, 0), RefreshHeader {

    private var KEY_LAST_UPDATE_TIME = "LAST_UPDATE_TIME"
    private var mLastTime: Date? = null
    private var mLastUpdateText: TextView? = null
    private var mShared: SharedPreferences? = null
    private var mLastUpdateFormat: DateFormat? = null
    private var mEnableLastTime = true

    //"下拉可以刷新";
    private var mTextPulling: String? = null

    //"正在刷新...";
    private var mTextRefreshing: String? = null

    //"正在加载...";
    private var mTextLoading: String? = null

    //"释放立即刷新";
    private var mTextRelease: String? = null

    //"刷新完成";
    private var mTextFinish: String? = null

    //"刷新失败";
    private var mTextFailed: String? = null

    //"上次更新 M-d HH:mm";
    private var mTextUpdate: String? = null

    //"释放进入二楼";
    private var mTextSecondary: String? = null

    override fun onFinish(layout: RefreshLayout, success: Boolean): Int {
        if (success) {
            mTitleText.text = mTextFinish
            if (mLastTime != null) {
                setLastUpdateTime(Date())
            }
        } else {
            mTitleText.text = mTextFailed
        }
        return super.onFinish(layout, success) //延迟500毫秒之后再弹回
    }

    override fun onStateChanged(refreshLayout: RefreshLayout, oldState: RefreshState, newState: RefreshState) {
        val arrowView: View = mArrowView
        val updateView: View? = mLastUpdateText
        when (newState) {
            RefreshState.None -> {
                updateView?.visibility = if (mEnableLastTime) View.VISIBLE else View.GONE
                mTitleText.text = mTextPulling
                arrowView.visibility = View.VISIBLE
                arrowView.animate().rotation(0f)
            }
            RefreshState.PullDownToRefresh -> {
                mTitleText.text = mTextPulling
                arrowView.visibility = View.VISIBLE
                arrowView.animate().rotation(0f)
            }
            RefreshState.Refreshing, RefreshState.RefreshReleased -> {
                mTitleText.text = mTextRefreshing
                arrowView.visibility = View.GONE
            }
            RefreshState.ReleaseToRefresh -> {
                mTitleText.text = mTextRelease
                arrowView.animate().rotation(180f)
            }
            RefreshState.ReleaseToTwoLevel -> {
                mTitleText.text = mTextSecondary
                arrowView.animate().rotation(0f)
            }
            RefreshState.Loading -> {
                arrowView.visibility = View.GONE
                updateView?.visibility = if (mEnableLastTime) View.INVISIBLE else View.GONE
                mTitleText.text = mTextLoading
            }
        }
    }

    private fun setLastUpdateTime(time: Date): CommonClassicsHeader {
        val thisView: View = this
        mLastTime = time
        mLastUpdateText?.text = mLastUpdateFormat?.format(time)
        if (mShared != null && !thisView.isInEditMode) {
            mShared?.edit()?.putLong(KEY_LAST_UPDATE_TIME, time.time)?.apply()
        }
        return this
    }

    fun setTimeFormat(format: DateFormat): CommonClassicsHeader {
        mLastUpdateFormat = format
        if (null != mLastTime) {
            mLastUpdateText?.text = mLastUpdateFormat?.format(mLastTime)
        }
        return this
    }

    fun setLastUpdateText(text: CharSequence?): CommonClassicsHeader {
        mLastTime = null
        mLastUpdateText?.text = text
        return this
    }

    override fun setAccentColor(@ColorInt accentColor: Int): CommonClassicsHeader {
        mLastUpdateText?.setTextColor(accentColor and 0x00ffffff or -0x34000000)
        return super.setAccentColor(accentColor)
    }

    fun setEnableLastTime(enable: Boolean): CommonClassicsHeader {
        val updateView: View? = mLastUpdateText
        mEnableLastTime = enable
        updateView?.visibility = if (enable) View.VISIBLE else View.GONE
        if (mRefreshKernel != null) {
            mRefreshKernel.requestRemeasureHeightFor(this)
        }
        return this
    }

    fun setTextSizeTime(size: Float): CommonClassicsHeader {
        mLastUpdateText?.textSize = size
        if (mRefreshKernel != null) {
            mRefreshKernel.requestRemeasureHeightFor(this)
        }
        return this
    }

    fun setTextTimeMarginTop(dp: Float): CommonClassicsHeader {
        val updateView: View? = mLastUpdateText
        val lp = updateView?.layoutParams as MarginLayoutParams
        lp.topMargin = SmartUtil.dp2px(dp)
        updateView.layoutParams = lp
        return this
    }

    companion object {
        val ID_TEXT_UPDATE = R.id.srl_classics_update
        var REFRESH_HEADER_PULLING: String? = null //"下拉可以刷新";
        var REFRESH_HEADER_REFRESHING: String? = null //"正在刷新...";
        var REFRESH_HEADER_LOADING: String? = null //"正在加载...";
        var REFRESH_HEADER_RELEASE: String? = null //"释放立即刷新";
        var REFRESH_HEADER_FINISH: String? = null //"刷新完成";
        var REFRESH_HEADER_FAILED: String? = null //"刷新失败";
        var REFRESH_HEADER_UPDATE: String? = null //"上次更新 M-d HH:mm";
        var REFRESH_HEADER_SECONDARY: String? = null //"释放进入二楼";
    }

    init {
        initWidgets(attrs)
    }

    private fun initWidgets(attrs: AttributeSet?) {
        View.inflate(context, R.layout.view_common_classics_header_layout, this)
        val thisView: View? = this
        mArrowView = thisView?.findViewById(R.id.srl_classics_arrow)
        val arrowView: View? = mArrowView
        mLastUpdateText = thisView?.findViewById(R.id.srl_classics_update)
        val updateView: View? = mLastUpdateText
        mProgressView = thisView?.findViewById(R.id.srl_classics_progress)
        val progressView: View? = mProgressView
        mTitleText = thisView?.findViewById(R.id.srl_classics_title)

        val ta = context.obtainStyledAttributes(attrs, R.styleable.ClassicsHeader)
        val lpArrow = arrowView?.layoutParams as LayoutParams
        val lpProgress = progressView?.layoutParams as LayoutParams
        val lpUpdateText = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        lpUpdateText.topMargin = ta.getDimensionPixelSize(R.styleable.ClassicsHeader_srlTextTimeMarginTop, SmartUtil.dp2px(0f))
        lpProgress.rightMargin = ta.getDimensionPixelSize(R.styleable.ClassicsFooter_srlDrawableMarginRight, SmartUtil.dp2px(20f))
        lpArrow.rightMargin = lpProgress.rightMargin
        lpArrow.width = ta.getLayoutDimension(R.styleable.ClassicsHeader_srlDrawableArrowSize, lpArrow.width)
        lpArrow.height = ta.getLayoutDimension(R.styleable.ClassicsHeader_srlDrawableArrowSize, lpArrow.height)
        lpProgress.width = ta.getLayoutDimension(R.styleable.ClassicsHeader_srlDrawableProgressSize, lpProgress.width)
        lpProgress.height = ta.getLayoutDimension(R.styleable.ClassicsHeader_srlDrawableProgressSize, lpProgress.height)
        lpArrow.width = ta.getLayoutDimension(R.styleable.ClassicsHeader_srlDrawableSize, lpArrow.width)
        lpArrow.height = ta.getLayoutDimension(R.styleable.ClassicsHeader_srlDrawableSize, lpArrow.height)
        lpProgress.width = ta.getLayoutDimension(R.styleable.ClassicsHeader_srlDrawableSize, lpProgress.width)
        lpProgress.height = ta.getLayoutDimension(R.styleable.ClassicsHeader_srlDrawableSize, lpProgress.height)
        mFinishDuration = ta.getInt(R.styleable.ClassicsHeader_srlFinishDuration, mFinishDuration)
        mEnableLastTime = ta.getBoolean(R.styleable.ClassicsHeader_srlEnableLastTime, mEnableLastTime)
        mSpinnerStyle = SpinnerStyle.values[ta.getInt(R.styleable.ClassicsHeader_srlClassicsSpinnerStyle, mSpinnerStyle.ordinal)]

        if (ta.hasValue(R.styleable.ClassicsHeader_srlDrawableArrow)) {
            mArrowView.setImageDrawable(ta.getDrawable(R.styleable.ClassicsHeader_srlDrawableArrow))
        } else if (mArrowView.drawable == null) {
            mArrowDrawable = ArrowDrawable()
            mArrowDrawable.setColor(-0x99999a)
            mArrowView.setImageDrawable(mArrowDrawable)
        }
        if (ta.hasValue(R.styleable.ClassicsHeader_srlDrawableProgress)) {
            mProgressView.setImageDrawable(ta.getDrawable(R.styleable.ClassicsHeader_srlDrawableProgress))
        } else if (mProgressView.drawable == null) {
            mProgressDrawable = ProgressDrawable()
            mProgressDrawable.setColor(-0x99999a)
            mProgressView.setImageDrawable(mProgressDrawable)
        }

        if (ta.hasValue(R.styleable.ClassicsHeader_srlTextSizeTitle)) {
            mTitleText.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                    ta.getDimensionPixelSize(R.styleable.ClassicsHeader_srlTextSizeTitle,
                            SmartUtil.dp2px(16f)).toFloat())
        }
        if (ta.hasValue(R.styleable.ClassicsHeader_srlTextSizeTime)) {
            mLastUpdateText?.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                    ta.getDimensionPixelSize(R.styleable.ClassicsHeader_srlTextSizeTime,
                            SmartUtil.dp2px(12f)).toFloat())
        }
        if (ta.hasValue(R.styleable.ClassicsHeader_srlPrimaryColor)) {
            super.setPrimaryColor(ta.getColor(R.styleable.ClassicsHeader_srlPrimaryColor, 0))
        }
        if (ta.hasValue(R.styleable.ClassicsHeader_srlAccentColor)) {
            setAccentColor(ta.getColor(R.styleable.ClassicsHeader_srlAccentColor, 0))
        }

        mTextPulling = when {
            ta.hasValue(R.styleable.ClassicsHeader_srlTextPulling) -> {
                ta.getString(R.styleable.ClassicsHeader_srlTextPulling)
            }
            REFRESH_HEADER_PULLING != null -> {
                REFRESH_HEADER_PULLING
            }
            else -> {
                context.getString(R.string.srl_header_pulling)
            }
        }
        mTextLoading = when {
            ta.hasValue(R.styleable.ClassicsHeader_srlTextLoading) -> {
                ta.getString(R.styleable.ClassicsHeader_srlTextLoading)
            }
            REFRESH_HEADER_LOADING != null -> {
                REFRESH_HEADER_LOADING
            }
            else -> {
                context.getString(R.string.srl_header_loading)
            }
        }
        mTextRelease = when {
            ta.hasValue(R.styleable.ClassicsHeader_srlTextRelease) -> {
                ta.getString(R.styleable.ClassicsHeader_srlTextRelease)
            }
            REFRESH_HEADER_RELEASE != null -> {
                REFRESH_HEADER_RELEASE
            }
            else -> {
                context.getString(R.string.srl_header_release)
            }
        }
        mTextFinish = when {
            ta.hasValue(R.styleable.ClassicsHeader_srlTextFinish) -> {
                ta.getString(R.styleable.ClassicsHeader_srlTextFinish)
            }
            REFRESH_HEADER_FINISH != null -> {
                REFRESH_HEADER_FINISH
            }
            else -> {
                context.getString(R.string.srl_header_finish)
            }
        }
        mTextFailed = when {
            ta.hasValue(R.styleable.ClassicsHeader_srlTextFailed) -> {
                ta.getString(R.styleable.ClassicsHeader_srlTextFailed)
            }
            REFRESH_HEADER_FAILED != null -> {
                REFRESH_HEADER_FAILED
            }
            else -> {
                context.getString(R.string.srl_header_failed)
            }
        }
        mTextSecondary = when {
            ta.hasValue(R.styleable.ClassicsHeader_srlTextSecondary) -> {
                ta.getString(R.styleable.ClassicsHeader_srlTextSecondary)
            }
            REFRESH_HEADER_SECONDARY != null -> {
                REFRESH_HEADER_SECONDARY
            }
            else -> {
                context.getString(R.string.srl_header_secondary)
            }
        }
        mTextRefreshing = when {
            ta.hasValue(R.styleable.ClassicsHeader_srlTextRefreshing) -> {
                ta.getString(R.styleable.ClassicsHeader_srlTextRefreshing)
            }
            REFRESH_HEADER_REFRESHING != null -> {
                REFRESH_HEADER_REFRESHING
            }
            else -> {
                context.getString(R.string.srl_header_refreshing)
            }
        }
        mTextUpdate = when {
            ta.hasValue(R.styleable.ClassicsHeader_srlTextUpdate) -> {
                ta.getString(R.styleable.ClassicsHeader_srlTextUpdate)
            }
            REFRESH_HEADER_UPDATE != null -> {
                REFRESH_HEADER_UPDATE
            }
            else -> {
                context.getString(R.string.srl_header_update)
            }
        }
        mLastUpdateFormat = SimpleDateFormat(mTextUpdate, Locale.getDefault())
        ta.recycle()
        progressView.animate().interpolator = null
        updateView?.visibility = if (mEnableLastTime) View.VISIBLE else View.GONE
        mTitleText.text = if (thisView?.isInEditMode == false) mTextRefreshing else mTextPulling
        if (thisView?.isInEditMode == false) {
            arrowView.visibility = View.GONE
        } else {
            progressView.visibility = View.GONE
        }

        try { //try 不能删除-否则会出现兼容性问题
            if (context is FragmentActivity) {
                val fragmentActivity: FragmentActivity = context as FragmentActivity
                val manager = fragmentActivity.supportFragmentManager
                if (manager != null) {
                    @SuppressLint("RestrictedApi")
                    val fragments = manager.fragments
                    if (fragments.size > 0) {
                        setLastUpdateTime(Date())
                        return
                    }
                }
            }
        } catch (e: Throwable) {
            LoggerUtils.e(e.toString())
        }
        KEY_LAST_UPDATE_TIME += context.javaClass.name
        mShared = context.getSharedPreferences("ClassicsHeader", Context.MODE_PRIVATE)
        setLastUpdateTime(Date(mShared?.getLong(KEY_LAST_UPDATE_TIME, System.currentTimeMillis())
                ?: 0L))
    }
}