package com.qichuang.commonlibs.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.maishuo.haohai.R
import com.qichuang.commonlibs.basic.CustomBaseAdapter

/**
 * author : xpSun
 * date : 2021/4/10
 * description :
 */
class CommonRecyclerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) :
    RecyclerView(context, attrs, defStyleAttr) {

    private var onRefreshClickListener: (() -> Unit)? = null
    var isEnableEmptyView: Boolean = true

    fun setOnRefreshClickListener(onRefreshClickListener: (() -> Unit)?) {
        this.onRefreshClickListener = onRefreshClickListener
    }

    //关闭默认局部刷新动画
    @JvmOverloads
    fun closeDefaultAnimator(mRecyclerView: RecyclerView? = this) {
        if (null == mRecyclerView) return
        mRecyclerView.itemAnimator?.addDuration = 0
        mRecyclerView.itemAnimator?.changeDuration = 0
        mRecyclerView.itemAnimator?.moveDuration = 0
        mRecyclerView.itemAnimator?.removeDuration = 0

        (mRecyclerView.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
    }

    override fun setAdapter(adapter: Adapter<*>?) {
        super.setAdapter(adapter)

        try {
            if (adapter is CustomBaseAdapter<*, *> && isEnableEmptyView) {
                val emptyView = LayoutInflater.from(context)
                    .inflate(
                        R.layout.view_common_empty_layout,
                        this,
                        false
                    )

                emptyView.setOnClickListener {
                    onRefreshClickListener?.invoke()
                }

                adapter.setEmptyView(emptyView)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}