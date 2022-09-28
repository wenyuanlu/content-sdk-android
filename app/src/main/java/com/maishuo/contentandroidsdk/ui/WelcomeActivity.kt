package com.maishuo.contentandroidsdk.ui

import android.content.Intent
import android.widget.LinearLayout
import com.maishuo.contentandroidsdk.R
import com.maishuo.contentandroidsdk.base.BaseActivity

/**
 * author : xpSun
 * date : 12/15/21
 * description :
 */
class WelcomeActivity : BaseActivity() {

    private var root: LinearLayout? = null

    override fun fetchRootViewById(): Int {
        return R.layout.activity_welcome_layout
    }

    override fun initWidgets() {
        root = findViewById(R.id.welcome_root)

        root?.postDelayed({
            val intent = Intent(this@WelcomeActivity, MainActivity::class.java)
            startActivity(intent)
            finish()
        }, 3000L)
    }

    override fun initWidgetsEvent() {

    }

    override fun showLeftGoBackView(): Boolean {
        return false
    }
}