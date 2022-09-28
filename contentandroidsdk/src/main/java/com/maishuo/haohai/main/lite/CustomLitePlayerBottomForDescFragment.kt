package com.maishuo.haohai.main.lite

import com.maishuo.haohai.databinding.FragmentLitePlayerBottomDescLayoutBinding
import com.qichuang.commonlibs.basic.CustomBaseFragment

class CustomLitePlayerBottomForDescFragment :
    CustomBaseFragment<FragmentLitePlayerBottomDescLayoutBinding>() {

    var desc: String? = null

    override fun initWidgets() {
        vb?.litePlayerDescTvShow?.text = desc
    }

    override fun initWidgetsEvent() {

    }

}