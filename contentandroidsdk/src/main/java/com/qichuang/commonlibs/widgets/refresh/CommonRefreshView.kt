package com.qichuang.commonlibs.widgets.refresh

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import androidx.annotation.ColorInt
import com.scwang.smart.refresh.footer.ClassicsFooter
import com.scwang.smart.refresh.header.MaterialHeader
import com.scwang.smart.refresh.layout.SmartRefreshLayout

class CommonRefreshView constructor(context: Context?, attrs: AttributeSet)
    : SmartRefreshLayout(context, attrs) {

    private var refreshHeader: MaterialHeader? = null
    private var refreshBottom: ClassicsFooter? = null

    init {
        refreshHeader = MaterialHeader(context)
        refreshHeader?.setShowBezierWave(true)
        refreshHeader?.setColorSchemeColors(Color.parseColor("#333333"))
        setPrimaryColorsId(android.R.color.transparent)

        refreshBottom = ClassicsFooter(context)

        setRefreshHeader(refreshHeader!!)
        setRefreshFooter(refreshBottom!!)
    }
}