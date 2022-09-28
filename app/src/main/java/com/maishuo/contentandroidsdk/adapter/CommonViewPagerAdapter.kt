package com.maishuo.contentandroidsdk.adapter

import android.os.Parcelable
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter

/**
 * author : xpSun
 * date : 2021/3/19
 * description :
 */
class CommonViewPagerAdapter(
    fm: FragmentManager,
    private val fragmentSparseArray: MutableList<Fragment>? = null,
    private val titles: MutableList<String>? = null
) : FragmentStatePagerAdapter(fm) {

    override fun getItemPosition(`object`: Any): Int {
        return POSITION_NONE
    }

    override fun saveState(): Parcelable? {
        return null
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return if (null == titles) "" else titles[position]
    }

    override fun getItem(position: Int): Fragment {
        return fragmentSparseArray!![position]
    }

    override fun getCount(): Int {
        return fragmentSparseArray?.size ?: 0
    }
}