package com.maishuo.haohai.main.adapter

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.maishuo.haohai.R
import com.maishuo.haohai.api.response.GetListResponse
import com.maishuo.haohai.widgets.CustomRoundedImageView
import com.qichuang.commonlibs.basic.CustomBaseAdapter
import com.qichuang.commonlibs.basic.CustomBaseViewHolder
import com.qichuang.commonlibs.utils.GlideUtils

class CustomLiteOtherRecommendAdapter : CustomBaseAdapter<GetListResponse,
        CustomBaseViewHolder>(R.layout.view_lite_other_recommend_item_layout) {

    override fun onConvert(holder: CustomBaseViewHolder, item: GetListResponse?) {
        val tip = holder.getView<ImageView>(R.id.lite_other_recommend_top_tip)
        val cover = holder.getView<CustomRoundedImageView>(R.id.lite_other_recommend_image)
        val title = holder.getView<TextView>(R.id.lite_other_recommend_title)
        val desc = holder.getView<TextView>(R.id.lite_other_recommend_desc)
        val author = holder.getView<TextView>(R.id.lite_other_recommend_author)

        when (holder.adapterPosition) {
            0 -> {
                tip.visibility = View.VISIBLE
                tip.setImageResource(R.mipmap.other_recommend_top_1_icon)
            }
            1 -> {
                tip.visibility = View.VISIBLE
                tip.setImageResource(R.mipmap.other_recommend_top_2_icon)
            }
            2 -> {
                tip.visibility = View.VISIBLE
                tip.setImageResource(R.mipmap.other_recommend_top_3_icon)
            }
            else -> {
                tip.visibility = View.GONE
            }
        }

        GlideUtils.loadImage(holder.view.context, item?.album_cover, cover)
        title.text = item?.album_name ?: ""
        desc.text = item?.summary ?: ""
        author.text = "作者: ${item?.author_name ?: ""}"
    }
}