package com.qichuang.commonlibs.basic

import android.view.View

/**
 * author : xpSun
 * date : 2021/3/16
 * description :
 */
interface BasicCommonWidgetListener : BasicCommonListener {

    fun fetchRootView(): View?

    fun initWidgets()

    fun initWidgetsEvent()

    fun isShowCustomTitle(visibility: Boolean? = false)

}