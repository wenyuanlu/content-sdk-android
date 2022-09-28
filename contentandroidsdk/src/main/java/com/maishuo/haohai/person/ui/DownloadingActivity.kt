package com.maishuo.haohai.person.ui

import androidx.recyclerview.widget.LinearLayoutManager
import com.arialyy.aria.core.task.DownloadGroupTask
import com.maishuo.haohai.R
import com.maishuo.haohai.api.bean.DownloadingStatus
import com.maishuo.haohai.api.response.GetListResponse
import com.maishuo.haohai.databinding.ActivityDownloadingBinding
import com.maishuo.haohai.listener.OnDialogCallBackListener
import com.maishuo.haohai.person.adapter.DownloadingAdapter
import com.maishuo.haohai.person.adapter.DownloadingAdapterCallBack
import com.maishuo.haohai.person.service.DownloadCallBack
import com.maishuo.haohai.person.service.DownloadService
import com.maishuo.haohai.utils.DialogUtils
import com.qichuang.commonlibs.basic.CustomBaseActivity
import com.qichuang.commonlibs.utils.GsonUtils
import com.qichuang.commonlibs.utils.NetUtils
import com.qichuang.commonlibs.utils.ToastUtil
import com.qichuang.roomlib.RoomManager
import com.qichuang.roomlib.entity.ListenCommonEntity


