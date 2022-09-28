package com.maishuo.haohai.person.ui

import androidx.fragment.app.Fragment
import com.maishuo.haohai.api.bean.MyFavoriteCountEvent
import com.maishuo.haohai.databinding.ActivityMyFavoriteBinding
import com.maishuo.haohai.main.adapter.CommonViewPagerAdapter
import com.qichuang.commonlibs.basic.CustomBaseActivity
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class MyFavoriteActivity : CustomBaseActivity<ActivityMyFavoriteBinding>() {
    private var count: Int = 0
    private val fragments: MutableList<Fragment> =
        mutableListOf(MyFavoriteFragment(1), MyFavoriteFragment(3))
    private val tabMenus: MutableList<String> = mutableListOf("小说", "课程")

    override fun initWidgets() {
        setTitle("我的订阅")
        EventBus.getDefault().register(this)
        vb?.myFavoriteViewpager?.adapter =
            CommonViewPagerAdapter(supportFragmentManager, fragments, tabMenus)
        vb?.myFavoriteTitleTabLayout?.viewPager = vb?.myFavoriteViewpager
    }

    override fun initWidgetsEvent() {}

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: MyFavoriteCountEvent?) {
        if ((event?.count ?: 0) > 0) {
            count = event?.count ?: 0
        } else {
            count--
        }
        if (count > 0) {
            setTitle("我的订阅（${count}）")
        } else {
            setTitle("我的订阅")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }
}