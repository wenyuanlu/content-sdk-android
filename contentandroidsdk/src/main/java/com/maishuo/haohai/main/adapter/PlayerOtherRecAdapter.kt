package com.maishuo.haohai.main.adapter

import com.maishuo.haohai.R
import com.maishuo.haohai.api.response.GetListResponse
import com.maishuo.haohai.widgets.CustomRoundedImageView
import com.qichuang.commonlibs.basic.CustomBaseAdapter
import com.qichuang.commonlibs.basic.CustomBaseViewHolder
import com.qichuang.commonlibs.utils.GlideUtils

/**
 * author : xpSun
 * date : 11/17/21
 * description :
 */
class PlayerOtherRecAdapter :
    CustomBaseAdapter<GetListResponse,
            CustomBaseViewHolder>(R.layout.view_player_other_rec_item_layout) {

    override fun onConvert(holder: CustomBaseViewHolder, item: GetListResponse?) {
        val ivShow = holder.getView<CustomRoundedImageView>(R.id.other_rec_iv_show)
        GlideUtils.loadImage(
            holder.itemView.context,
            item?.album_cover,
            ivShow
        )
        holder.setText(R.id.other_rec_tv_show, item?.album_name ?: "")
    }

}