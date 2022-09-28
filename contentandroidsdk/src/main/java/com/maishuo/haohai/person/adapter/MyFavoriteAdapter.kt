package com.maishuo.haohai.person.adapter

import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageView
import com.maishuo.haohai.R
import com.maishuo.haohai.api.response.GetListResponse
import com.maishuo.haohai.listener.OnDialogCallBackListener
import com.maishuo.haohai.person.viewmodel.MyFavoriteViewModel
import com.maishuo.haohai.utils.DialogUtils
import com.qichuang.commonlibs.basic.CustomBaseAdapter
import com.qichuang.commonlibs.basic.CustomBaseViewHolder
import com.qichuang.commonlibs.utils.GlideUtils

/**
 * author : xpSun
 * date : 11/8/21
 * description :
 */
class MyFavoriteAdapter(private val viewModel: MyFavoriteViewModel) :
    CustomBaseAdapter<GetListResponse, CustomBaseViewHolder>(R.layout.item_person_my_favorite) {

    override fun onConvert(holder: CustomBaseViewHolder, item: GetListResponse?) {
        GlideUtils.loadImage(
            holder.itemView.context,
            item?.album_cover,
            holder.getView(R.id.iv_my_favorite),
            R.mipmap.home_item_default_icon
        )

        holder.setText(R.id.tv_my_favorite_content, item?.album_name)
        if (item?.program_id ?: 0 == 0) {
            holder.setText(
                R.id.tv_my_favorite_section,
                String.format("%s %s", item?.author_name ?: "", item?.anchor_name ?: "")
            )
        } else {
            holder.setText(
                R.id.tv_my_favorite_section,
                String.format("第%s章 %s", item?.program_au ?: "", item?.program_name ?: "")
            )
        }

        holder.getView<AppCompatImageView>(R.id.iv_my_favorite_delete).setOnClickListener {
            deleteCurrentFavorite(item)
        }

    }

    //取消当条收藏
    private fun deleteCurrentFavorite(item: GetListResponse?) {
        DialogUtils.showCommonDialog(
            context as AppCompatActivity?,
            "",
            "您确定要删除此订阅吗？",
            true,
            object : OnDialogCallBackListener {
                override fun onConfirm(content: String?) {
                    removeItem(getItemPosition(item))
                    viewModel.keep(item?.album_type, item?.album_id, item?.program_id)
                }

                override fun onCancel() {}
            })
    }
}