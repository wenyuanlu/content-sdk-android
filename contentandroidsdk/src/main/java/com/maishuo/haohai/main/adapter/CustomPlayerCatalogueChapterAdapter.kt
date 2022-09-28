package com.maishuo.haohai.main.adapter

import android.widget.TextView
import androidx.core.content.ContextCompat
import com.maishuo.haohai.R
import com.qichuang.commonlibs.basic.CustomBaseAdapter
import com.qichuang.commonlibs.basic.CustomBaseViewHolder

/**
 * author : xpSun
 * date : 11/12/21
 * description :
 */
class CustomPlayerCatalogueChapterAdapter :
    CustomBaseAdapter<String, CustomBaseViewHolder>(R.layout.view_player_left_menu_chapter_item_layout) {

    var selectorPosition: Int? = null
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onConvert(holder: CustomBaseViewHolder, item: String?) {
        val tvShow = holder.getView<TextView>(R.id.player_lift_menu_chapter_item_name)

        val color = if ((selectorPosition ?: 0) == holder.layoutPosition) {
            R.color.color_9e64b0
        } else {
            R.color.color_666666
        }
        tvShow.text = item ?: ""
        tvShow.setTextColor(ContextCompat.getColor(holder.itemView.context, color))
    }
}