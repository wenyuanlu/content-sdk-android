package com.qichuang.commonlibs.basic

import androidx.viewbinding.ViewBinding
import com.maishuo.haohai.databinding.ViewCommonRefreshRecyclerLayoutBinding
import com.qichuang.commonlibs.utils.ToastUtil
import com.qichuang.commonlibs.widgets.refresh.CommonRefreshView

/**
 * 处理分页功能的basicFragment
 */
abstract class BasicPagingRefreshFragment<T:ViewBinding?,K> :
    CustomBaseFragment<T>(),
    BasicCommonRefreshListener<K> {

    private var pageNumber: Int = BasicCommonRefreshListener.DEFAULT_DATA_LIST_PAGE
    private var responseDatas: MutableList<K> = mutableListOf()
    private var commonRefreshView: CommonRefreshView? = null

    private var isRefreshSlide: Boolean = false
    private var isLoadMoreSlide: Boolean = false

    //滑动状态
    private var slideStatus: Int = BasicCommonRefreshListener.SLIDE_STATUS_DEFAULT

    override fun fetchPageSlideStatus(): Int {
        return slideStatus
    }

    override fun initWidgetsEvent() {
        onRefreshEvent()
        initCommonWidgetEvent()

        commonRefreshView =fetchRefreshView()

        commonRefreshView?.setOnRefreshListener {
            onRefreshEvent()
        }

        commonRefreshView?.setOnLoadMoreListener {
            onLoadMoreEvent()
        }
    }

    //调用下拉刷新
    override fun onRefreshEvent() {
        if (isRefreshSlide) {
            isRefreshSlide = false
            commonRefreshView?.finishRefresh()
            return
        }

        slideStatus = BasicCommonRefreshListener.SLIDE_STATUS_REFRESH
        pageNumber = BasicCommonRefreshListener.DEFAULT_DATA_LIST_PAGE
        onDataResponseEvent(pageNumber, BasicCommonRefreshListener.DEFAULT_PAGING_LIST_SIZE)
        isRefreshSlide = true
    }

    //调用上拉加载更多
    override fun onLoadMoreEvent() {
        if (isLoadMoreSlide) {
            isLoadMoreSlide = false
            commonRefreshView?.finishLoadMore()
            return
        }

        slideStatus = BasicCommonRefreshListener.SLIDE_STATUS_LOAD_MORE
        pageNumber = pageNumber.inc()
        onDataResponseEvent(pageNumber, BasicCommonRefreshListener.DEFAULT_PAGING_LIST_SIZE)
        isLoadMoreSlide = true
    }

    //处理默认的请求返回
    override fun defaultDataResponse(datas: MutableList<K>?) {
        if (BasicCommonRefreshListener.SLIDE_STATUS_LOAD_MORE != slideStatus) {
            responseDatas.clear()
        }

        val responses:MutableList<K> = mutableListOf()

        if(!responseDatas.isNullOrEmpty()){
            responses.addAll(responseDatas)
        }

        if (!datas.isNullOrEmpty()) {
            responses.addAll(datas)
        }

        dataResponseObserver(responses)
        responseDatas = responses

        slideStatus = BasicCommonRefreshListener.SLIDE_STATUS_DEFAULT
    }

    //处理刷新请求返回
    override fun refreshDataResponse(datas: MutableList<K>?) {
        isRefreshSlide = false
        commonRefreshView?.finishRefresh()
        defaultDataResponse(datas)
    }

    //处理上拉加载更多请求返回
    override fun loadMoreDataResponse(datas: MutableList<K>?) {
        isLoadMoreSlide = false
        commonRefreshView?.finishLoadMore()
        if (datas.isNullOrEmpty()) {
            ToastUtil.showToast("暂无更多数据")
            return
        }

        defaultDataResponse(datas)
    }

    //再请求返回时调用的处理请求返回
    override fun addRemoteDataResponse(datas: MutableList<K>?) {
        when (slideStatus) {
            BasicCommonRefreshListener.SLIDE_STATUS_REFRESH -> {
                refreshDataResponse(datas)
            }
            BasicCommonRefreshListener.SLIDE_STATUS_LOAD_MORE -> {
                loadMoreDataResponse(datas)
            }
            BasicCommonRefreshListener.SLIDE_STATUS_DEFAULT -> {
                defaultDataResponse(datas)
            }
        }
    }

    override fun resetDefaultStatus() {
        isRefreshSlide = false
        isLoadMoreSlide = false
        slideStatus = BasicCommonRefreshListener.SLIDE_STATUS_DEFAULT
        commonRefreshView?.finishRefresh()
        commonRefreshView?.finishLoadMore()
    }
}