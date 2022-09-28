package com.maishuo.contentandroidsdk.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import com.maishuo.contentandroidsdk.utils.LoggerUtils

/**
 * author : xpSun
 * date : 12/14/21
 * description :
 */
abstract class BaseFragment : Fragment(), IWidgetsListener {

    var rootView: View? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val rootViewId = fetchRootViewById()
        rootView = inflater.inflate(rootViewId, container, false)
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initWidgets()
        initWidgetsEvent()
    }

    fun commitFragment(@IdRes layoutId: Int, fragment: Fragment?) {
        if (null == fragment) {
            return
        }

        val fragmentTransaction = childFragmentManager.beginTransaction()
        if (!fragment.isAdded) {
            fragmentTransaction.add(layoutId, fragment)
            fragmentTransaction.commitNow()
        } else {
            val fragments = childFragmentManager.fragments

            if (!fragments.isNullOrEmpty()) {
                for (item in fragments.iterator()) {
                    fragmentTransaction.hide(item)
                }
            }

            fragmentTransaction.show(fragment)
            fragmentTransaction.commitNow()
        }
    }

    override fun onStart() {
        super.onStart()
        LoggerUtils.e("onStart")
    }

    override fun onResume() {
        super.onResume()
        LoggerUtils.e("onResume")
    }

    override fun onPause() {
        super.onPause()
        LoggerUtils.e("onPause")
    }

    override fun onStop() {
        super.onStop()
        LoggerUtils.e("onStop")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        LoggerUtils.e("onDestroyView")
    }

    override fun onDestroy() {
        super.onDestroy()
        LoggerUtils.e("onDestroy")
    }

    override fun onDetach() {
        super.onDetach()
        LoggerUtils.e("onDetach")
    }
}