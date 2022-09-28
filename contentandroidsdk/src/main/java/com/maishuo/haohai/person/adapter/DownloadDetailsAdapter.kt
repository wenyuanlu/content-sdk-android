package com.maishuo.haohai.person.adapter

import android.view.View
import android.widget.CheckBox
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.maishuo.haohai.R
import com.maishuo.haohai.listener.OnDialogCallBackListener
import com.maishuo.haohai.utils.DialogUtils
import com.qichuang.commonlibs.basic.CustomBaseAdapter
import com.qichuang.commonlibs.basic.CustomBaseViewHolder
import com.qichuang.commonlibs.download.DownloadFileUtil
import com.qichuang.roomlib.RoomManager
import com.qichuang.roomlib.entity.ListenCommonEntity
import java.io.File

/**
 * author : xpSun
 * date : 11/8/21
 * description :
 */
class DownloadDetailsAdapter(private val adapterCallBack: DownloadDetailsAdapterCallBack) :
    CustomBaseAdapter<ListenCommonEntity, CustomBaseViewHolder>(R.layout.item_download_details) {

    override fun onConvert(holder: CustomBaseViewHolder, item: ListenCommonEntity?) {
        val content = String.format("第%s章：%s", item?.program_au, item?.program_name)
        holder.setText(R.id.tv_item_title, content)

        holder.getView<CheckBox>(R.id.ck_item_download_details).visibility =
            if (item?.showCheck == true) View.VISIBLE else View.GONE

        holder.getView<CheckBox>(R.id.ck_item_download_details).isChecked = item?.isCheck == true

        holder.getView<CheckBox>(R.id.ck_item_download_details)
            .setOnCheckedChangeListener { _, isChecked ->
                item?.isCheck = isChecked
                adapterCallBack.checkBoxChange()
            }

        holder.getView<ImageView>(R.id.iv_item_delete).setOnClickListener {
            deleteCurrentItem(item)
        }
    }

    //单个删除数据库数据和下载文件
    private fun deleteCurrentItem(item: ListenCommonEntity?) {
        DialogUtils.showCommonDialog(
            context as AppCompatActivity?,
            "",
            "确定删除？",
            true,
            object : OnDialogCallBackListener {
                override fun onConfirm(content: String?) {
                    removeItem(getItemPosition(item))
                    deleteSingle(item?.program_id)
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
                        for (bean in checkedList()) {
                            remove(bean)
                            deleteSingle(bean.program_id)
                        }
                    }

                    override fun onCancel() {}
                })
        }
    }

    /**
     * 删除文件和数据库
     */
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
        adapterCallBack.deleteComplete()
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

interface DownloadDetailsAdapterCallBack {
    fun deleteComplete()
    fun checkBoxChange()
}