class DownloadingActivity : CustomBaseActivity<ActivityDownloadingBinding>(),
    DownloadingAdapterCallBack, DownloadCallBack {

    private var adapter: DownloadingAdapter? = null

    override fun initWidgets() {
        setTitle("下载中")
        DownloadService.start()
        DownloadService.downloadCallBack = this
        initRecyclerView()
        initLocalData()
    }

    override fun initWidgetsEvent() {
        vb?.tvDownloadStart?.setOnClickListener {
            if (!NetUtils.isNetworkConnected(this)) {
                ToastUtil.showToast("网络异常")
            } else {
                if (vb?.tvDownloadStart?.text == "全部开始") {
                    vb?.tvDownloadStart?.text = "全部暂停"
                    vb?.ivDownloadStart?.setImageResource(R.mipmap.icon_download_pause)
                    //设置页面为等待状态
                    updateDownloadStatus(DownloadingStatus.STATE_WAIT)
                    //设置任务为等待状态
                    DownloadService.waitTask(0)
                    if (adapter != null) {
                        adapter?.allReDownload()
                    }
                } else {
                    vb?.tvDownloadStart?.text = "全部开始"
                    vb?.ivDownloadStart?.setImageResource(R.mipmap.icon_download_play)
                    DownloadService.pauseTask(0)
                    updateDownloadStatus(DownloadingStatus.STATE_STOP)
                }
            }
        }
        vb?.tvDownloadDelete?.setOnClickListener {
            DialogUtils.showCommonDialog(
                this,
                "",
                "确定要删除全部下载任务？",
                true,
                object : OnDialogCallBackListener {
                    override fun onConfirm(content: String?) {
                        DownloadService.cancelTask(0)
                        if (adapter != null) {
                            adapter?.deleteAll()
                            adapter?.clearData()
                        }
                        deleteComplete()
                    }

                    override fun onCancel() {}
                })
        }

    }

    /**
     * 单条删除完判断有无数据
     */
    override fun deleteComplete() {
        if (adapter != null && adapter?.data.isNullOrEmpty()) {
            vb?.recyclerView?.handleFailure("暂无数据")
        }
    }

    private fun initRecyclerView() {
        vb?.recyclerView?.setLayoutManager(LinearLayoutManager(this))
        vb?.recyclerView?.setRefreshEnable(false)
        vb?.recyclerView?.setLoadMoreEnable(false)
        adapter = DownloadingAdapter(this)
        vb?.recyclerView?.setAdapter(adapter)
    }

    private fun initLocalData() {
        val resultData = mutableListOf<GetListResponse>()
        val tasks = DownloadService.getTaskList()
        if (!tasks.isNullOrEmpty()) {
            for (task in tasks) {
                if (task.state == DownloadingStatus.STATE_RUNNING) {
                    vb?.tvDownloadStart?.text = "全部暂停"
                    vb?.ivDownloadStart?.setImageResource(R.mipmap.icon_download_pause)
                }
                if (task.state != 1) {
                    val entity =
                        RoomManager.getInstance(this).loadSingleFailListenCommon(task.alias.toInt())
                    if (entity != null) {
                        val bean = entityToGetListResponse(entity)
                        bean.downloading_id = task.id
                        bean.downloading_status = task.state
                        resultData.add(bean)
                    }
                }
            }
        }
        //如果下载任务列表为空则直接取出数据库下载失败的数据
        //如果下载任务列表不为空则取出数据库下载失败的数据并赋于任务列表的状态和id
        if (resultData.isNullOrEmpty()) {
            resultData.addAll(entityToGetListResponses())
        } else {
            val list = entityToGetListResponses()
            for (bean in list) {
                for (index in resultData.indices) {
                    if (resultData[index].program_id == bean.program_id) {
                        break
                    }
                    if (index == resultData.size.dec()) {
                        resultData.add(bean)
                    }
                }
            }
        }
        if (!resultData.isNullOrEmpty()) {
            vb?.recyclerView?.start = 1
            vb?.recyclerView?.handleSuccess(adapter, resultData)
        }
    }

    override fun downloadRunning(task: DownloadGroupTask?) {
        if (vb?.tvDownloadStart?.text == "全部开始") {
            vb?.tvDownloadStart?.text = "全部暂停"
            vb?.ivDownloadStart?.setImageResource(R.mipmap.icon_download_pause)
        }
        if (adapter != null && task != null) {
            val list = adapter?.data
            if (!list.isNullOrEmpty()) {
                for (bean in list) {
                    if (bean.program_id.toString() == task.entity?.alias) {
                        val position = adapter?.getItemPosition(bean)
                        if (position != null) {
                            bean.downloading_status = task.state
                            bean.downloading_progress = task.percent
                            adapter?.notifyItemChanged(position)
                        }
                        break
                    }
                }
            }
        }
    }

    override fun downloadComplete(alias: String?) {
        if (vb?.tvDownloadStart?.text == "全部暂停") {
            vb?.tvDownloadStart?.text = "全部开始"
            vb?.ivDownloadStart?.setImageResource(R.mipmap.icon_download_play)
        }
        if (adapter != null) {
            val list = adapter?.data
            if (!list.isNullOrEmpty()) {
                for (bean in list) {
                    if (bean.program_id.toString() == alias) {
                        adapter?.remove(bean)
                        break
                    }
                }
                deleteComplete()
            }
        }
    }

    override fun downloadFail(alias: String?) {
        if (vb?.tvDownloadStart?.text == "全部暂停") {
            vb?.tvDownloadStart?.text = "全部开始"
            vb?.ivDownloadStart?.setImageResource(R.mipmap.icon_download_play)
        }
        if (adapter != null) {
            val list = adapter?.data
            if (!list.isNullOrEmpty()) {
                for (bean in list) {
                    val position = adapter?.getItemPosition(bean)
                    if (bean.program_id.toString() == alias) {
                        if (position != null) {
                            if (bean.downloading_status == DownloadingStatus.STATE_STOP) {
                                bean.downloading_status = DownloadingStatus.STATE_STOP
                            } else {
                                bean.downloading_status = DownloadingStatus.STATE_FAIL
                            }
                            adapter?.notifyItemChanged(position)
                        }
                        break
                    }
                }
            }
        }
    }

    private fun updateDownloadStatus(status: Int?) {
        if (adapter != null) {
            val list = adapter?.data
            if (!list.isNullOrEmpty()) {
                for (bean in list) {
                    val position = adapter?.getItemPosition(bean)
                    if (position != null) {
                        if (bean.downloading_status == DownloadingStatus.STATE_FAIL) {
                            bean.downloading_id = -1
                        }
                        bean.downloading_status = status
                        adapter?.notifyItemChanged(position)
                    }
                }
            }
        }
    }

    private fun entityToGetListResponses(): MutableList<GetListResponse> {
        val changeList: MutableList<GetListResponse> = mutableListOf()
        val list = RoomManager.getInstance(this).loadAllFailListenCommon()
        if (!list.isNullOrEmpty()) {
            for (bean in list) {
                changeList.add(entityToGetListResponse(bean))
            }
        }
        return changeList
    }

    private fun entityToGetListResponse(entity: ListenCommonEntity?): GetListResponse {
        val jsonValue = GsonUtils.toJson(entity)
        return GsonUtils.fetchGson().fromJson(jsonValue, GetListResponse::class.java)
    }

}