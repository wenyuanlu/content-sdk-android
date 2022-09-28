package com.qichuang.commonlibs.basic

import android.view.View
import androidx.viewbinding.ViewBinding

/**
 * author : xpSun
 * date : 11/8/21
 * description :
 */
abstract class CustomBaseFragment<T : ViewBinding?> : BaseFragment(), CustomInitRootViewListener {

    var vb: T? = null

    override fun fetchRootView(): View? {
        vb = rootViewByReflect<T>(layoutInflater)
        return vb?.root
    }
}