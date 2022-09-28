package com.maishuo.haohai.main.ui

import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.maishuo.haohai.R
import com.maishuo.haohai.api.response.GetListResponse
import com.maishuo.haohai.common.Constant
import com.maishuo.haohai.databinding.ViewHomeMainRefreshLayoutBinding
import com.maishuo.haohai.main.adapter.HomeMainVoiceCurriculumAdapter
import com.maishuo.haohai.main.viewmodel.HomeMainViewModel
import com.qichuang.commonlibs.basic.BasicCommonRefreshListener
import com.qichuang.commonlibs.basic.BasicPagingRefreshFragment
import com.qichuang.commonlibs.utils.NetUtils
import com.qichuang.commonlibs.widgets.refresh.CommonRefreshView

/**
 * author : xpSun
 * date : 11/8/21
 * description :听课程
 */
class HomeMainVoiceCurriculumFragment :
        BasicPagingRefreshFragment<ViewHomeMainRefreshLayoutBinding, GetListResponse> {

    private var adapter: HomeMainVoiceCurriculumAdapter? = null
    private var viewModel: HomeMainViewModel? = null
    private var onItemClickChildPosition: Int? = null
    private var layoutManager: LinearLayoutManager? = null
    private var onResponseListener: ((response: GetListResponse?) -> Unit)? = null

    var apiResponses: MutableList<GetListResponse>? = null
        set(value) {
            field = value
            adapter?.setList(value)

            if (null == Constant.response && !value.isNullOrEmpty()) {
                onResponseListener?.invoke(value[0])
            }
        }

    constructor()

    constructor(listener: ((response: GetListResponse?) -> Unit)?) {
        onResponseListener = listener
    }

    override fun initWidgets() {
        var appCompatActivity: AppCompatActivity? = null
        if (activity is AppCompatActivity) {
            appCompatActivity = activity as AppCompatActivity
        }
        viewModel = HomeMainViewModel(appCompatActivity)
        viewModel?.let {
            it.fetchMainListLiveData.observe(this) { responseIt ->
                if (Constant.LIVE_DATA_STATUS_SUCCESS == responseIt.status) {
                    addRemoteDataResponse(responseIt.data)
//                    val adInterval = PreferencesUtils.getInt(PreferencesKey.AD_INFORMATION_INTERVAL)
//                    viewModel?.initResponseAd(responseIt.data, adInterval)
                } else {
                    resetDefaultStatus()
                }
            }
            it.keepLiveData.observe(this) {
                adapter?.data?.let { dataIt ->
                    val item = dataIt[onItemClickChildPosition ?: 0]
                    item.keep_status = if (Constant.LIVE_DATA_STATUS_SUCCESS == it.status) {
                        it.data?.status ?: 0
                    } else {
                        0
                    }
                    adapter?.notifyItemChanged(onItemClickChildPosition ?: 0)
                }
            }
            it.initResponseAdLiveData.observe(this) { responseIt ->
                addRemoteDataResponse(responseIt)
            }
        }

        layoutManager = LinearLayoutManager(context)
        vb?.homeMainRecyclerView?.layoutManager = layoutManager
        adapter = HomeMainVoiceCurriculumAdapter()
        vb?.homeMainRecyclerView?.adapter = adapter

        vb?.homeMainRefreshView?.setEnableLoadMore(NetUtils.isNetworkConnected(context))
    }

    override fun initCommonWidgetEvent() {
        adapter?.setOnItemClickListener { adapter, _, position ->
            if (adapter.data.isNullOrEmpty()) {
                return@setOnItemClickListener
            }
            val item = adapter.data[position]

            if (item is GetListResponse) {
                viewModel?.onItemClickClickListener(Constant.VOICE_CURRICULUM_TAG, item)
            }
        }

        adapter?.addChildClickViewIds(R.id.curriculum_item_keep_status)
        adapter?.setOnItemChildClickListener { adapter, view, position ->
            if (adapter.data.isNullOrEmpty()) {
                return@setOnItemChildClickListener
            }

            onItemClickChildPosition = position
            val item = adapter.data[position]

            if (item is GetListResponse) {
                if (R.id.curriculum_item_keep_status == view.id) {
                    viewModel?.keep(item.album_id, 0)
                }
            }
        }

        vb?.homeMainRecyclerView?.setOnRefreshClickListener {
            viewModel?.fetchMainList(
                    Constant.VOICE_CURRICULUM_TAG,
                    "",
                    BasicCommonRefreshListener.DEFAULT_PAGING_LIST_SIZE,
                    BasicCommonRefreshListener.DEFAULT_DATA_LIST_PAGE
            )
        }
    }

    override fun dataResponseObserver(datas: MutableList<GetListResponse>?) {
        vb?.homeMainRefreshView?.setEnableLoadMore(NetUtils.isNetworkConnected(context))
        apiResponses = datas
        adapter?.setNewInstance(datas)
    }

    override fun onDataResponseEvent(pageNumber: Int, pageSize: Int) {
        if (NetUtils.isNetworkConnected(context)) {
            viewModel?.fetchMainList(
                    Constant.VOICE_CURRICULUM_TAG,
                    "",
                    pageSize,
                    pageNumber
            )
        } else {
            viewModel?.fetchOffLineDatas(Constant.VOICE_CURRICULUM_TAG) {
                addRemoteDataResponse(it)
            }
        }
    }

    override fun fetchRefreshView(): CommonRefreshView? {
        return vb?.homeMainRefreshView
    }
}