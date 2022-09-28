package com.maishuo.contentandroidsdk.ui

import android.graphics.Color
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.maishuo.contentandroidsdk.R
import com.maishuo.contentandroidsdk.adapter.CommonViewPagerAdapter
import com.maishuo.contentandroidsdk.base.BaseFragment

/**
 * author : xpSun
 * date : 12/14/21
 * description :
 */
class MultipleTabLayoutFragment : BaseFragment() {

    companion object {
        private val tabMenus: MutableList<String> = mutableListOf("1", "2", "3", "4", "5", "6", "7")
        private val colors: MutableList<Int> = mutableListOf(
            Color.RED,
            Color.YELLOW,
            Color.BLUE,
            Color.GREEN,
            Color.BLACK,
            Color.CYAN,
            Color.WHITE
        )

        const val CUSTOM_SHOW_SDK_POSITION: Int = 2
    }

    private var tabLayout: TabLayout? = null
    private var viewPager: ViewPager? = null
    private var fragments: MutableList<Fragment> = mutableListOf()
    private var viewPagerAdapter: CommonViewPagerAdapter? = null
    private var contentSdkFragment: ContentAndroidSdkFragment? = null

    override fun fetchRootViewById(): Int {
        return R.layout.fragment_multip_tablayout_layout
    }

    override fun initWidgets() {
        tabLayout = rootView?.findViewById(R.id.multiply_table_fragment_table)
        viewPager = rootView?.findViewById(R.id.multiply_table_fragment_content)

        for (i in 0 until tabMenus.size) {
            if (CUSTOM_SHOW_SDK_POSITION == i) {
                contentSdkFragment = ContentAndroidSdkFragment()
                fragments.add(contentSdkFragment!!)
            } else {
                fragments.add(CommonEmptyFragment(colors[i]))
            }
        }

        viewPagerAdapter = CommonViewPagerAdapter(childFragmentManager, fragments, tabMenus)
        viewPager?.adapter = viewPagerAdapter
        tabLayout?.setupWithViewPager(viewPager)
        viewPager?.offscreenPageLimit = tabMenus.size

    }

    override fun initWidgetsEvent() {
        viewPager?.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {

            }

            override fun onPageSelected(position: Int) {
                contentSdkFragment?.showContentSDK(position)
            }

            override fun onPageScrollStateChanged(state: Int) {

            }
        })
    }
}