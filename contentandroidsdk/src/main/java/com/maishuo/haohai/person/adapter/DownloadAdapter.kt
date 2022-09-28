package com.maishuo.haohai.person.adapter

import android.view.View
import android.widget.CheckBox
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageView
import com.maishuo.haohai.R
import com.maishuo.haohai.listener.OnDialogCallBackListener
import com.maishuo.haohai.utils.DialogUtils
import com.qichuang.commonlibs.basic.CustomBaseAdapter
import com.qichuang.commonlibs.basic.CustomBaseViewHolder
import com.qichuang.commonlibs.download.DownloadFileUtil
import com.qichuang.commonlibs.utils.GlideUtils
import com.qichuang.roomlib.RoomManager
import com.qichuang.roomlib.entity.ListenCommonEntity
import java.io.File

/**
 * author : xpSun
 * date : 11/8/21
 * description :
 */
class DownloadAdapter(private val adapterCallBack: DownloadAdapterCallBack) :
    CustomBaseAdapter<ListenCommonEntity, CustomBaseViewHolder>(R.layout.item_download) {

    override fun onConvert(holder: CustomBaseViewHolder, item: ListenCommonEntity?) {
        GlideUtils.loadImage(
            holder.itemView.context,
            item?.album_cover,
            holder.getView(R.id.iv_download_cover),
            R.mipmap.home_item_default_icon
        )

        holder.setText(R.id.tv_download_content, item?.album_name)
        when (item?.album_type) {
            1 -> {
                holder.setText(
                    R.id.tv_download_sum,
                    String.format("已下载%s集", item.count)
                )
            }
            2 -> {
                holder.setText(
                    R.id.tv_download_sum,
                    String.format("来源：%s", item.author_name ?: "")
                )
            }
            3 -> {
                holder.setText(
                    R.id.tv_download_sum,
                    String.format("已下载%s讲", item.count)
                )
            }
        }

        holder.getView<CheckBox>(R.id.ck_item_download).visibility =
            if (item?.showCheck == true) View.VISIBLE else View.GONE

        holder.getView<CheckBox>(R.id.ck_item_download).isChecked = item?.isCheck == true

        holder.getView<CheckBox>(R.id.ck_item_download)
            .setOnCheckedChangeListener { _, isChecked ->
                item?.isCheck = isChecked
                adapterCallBack.checkBoxChange()
            }

        holder.getView<AppCompatImageView>(R.id.iv_download_delete).setOnClickListener {
            deleteCurrentItem(item)
        }

    }

    //删除多个头条
    fun deleteMultipleItem() {
        if (!checkedList().isNullOrEmpty()) {
            DialogUtils.showCommonDialog(
                context as AppCompatActivity?,
                "",
                "确定删除？",
                true,
                object : OnDialogCallBackListener {
                    override fun onConfirm(content: String?) {
                        for (bean in checkedList()) {
                            remove(bean)
                            deleteSingle(bean.program_id)
                        }
                        if (data.size == 0) {
                            adapterCallBack.deleteComplete()
                        }
                    }

                    override fun onCancel() {}
                })
        }
    }

    //删除单个头条和全部小说，全部课程
    private fun deleteCurrentItem(item: ListenCommonEntity?) {
        val content =
            if (item?.album_type == 1) "确定要删除该专辑吗" else if (item?.album_type == 3) "确定要删除该课程吗" else "确定删除？"
        DialogUtils.showCommonDialog(
            context as AppCompatActivity?,
            "",
            content,
            true,
            object : OnDialogCallBackListener {
                override fun onConfirm(content: String?) {
                    if (item?.album_type == 2) {
                        deleteSingle(item.program_id)
                    } else {
                        val list =
                            RoomManager.getInstance(context)
                                .loadListenCommonByAlbumTypeAndId(item?.album_type, item?.album_id)
                        if (!list.isNullOrEmpty()) {
                            for (bean in list) {
                                deleteSingle(bean.program_id)
                            }
                        }
                    }
                    if (data.size > 1) {
                        removeItem(getItemPosition(item))
                    } else {
                        adapterCallBack.deleteComplete()
                    }
                }

                override fun onCancel() {}
            })
    }

    private fun deleteSingle(programId: Int?) {
        //删除广告资源
        val singleAd =
            RoomManager.getInstance(context)
                .loadSingleAd(programId)
        if (singleAd?.mp3_url != null) {
            val file = File(singleAd.mp3_url!!)
            DownloadFileUtil().delete(file.parent!!)
        }
        //删除内容资源
        val singleContent =
            RoomManager.getInstance(context)
                .loadSingleListenCommon(programId)
        if (singleContent?.mp3_url != null) {
            val file = File(singleContent.mp3_url!!)
            DownloadFileUtil().delete(file.parent!!)
        }
        //删除数据库数据
        RoomManager.getInstance(context).deleteListenCommon(singleContent)
    }

    /**
     * 检查是否有选中的数据
     */
    fun checkedList(): MutableList<ListenCommonEntity> {
        val list: MutableList<ListenCommonEntity> = mutableListOf()
        for (bean in data) {
            if (bean.isCheck == true) {
                list.add(bean)
            }
        }
        return list
    }
}

interface DownloadAdapterCallBack {
    fun deleteComplete()
    fun checkBoxChange()
}