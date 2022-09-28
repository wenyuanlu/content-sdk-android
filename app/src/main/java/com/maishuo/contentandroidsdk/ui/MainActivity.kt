package com.maishuo.contentandroidsdk.ui

import android.Manifest
import android.content.Intent
import android.view.KeyEvent
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.TextView
import android.widget.Toast
import com.maishuo.contentandroidsdk.R
import com.maishuo.contentandroidsdk.base.BaseActivity
import com.maishuo.contentandroidsdk.utils.PermissionUtil
import com.maishuo.contentandroidsdk.utils.PreferencesUtils
import com.maishuo.haohai.common.ContentAndroidSDK
import com.maishuo.haohai.person.ui.InterestActivity
import com.maishuo.haohai.widgets.AutoScrollView
import com.maishuo.haohai.widgets.FloatingView
import com.qichuang.commonlibs.common.PreferencesKey
import kotlin.math.abs
import kotlin.system.exitProcess

class MainActivity : BaseActivity(), FloatingView.FloatingViewListener {

    companion object {
        private val APP_PERMISSIONS = arrayListOf(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
    }

    private var openSdk: TextView? = null
    private var openMultipleSdk: TextView? = null
    private var openRandomOaid: TextView? = null
    private var openLite: TextView? = null

    //TODO 测试
    private var floatingView: FloatingView? = null
    private var autoScrollView: AutoScrollView? = null
    private var tvDelete: TextView? = null
    private var deleteY: Int? = null
    private var isShow: Boolean = false
    private var onLeft: Boolean = false

    override fun fetchRootViewById(): Int {
        return R.layout.activity_main
    }

    override fun initWidgets() {
        openSdk = findViewById(R.id.open_sdk)
        openMultipleSdk = findViewById(R.id.open_multiple_sdk)
        openRandomOaid = findViewById(R.id.open_random_oaid)
        openLite = findViewById(R.id.open_content_sdk_lite)

        PermissionUtil.requestMorePermissions(this, APP_PERMISSIONS, 0x1001)

        //TODO 测试
        floatingView = findViewById(R.id.floatingView)
        autoScrollView = findViewById(R.id.scrollView)
        tvDelete = findViewById(R.id.tvDelete)
        floatingView?.setListener(this)
        deleteY = floatingView?.screenHeight?.minus(floatingView?.dip2px(50f)!!)
        initAutoScrollView()
    }

    override fun initWidgetsEvent() {
        openSdk?.setOnClickListener {
            val isFirstOpenSdk = PreferencesUtils.getBoolean(PreferencesKey.FIRST_OPEN_SDK, true)
            if (isFirstOpenSdk) {
                val intent = Intent(this@MainActivity, InterestActivity::class.java)
                startActivity(intent)
                PreferencesUtils.putBoolean(PreferencesKey.FIRST_OPEN_SDK, false)
            } else {
                val intent = Intent(this@MainActivity, SimpleMainActivity::class.java)
                startActivity(intent)
            }

        }

        openMultipleSdk?.setOnClickListener {
            val intent = Intent(this@MainActivity, MultipleTabLayoutActivity::class.java)
            startActivity(intent)
        }

        openRandomOaid?.setOnClickListener {
            val intent = Intent(this@MainActivity, SelectorOaidActivity::class.java)
            startActivity(intent)
        }

        openLite?.setOnClickListener {
            ContentAndroidSDK.openHaoHaiPlayerView(this, null)
        }
    }

    override fun showLeftGoBackView(): Boolean {
        return false
    }

    private var exitTime: Long? = null
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && event?.action == KeyEvent.ACTION_DOWN) {
            if (abs(System.currentTimeMillis() - (exitTime ?: 0L)) > 2000) {
                Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show()
                exitTime = System.currentTimeMillis()
            } else {
                exitProcess(0)
            }
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    //-------------------------TODO 测试-----------------------------------------
    /**
     * 手势移动中
     */
    override fun floatingViewMoving(y: Float, scrollLeft: Int) {
        if (y > deleteY!!.toFloat()) {
            tvDelete?.text = "松手即可删除"
        } else {
            tvDelete?.text = "拖动到此处删除"
        }
        if (!isShow) {
            isShow = true
            onLeft = scrollLeft == 0
            tvDelete?.visibility = View.VISIBLE
            setAutoScrollViewVisibility(View.GONE)
        }
    }

    /**
     * 手势抬起
     */
    override fun floatingViewUp(eventY: Float, scrollLeft: Int, scrollBottom: Int) {
        tvDelete?.visibility = View.GONE
        if (eventY > deleteY!!.toFloat()) {
            floatingView?.visibility = View.GONE
            autoScrollView?.visibility = View.GONE
            autoScrollView?.removeCallback()
            floatingView?.removeAllViews()
            autoScrollView?.removeAllViews()
        } else {
            isShow = false
            onLeft = scrollLeft == 0
            autoScrollView?.setLayout(scrollLeft, scrollBottom)
            setAutoScrollViewVisibility(View.VISIBLE)
        }
    }

    /**
     * 设置滚动控件动画状态
     */
    private fun setAutoScrollViewVisibility(visibility: Int) {
        val anim = if (visibility == View.GONE) {
            if (onLeft) R.anim.translate_left_out else R.anim.translate_right_out
        } else {
            if (onLeft) R.anim.translate_left_in else R.anim.translate_right_in
        }
        autoScrollView?.startAnimation(AnimationUtils.loadAnimation(this, anim))
        autoScrollView?.visibility = visibility
    }

    /**
     * 设置滚动数据
     */
    private fun initAutoScrollView() {
        val list: MutableList<String> = mutableListOf()
        list.add("推荐广告推荐广告")
        list.add("推荐标题推荐标题")
        list.add("推荐内容推荐内容")
        list.add("推荐视频推荐视频")
        list.add("推荐头条推荐头条")
        autoScrollView?.setData(list)
    }

}