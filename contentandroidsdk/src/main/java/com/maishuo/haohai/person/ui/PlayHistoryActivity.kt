package com.maishuo.haohai.person.ui

import android.annotation.SuppressLint
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.gyf.immersionbar.ImmersionBar
import com.maishuo.haohai.api.response.GetListResponse
import com.maishuo.haohai.databinding.ActivityPlayHistoryBinding
import com.maishuo.haohai.person.adapter.PlayHistoryAdapter
import com.maishuo.haohai.person.adapter.PlayHistoryAdapterCallBack
import com.maishuo.haohai.person.viewmodel.PlayHistoryViewModel
import com.qichuang.commonlibs.basic.CustomBaseActivity
import com.qichuang.roomlib.entity.ListenCommonEntity

class PlayHistoryActivity : CustomBaseActivity<ActivityPlayHistoryBinding>(),
    PlayHistoryAdapterCallBack {
    private var adapter: PlayHistoryAdapter? = null
    private var itemCheckBoxChangeTag: Boolean? = false

    var viewModel: PlayHistoryViewModel? = null

    override fun initWidgets() {
        ImmersionBar.with(this).init()
        initRecyclerView()
        initViewModel()
        getPlayHistoryList()
    }

    override fun initWidgetsEvent() {
        adapter?.setOnItemClickListener { adapter, _, position ->
            val bean = adapter.getItem(position) as ListenCommonEntity

        }
        vb?.let {
            it.playHistoryIvBack.setOnClickListener {
                finish()
            }
            it.playHistoryIvDelete.setOnClickListener { _ ->
                it.playHistoryIvDelete.visibility = View.GONE
                it.playHistoryTvTitle.text = "批量删除"
                it.playHistoryRlContainer.visibility = View.VISIBLE
                setCheckBoxStatus(showCheck = true, isCheck = false)
            }
            it.playHistoryTvCancel.setOnClickListener { _ ->
                it.playHistoryIvDelete.visibility = View.VISIBLE
                it.playHistoryTvTitle.text = "播放历史"
                it.playHistoryRlContainer.visibility = View.GONE
                setCheckBoxStatus(showCheck = false, isCheck = false)

            }
            it.playHistoryCb.setOnCheckedChangeListener { _, isChecked ->
                if (itemCheckBoxChangeTag == false) {
                    setCheckBoxStatus(true, isChecked)
                }
            }
            it.playHistoryTvDelete.setOnClickListener {
                adapter?.deleteMultipleItem()
            }
        }
    }

    private fun initRecyclerView() {
        vb?.playHistoryTvCancel?.isSelected = true
        vb?.recyclerView?.setLayoutManager(LinearLayoutManager(this))
        vb?.recyclerView?.setRefreshEnable(false)
        vb?.recyclerView?.setLoadMoreEnable(false)
        adapter = PlayHistoryAdapter(this)
        vb?.recyclerView?.setAdapter(adapter)
    }

    private fun initViewModel() {
        viewModel = ViewModelProvider(this).get(PlayHistoryViewModel::class.java)
    }

    /**
     * 获取标签列表
     */
    private fun getPlayHistoryList() {
        viewModel?.getPlayHistoryList(1)
        viewModel?.getPlayHistoryLiveData?.observe(this) {
            if (it.isNullOrEmpty()) {
                vb?.recyclerView?.handleFailure("暂无数据")
                vb?.playHistoryIvDelete?.visibility = View.GONE
            } else {
                var itemDate = ""
                val newBean = GetListResponse()
                newBean.isCeiling = true
                for (index in it.indices) {
                    val dateArray = it[index].last_listen_time?.split(" ")
                    if (dateArray != null) {
                        if (itemDate != dateArray[0]) {
                            itemDate = dateArray[0]
                            newBean.last_listen_time = dateArray[0]
                            it.add(index, newBean)
                        }
                        it[index].last_listen_time = dateArray[0]
                    }
                }
                vb?.recyclerView?.handleSuccess(adapter, it)
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun setCheckBoxStatus(showCheck: Boolean, isCheck: Boolean) {
        val list = adapter?.data
        if (list != null) {
            for (bean in list) {
                bean.showCheck = showCheck
                bean.isCheck = isCheck
            }
        }
        adapter?.notifyDataSetChanged()
    }


    override fun initStatusBar(): Boolean {
        return false
    }

    override fun deleteCheckedItem(albumLids: String) {
        viewModel?.deletePlayHistory(1, albumLids)
        viewModel?.deletePlayHistoryLiveData?.observe(this) {
            if (it == true) {
                val list = adapter?.data
                if (list.isNullOrEmpty()) {
                    vb?.recyclerView?.handleFailure("暂无数据")
                    vb?.playHistoryIvDelete?.visibility = View.GONE
                    vb?.playHistoryRlContainer?.visibility = View.GONE
                    vb?.playHistoryTvTitle?.text = "播放历史"
                }
            }
        }
    }

    /**
     * item checkbox change
     */
    override fun checkBoxChange() {
        val list = adapter?.data
        if (!list.isNullOrEmpty()) {
            itemCheckBoxChangeTag = true
            vb?.playHistoryCb?.isChecked = true
            for (bean in list) {
                if (bean.isCheck == false) {
                    vb?.playHistoryCb?.isChecked = false
                    break
                }
            }
            itemCheckBoxChangeTag = false
        }
    }

}