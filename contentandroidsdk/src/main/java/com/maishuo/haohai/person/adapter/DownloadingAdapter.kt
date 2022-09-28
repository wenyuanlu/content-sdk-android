package com.maishuo.haohai.person.adapter

import android.view.View
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageView
import com.maishuo.haohai.R
import com.maishuo.haohai.api.bean.DownloadingStatus
import com.maishuo.haohai.api.response.GetListResponse
import com.maishuo.haohai.listener.OnDialogCallBackListener
import com.maishuo.haohai.person.service.DownloadService
import com.maishuo.haohai.utils.DialogUtils
import com.maishuo.haohai.widgets.BGAProgressBar
import com.qichuang.commonlibs.basic.CustomBaseAdapter
import com.qichuang.commonlibs.basic.CustomBaseViewHolder
import com.qichuang.commonlibs.download.DownloadFileUtil
import com.qichuang.commonlibs.utils.GlideUtils
import com.qichuang.commonlibs.utils.NetUtils
import com.qichuang.commonlibs.utils.ToastUtil
import com.qichuang.roomlib.RoomManager
import java.io.File

/**
 * author : xpSun
 * date : 11/8/21
 * description :
 */
class DownloadingAdapter(private val adapterCallBack: DownloadingAdapterCallBack) :
    CustomBaseAdapter<GetListResponse, CustomBaseViewHolder>(R.layout.item_downloading) {

    override fun onConvert(holder: CustomBaseViewHolder, item: GetListResponse?) {
        GlideUtils.loadImage(
            holder.itemView.context,
            item?.album_cover,
            holder.getView(R.id.iv_downloading_cover),
            R.mipmap.home_item_default_icon
        )

        holder.setText(R.id.tv_downloading_content, item?.album_name)
        when (item?.album_type) {
            1 -> {
                holder.setText(
                    R.id.tv_downloading_sum,
                    String.format("第%s章：%s", item.program_au ?: "", item.program_name)
                )
            }
            2 -> {
                holder.setText(
                    R.id.tv_downloading_sum,
                    String.format("来源：%s", item.author_name ?: "")
                )
            }
            3 -> {
                holder.setText(
                    R.id.tv_downloading_sum,
                    String.format("第%s讲：%s", item.program_au ?: "", item.program_name)
                )
            }
        }

        holder.getView<AppCompatImageView>(R.id.iv_downloading_delete).setOnClickListener {
            DialogUtils.showCommonDialog(
                context as AppCompatActivity?,
                "",
                "确定要删除该下载任务？",
                true,
                object : OnDialogCallBackListener {
                    override fun onConfirm(content: String?) {
                        //删除任务
                        DownloadService.cancelTask(item?.downloading_id)
                        //删除数据
                        deleteSingle(item?.program_id)
                        //删除列表项
                        if (item != null) {
                            remove(item)
                        }
                        adapterCallBack.deleteComplete()
                    }

                    override fun onCancel() {}
                })
        }
        holder.getView<RelativeLayout>(R.id.rl_downloading_status)
            .setOnClickListener(MyClick(getItemPosition(item), this))

        //0-失败,1-完成,2-停止,3-等待,4-正在执行
        when (item?.downloading_status) {
            DownloadingStatus.STATE_WAIT -> {
                holder.getView<AppCompatImageView>(R.id.iv_downloading_status)
                    .setImageResource(R.mipmap.icon_downloading_wait)
                holder.setText(R.id.tv_downloading_status, "等待下载")
                holder.getView<BGAProgressBar>(R.id.pb_downloading).visibility = View.GONE
            }
            DownloadingStatus.STATE_OTHER, DownloadingStatus.STATE_FAIL -> {
                holder.getView<AppCompatImageView>(R.id.iv_downloading_status)
                    .setImageResource(R.mipmap.icon_downloading_fail)
                holder.setText(R.id.tv_downloading_status, "下载失败")
                holder.getView<BGAProgressBar>(R.id.pb_downloading).visibility = View.GONE
            }
            DownloadingStatus.STATE_STOP -> {
                holder.getView<AppCompatImageView>(R.id.iv_downloading_status)
                    .setImageResource(R.mipmap.icon_downloading_pause)
                holder.setText(R.id.tv_downloading_status, "已暂停")
                holder.getView<BGAProgressBar>(R.id.pb_downloading).visibility = View.GONE
            }
            DownloadingStatus.STATE_PRE, DownloadingStatus.STATE_POST_PRE, DownloadingStatus.STATE_RUNNING -> {
                holder.getView<AppCompatImageView>(R.id.iv_downloading_status)
                    .setImageResource(R.mipmap.icon_downloading_wait)
                holder.setText(R.id.tv_downloading_status, "下载中")
                holder.getView<BGAProgressBar>(R.id.pb_downloading).visibility = View.VISIBLE
                holder.getView<BGAProgressBar>(R.id.pb_downloading).progress =
                    item.downloading_progress!!
            }
        }

    }

    //创建类实现onClickListener方法
    class MyClick(val position: Int, val adapter: DownloadingAdapter) : View.OnClickListener {
        override fun onClick(v: View?) {
            if (!NetUtils.isNetworkConnected(adapter.context)) {
                ToastUtil.showToast("网络异常")
                return
            }
            val item = adapter.getItem(position)
            when (item.downloading_status) {
                DownloadingStatus.STATE_PRE, DownloadingStatus.STATE_POST_PRE, DownloadingStatus.STATE_WAIT, DownloadingStatus.STATE_RUNNING -> {
                    DownloadService.pauseTask(item.downloading_id ?: -1)
                    item.downloading_status = DownloadingStatus.STATE_STOP
                    adapter.notifyItemChanged(position)
                }
                DownloadingStatus.STATE_OTHER, DownloadingStatus.STATE_FAIL, DownloadingStatus.STATE_STOP -> {
                    adapter.singleDownload(item)
                    item.downloading_status = DownloadingStatus.STATE_RUNNING
                    adapter.notifyItemChanged(position)
                }
            }
        }


    }

    /**
     * 所有重新下载
     */
    fun allReDownload() {
        if (!data.isNullOrEmpty()) {
            for (bean in data) {
                singleDownload(bean)
            }
        }
    }

    /**
     * 单个重新下载
     */
    private fun singleDownload(item: GetListResponse) {
        if (item.downloading_status == DownloadingStatus.STATE_FAIL || item.downloading_id ?: 0 <= 0) {
            val urls = arrayListOf(item.mp3_url.toString(), item.album_cover.toString())
            val fileNames = arrayListOf("voice.mp3", "cover.jpg")
            val alias = item.program_id.toString()
            DownloadService.contentMap?.put(alias, item)
            DownloadService.start(urls, fileNames, alias)
        } else {
            val programId = if (item.album_type == 2) {
                item.album_id.toString()
            } else {
                item.program_id.toString()
            }
            //如果下载过程中退出app或者crash会导致contentMap为空，因为下载成功回调需要通过contentMap作判断，所以需要重新赋值
            if (DownloadService.contentMap?.containsKey(programId) == false) {
                DownloadService.contentMap?.put(item.program_id.toString(), item)
            }
            DownloadService.resumeTask(item.downloading_id)
        }
    }

    /**
     * 删除所有文件和数据库
     */
    fun deleteAll() {
        for (bean in data) {
            deleteSingle(bean.program_id)
        }
    }

    /**
     * 删除单个文件和数据库
     */
    private fun deleteSingle(programId: Int?) {
        //删除广告资源
        val singleAd =
            RoomManager.getInstance(context)
                .loadSingleAd(programId)
        if (singleAd?.mp3_url != null && !singleAd.mp3_url!!.startsWith("http")) {
            val file = File(singleAd.mp3_url!!)
            DownloadFileUtil().delete(file.parent!!)
        }
        //删除内容资源
        val singleContent =
            RoomManager.getInstance(context)
                .loadSingleListenCommon(programId)
        if (singleContent?.mp3_url != null && !singleContent.mp3_url!!.startsWith("http")) {
            val file = File(singleContent.mp3_url!!)
            DownloadFileUtil().delete(file.parent!!)
        }
        //删除数据库数据
        RoomManager.getInstance(context).deleteListenCommon(singleContent)
    }
}

interface DownloadingAdapterCallBack {
    fun deleteComplete()
}