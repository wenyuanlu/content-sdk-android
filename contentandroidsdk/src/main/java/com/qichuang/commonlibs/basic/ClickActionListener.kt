package com.qichuang.commonlibs.basic

import android.view.View

interface ClickActionListener : View.OnClickListener {

    override fun onClick(v: View) {}

    //title右侧按钮回调
    fun onClickByTitleRightMenu() {}
}