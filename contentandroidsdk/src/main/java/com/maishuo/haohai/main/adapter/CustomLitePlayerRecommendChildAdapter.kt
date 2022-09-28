package com.maishuo.haohai.main.adapter

import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import com.maishuo.haohai.R
import com.maishuo.haohai.api.response.GetListResponse
import com.qichuang.commonlibs.basic.CustomBaseAdapter
import com.qichuang.commonlibs.basic.CustomBaseViewHolder
import com.qichuang.commonlibs.utils.GlideUtils

/**
 * author : xpSun
 * date : 2022/3/14
 * description :
 */
class CustomLitePlayerRecommendChildAdapter : CustomBaseAdapter<GetListResponse,
        CustomBaseViewHolder>(R.layout.view_lite_player_recommend_child_item_layout) {
    override fun onConvert(holder: CustomBaseViewHolder, item: GetListResponse?) {
        val ivShow =
            holder.getView<AppCompatImageView>(R.id.lite_player_recommend_child_item_iv_show)
        val tvShow = holder.getView<TextView>(R.id.lite_player_recommend_child_item_tv_show)

        GlideUtils.loadImage(holder.view.context, item?.album_cover, ivShow)
        tvShow.text = item?.album_name ?: ""
    }
}