package com.maishuo.haohai.person.ui

import android.content.Intent
import androidx.fragment.app.Fragment
import com.maishuo.haohai.databinding.ActivityMyFavoriteBinding
import com.maishuo.haohai.main.adapter.CommonViewPagerAdapter
import com.qichuang.commonlibs.basic.CustomBaseActivity
import com.qichuang.roomlib.RoomManager

class DownloadActivity : CustomBaseActivity<ActivityMyFavoriteBinding>() {
    private val fragments: MutableList<Fragment> =
        mutableListOf(DownloadFragment(1), DownloadFragment(3), DownloadFragment(2))
    private val tabMenus: MutableList<String> = mutableListOf("小说", "课程", "头条")

    override fun initWidgets() {
        setTitle("我的下载")
        vb?.myFavoriteViewpager?.adapter =
            CommonViewPagerAdapter(supportFragmentManager, fragments, tabMenus)
        vb?.myFavoriteTitleTabLayout?.viewPager = vb?.myFavoriteViewpager
    }

    override fun initWidgetsEvent() {}

    override fun onClickByTitleRightMenu() {
        super.onClickByTitleRightMenu()
        startActivity(
            Intent(this, DownloadingActivity::class.java)
        )
    }

    /**
     * 显示下载中逻辑
     */
    private fun showRightTitle() {
        val taskList = RoomManager.getInstance(this).loadAllFailListenCommon()
        if (!taskList.isNullOrEmpty()) {
            setTitleRightMenu("下载中")
        } else {
            isShowTitleRightMenu(false)
        }
    }

    override fun onResume() {
        super.onResume()
        showRightTitle()
    }

}