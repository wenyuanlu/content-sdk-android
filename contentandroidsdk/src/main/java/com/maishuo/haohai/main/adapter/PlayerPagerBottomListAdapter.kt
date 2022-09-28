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
class PlayerPagerBottomListAdapter : CustomBaseAdapter<GetListResponse,
        CustomBaseViewHolder>(R.layout.view_player_pager_bottom_dialog_item_layout) {

    var currentMediaItemId: String? = null
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onConvert(holder: CustomBaseViewHolder, item: GetListResponse?) {
        val playerStatus =
            holder.getView<AppCompatImageView>(R.id.player_list_bottom_dialog_item_status)
        val playerName = holder.getView<TextView>(R.id.player_list_bottom_dialog_item_name)
        val downloadStatus =
            holder.getView<AppCompatImageView>(R.id.player_list_bottom_dialog_item_download)

        playerName.text = item?.album_name ?: ""
        playerStatus.visibility = if (currentMediaItemId == item?.album_id.toString()) {
            View.VISIBLE
        } else {
            View.INVISIBLE
        }

        when (item?.download_status) {
            Constant.COMMON_DOWNLOAD_STATUS_1 -> {
                downloadStatus.setImageResource(R.mipmap.download_status_1)
            }
            Constant.COMMON_DOWNLOAD_STATUS_2 -> {
                downloadStatus.setImageResource(R.mipmap.download_status_2)
            }
            Constant.COMMON_DOWNLOAD_STATUS_3 -> {
                downloadStatus.setImageResource(R.mipmap.download_status_3)
            }
            else -> {
                downloadStatus.setImageResource(R.mipmap.download_status_1)
            }
        }
    }
}