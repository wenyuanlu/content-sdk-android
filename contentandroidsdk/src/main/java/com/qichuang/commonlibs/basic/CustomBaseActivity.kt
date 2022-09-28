package com.qichuang.commonlibs.basic

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import androidx.annotation.StringRes
import androidx.viewbinding.ViewBinding
import com.gyf.immersionbar.ImmersionBar
import com.maishuo.haohai.R
import com.qichuang.commonlibs.utils.PreferencesUtils

abstract class CustomBaseActivity<T : ViewBinding?> : BaseActivity(), CustomInitRootViewListener {

    var vb: T? = null

    override fun fetchRootView(): View? {
        vb = rootViewByReflect<T>(layoutInflater)
        return vb?.root
    }

    /**
     * 设置标题栏的标题
     */
    override fun setTitle(@StringRes id: Int) {
        setTitle(getString(id))
    }

    override fun startActivityForResult(intent: Intent, requestCode: Int, options: Bundle?) {
        super.startActivityForResult(intent, requestCode, options)
        overridePendingTransition(R.anim.right_in_activity, R.anim.right_out_activity)
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.left_in_activity, R.anim.left_out_activity)
    }

    override fun onResume() {
        super.onResume()

        if(initStatusBar()){
            val proxy = PreferencesUtils.getString("ProxyIp")
            if (!TextUtils.isEmpty(proxy)) {
                ImmersionBar.with(this).statusBarColor(R.color.red).init()
            } else {
                ImmersionBar.with(this)
                    .statusBarDarkFont(true)
                    .fitsSystemWindows(true)
                    .statusBarColor(R.color.white)
                    .init()
            }
        }
    }

    open fun initStatusBar():Boolean{
        return true
    }

}