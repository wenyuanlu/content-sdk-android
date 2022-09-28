package com.maishuo.haohai.widgets

import android.app.Activity
import android.text.TextPaint
import android.text.style.UnderlineSpan
import androidx.core.content.ContextCompat
import com.maishuo.haohai.R

/**
 * 无下划线的Span
 */
class NoUnderlineSpan constructor(val activity: Activity?, val color: Int? = null) : UnderlineSpan() {
    override fun updateDrawState(ds: TextPaint) {
        if (null == activity) {
            return
        }

        ds.color = ContextCompat.getColor(activity, color ?: R.color.color_009ae8)
        ds.isUnderlineText = false
    }
}