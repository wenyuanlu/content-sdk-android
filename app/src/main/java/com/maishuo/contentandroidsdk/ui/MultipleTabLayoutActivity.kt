package com.maishuo.contentandroidsdk.ui

import android.graphics.Color
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.maishuo.contentandroidsdk.R
import com.maishuo.contentandroidsdk.base.BaseActivity

/**
 * author : xpSun
 * date : 12/14/21
 * description :
 */
class MultipleTabLayoutActivity : BaseActivity() {

    private var bottomNavigationView: BottomNavigationView? = null
    private var contentFragment: MultipleTabLayoutFragment? = null

    private var commonEmptyFragmentRed: CommonEmptyFragment? = null
    private var commonEmptyFragmentYellow: CommonEmptyFragment? = null

    override fun fetchRootViewById(): Int {
        return R.layout.activiy_multiple_tablayout_layout
    }

    override fun initWidgets() {
        bottomNavigationView = findViewById(R.id.multiply_table_bottom_layout)

        contentFragment = MultipleTabLayoutFragment()
        commonEmptyFragmentRed = CommonEmptyFragment(Color.RED)
        commonEmptyFragmentYellow = CommonEmptyFragment(Color.YELLOW)

        commitFragment(R.id.multiply_table_content, contentFragment)
    }

    override fun initWidgetsEvent() {
        bottomNavigationView?.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.item_bottom_1 -> {
                    commitFragment(R.id.multiply_table_content, contentFragment)
                }
                R.id.item_bottom_2 -> {
                    commitFragment(R.id.multiply_table_content, commonEmptyFragmentRed)
                }
                R.id.item_bottom_3 -> {
                    commitFragment(R.id.multiply_table_content, commonEmptyFragmentYellow)
                }
            }
            false
        }
    }
}