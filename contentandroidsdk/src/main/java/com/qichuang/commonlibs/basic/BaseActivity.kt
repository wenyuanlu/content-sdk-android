package com.qichuang.commonlibs.basic

import android.content.Intent
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import com.maishuo.haohai.databinding.ActivityBaseBinding

/**
 * author : xpSun
 * date : 11/5/21
 * description :
 */
abstract class BaseActivity : IBasicActivity(),
    BasicCommonWidgetListener, ClickActionListener {

    private var binding: ActivityBaseBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        CustomBasicApplication.addActivity(this)

        initBaseWidgets()
        initWidgets()
        initWidgetsEvent()
    }

    private fun initBaseWidgets() {
        binding = ActivityBaseBinding.inflate(LayoutInflater.from(this))
        setContentView(binding?.root)

        val view = fetchRootView()
        if (null != view) {
            binding?.baseContentLayout?.addView(view)
        }
        binding?.baseTitleBack?.setOnClickListener { onBackListener() }
    }

    open fun onBackListener() {
        finish()
    }

    //设置title
    open fun setTitle(title: String?) {
        binding?.baseTitleTvShow?.text = title ?: ""
        binding?.baseTitleLinear?.visibility = View.VISIBLE
        isShowCustomTitle(true)
    }

    //设置title右侧按钮
    open fun setTitleRightMenu(rightMenu: String?) {
        setTitleRightMenu(rightMenu, 0)
    }

    //设置title右侧按钮
    open fun setTitleRightMenu(rightMenu: String?, color: Int) {
        binding?.baseTitleRightMenu?.text = rightMenu ?: ""
        binding?.baseTitleRightMenu?.visibility = View.VISIBLE
        binding?.baseTitleRightMenu?.setOnClickListener { onClickByTitleRightMenu() }
        if (color != 0) {
            binding?.baseTitleRightMenu?.setTextColor(color)
        }
    }

    //显示隐藏title
    override fun isShowCustomTitle(visibility: Boolean?) {
        binding?.baseTitleLayout?.visibility = if (visibility == true) View.VISIBLE else View.GONE
    }

    //显示隐藏右侧按钮
    open fun isShowTitleRightMenu(visibility: Boolean?) {
        binding?.baseTitleRightMenu?.visibility =
            if (visibility == true) View.VISIBLE else View.GONE
    }

    override fun onDestroy() {
        CustomBasicApplication.removeActivity(this)
        super.onDestroy()
    }

    //如果当前的 Activity（singleTop 启动模式） 被复用时会回调
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // 设置为当前的 Intent，避免 Activity 被杀死后重启 Intent 还是最原先的那个
        setIntent(intent)
    }

    override fun getResources(): Resources {
        val res = super.getResources()
        val config = Configuration()
        config.setToDefaults()
        res.updateConfiguration(config, res.displayMetrics)
        return res
    }
}