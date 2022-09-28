package com.maishuo.haohai.common.adapter

import com.maishuo.haohai.R
import com.maishuo.haohai.api.bean.DialogBottomMoreBean
import com.qichuang.commonlibs.basic.CustomBaseAdapter
import com.qichuang.commonlibs.basic.CustomBaseViewHolder

class DialogBottomMoreAdapter : CustomBaseAdapter<DialogBottomMoreBean,
        CustomBaseViewHolder>(R.layout.view_dialog_bottom_more_item_layout) {

    override fun onConvert(holder: CustomBaseViewHolder, item: DialogBottomMoreBean?) {
        val text = item?.text ?: ""
        val select = item?.isSelect ?: false
        holder.setText(R.id.tv_dialog_report_item, text)
        holder.setVisible(R.id.iv_dialog_report_select, select)
    }
}