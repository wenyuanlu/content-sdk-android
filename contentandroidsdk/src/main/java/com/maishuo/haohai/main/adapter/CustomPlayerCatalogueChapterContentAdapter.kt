package com.maishuo.haohai.main.adapter

import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import com.maishuo.haohai.R
import com.maishuo.haohai.api.response.GetListResponse
import com.maishuo.haohai.common.Constant
import com.qichuang.commonlibs.basic.CustomBaseAdapter
import com.qichuang.commonlibs.basic.CustomBaseViewHolder

/**
 * author : xpSun
 * date : 11/12/21
 * description :
 */
class CustomPlayerCatalogueChapterContentAdapter :
    CustomBaseAdapter<GetListResponse,
            CustomBaseViewHolder>(R.layout.view_player_left_menu_chapter_content_item_layout) {

    var currentMediaItemId: String? = null
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onConvert(holder: CustomBaseViewHolder, item: GetListResponse?) {
        val currentPlayerIcon = holder.getView<AppCompatImageView>(R.id.player_left_menu_status)
        val titleShow = holder.getView<TextView>(R.id.player_left_menu_number)
        val contentShow = holder.getView<TextView>(R.id.player_left_menu_name)

        if (currentMediaItemId == item?.program_lid) {
            currentPlayerIcon.visibility = View.VISIBLE
            titleShow.visibility = View.GONE
        } else {
            currentPlayerIcon.visibility = View.GONE
            titleShow.visibility = View.VISIBLE
        }

        titleShow.text = (item?.program_au ?: "").toString()
        contentShow.text = item?.program_name ?: ""
    }
}