package com.maishuo.contentandroidsdk.ui

import androidx.annotation.ColorInt
import com.maishuo.contentandroidsdk.base.BaseFragment
import com.maishuo.contentandroidsdk.R

/**
 * author : xpSun
 * date : 12/14/21
 * description :
 */
class CommonEmptyFragment constructor(@ColorInt val color: Int? = null) : BaseFragment() {

    constructor() : this(null)

    override fun fetchRootViewById(): Int {
        return R.layout.fragment_common_empty_layout
    }

    override fun initWidgets() {
        if (null != color) {
            rootView?.setBackgroundColor(color)
        }
    }

    override fun initWidgetsEvent() {

    }
}