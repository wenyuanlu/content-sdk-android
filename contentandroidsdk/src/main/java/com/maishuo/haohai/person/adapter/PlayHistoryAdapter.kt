package com.maishuo.haohai.person.adapter

import android.view.View
import android.widget.CheckBox
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.maishuo.haohai.R
import com.maishuo.haohai.api.response.GetListResponse
import com.maishuo.haohai.listener.OnDialogCallBackListener
import com.maishuo.haohai.utils.DialogUtils
import com.qichuang.commonlibs.basic.CustomBaseAdapter
import com.qichuang.commonlibs.basic.CustomBaseViewHolder
import com.qichuang.commonlibs.utils.GlideUtils

class PlayHistoryAdapter(private val adapterCallBack: PlayHistoryAdapterCallBack) :
    CustomBaseAdapter<GetListResponse, CustomBaseViewHolder>(R.layout.item_play_history) {

    override fun onConvert(holder: CustomBaseViewHolder, item: GetListResponse?) {
        if (item?.isCeiling == true) {
            holder.getView<TextView>(R.id.tv_group).visibility = View.VISIBLE
        } else {
            GlideUtils.loadImage(
                holder.itemView.context,
                item?.album_cover,
                holder.getView(R.id.iv_cover),
                R.mipmap.home_item_default_icon
            )

            val content = if (item?.program_id ?: 0 == 0) {
                String.format("%s %s", item?.author_name ?: "", item?.anchor_name ?: "")
            } else {
                String.format("第%s章 %s", item?.program_au ?: "", item?.program_name ?: "")
            }

            holder.setText(R.id.tv_title, item?.album_name)
            holder.setText(R.id.tv_content, content)
            holder.setText(R.id.tv_progress, String.format("收听至%s秒", item?.play_second ?: ""))
        }
        holder.getView<CheckBox>(R.id.ck_item).visibility =
            if (item?.showCheck == true) View.VISIBLE else View.GONE

        holder.getView<CheckBox>(R.id.ck_item).isChecked = item?.isCheck == true

        holder.getView<CheckBox>(R.id.ck_item)
            .setOnCheckedChangeListener { _, isChecked ->
                item?.isCheck = isChecked
                adapterCallBack.checkBoxChange()
            }
    }

    //单个删除数据库数据和下载文件
    private fun deleteCurrentItem(item: GetListResponse?) {
        DialogUtils.showCommonDialog(
            context as AppCompatActivity?,
            "",
            "确定删除？",
            true,
            object : OnDialogCallBackListener {
                override fun onConfirm(content: String?) {
                    removeItem(getItemPosition(item))
//                    deleteSingle(item?.program_id)
                }

                override fun onCancel() {}
            })
    }

    //多个删除数据库数据和下载文件
    fun deleteMultipleItem() {
        if (!checkedList().isNullOrEmpty()) {
            DialogUtils.showCommonDialog(
                context as AppCompatActivity?,
                "",
                "确定删除？",
                true,
                object : OnDialogCallBackListener {
                    override fun onConfirm(content: String?) {
                        var ids = ""
                        for (bean in checkedList()) {
                            remove(bean)
                            ids = ids + bean.album_lid + ","
                        }
                        adapterCallBack.deleteCheckedItem(ids.substringBeforeLast(","))
                    }

                    override fun onCancel() {}
                })
        }
    }

    /**
     * 检查是否有选中的数据
     */
    fun checkedList(): MutableList<GetListResponse> {
        val list: MutableList<GetListResponse> = mutableListOf()
        for (bean in data) {
            if (bean.isCheck == true) {
                list.add(bean)
            }
        }
        return list
    }
}

interface PlayHistoryAdapterCallBack {
    fun deleteCheckedItem(albumLids: String)
    fun checkBoxChange()
}