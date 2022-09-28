package com.maishuo.contentandroidsdk.base

import android.os.Bundle
import android.view.MenuItem
import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.maishuo.contentandroidsdk.utils.LoggerUtils

/**
 * author : xpSun
 * date : 12/14/21
 * description :
 */
abstract class BaseActivity : AppCompatActivity(), IWidgetsListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val actionBar = supportActionBar
        if (actionBar != null && showLeftGoBackView()) {
            actionBar.setHomeButtonEnabled(true)
            actionBar.setDisplayHomeAsUpEnabled(true)
        }
        setContentView(fetchRootViewById())
        initWidgets()
        initWidgetsEvent()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    open fun showLeftGoBackView(): Boolean {
        return true
    }

    fun commitFragment(@IdRes layoutId: Int, fragment: Fragment?) {
        try {
            if (null == fragment) {
                return
            }

            val fragmentTransaction = supportFragmentManager.beginTransaction()
            if (!fragment.isAdded) {
                fragmentTransaction.add(layoutId, fragment)
                fragmentTransaction.commitNow()
            } else {
                val fragments = supportFragmentManager.fragments

                if (!fragments.isNullOrEmpty()) {
                    for (item in fragments.iterator()) {
                        fragmentTransaction.hide(item)
                    }
                }

                fragmentTransaction.show(fragment)
                fragmentTransaction.commitNow()
            }
        } catch (e: Exception) {
            e.printStackTrace()
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

    override fun onDestroy() {
        super.onDestroy()
        LoggerUtils.e("onDestroy")
    }

}