package com.custom.appdemo.widgets

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView

/**
 * author : xpSun
 * date : 2022/3/9
 * description :
 */
class HorizontalTextview @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int? = null
) : AppCompatTextView(context, attrs, defStyleAttr ?: 0) {

    override fun isFocused(): Boolean = true

}