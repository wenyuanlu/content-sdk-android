package com.qichuang.commonlibs.basic

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

/**
 * author : xpSun
 * date : 11/5/21
 * description :
 */
abstract class BaseFragment constructor(): IBasicFragment(),
    BasicCommonWidgetListener, ClickActionListener {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return fetchRootView()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        initWidgets()
        initWidgetsEvent()
    }

    override fun isShowCustomTitle(visibility: Boolean?) {

    }

    override fun onDestroyView() {
        super.onDestroyView()
    }
}