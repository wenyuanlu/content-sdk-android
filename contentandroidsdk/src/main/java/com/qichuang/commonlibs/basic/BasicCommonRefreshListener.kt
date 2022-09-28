package com.qichuang.commonlibs.basic

import com.qichuang.commonlibs.widgets.refresh.CommonRefreshView

interface BasicCommonRefreshListener<T> : BasicCommonListener {

    companion object {
        //统一的页码
        const val DEFAULT_DATA_LIST_PAGE: Int = 1

        //统一的总页数
        const val DEFAULT_PAGING_LIST_SIZE: Int = 10

        //滑动状态-下拉刷新
        const val SLIDE_STATUS_REFRESH: Int = 0

        //滑动状态-上拉加载更多
        const val SLIDE_STATUS_LOAD_MORE: Int = 1

        //滑动状态-默认不滑动
        const val SLIDE_STATUS_DEFAULT: Int = -1
    }

    //子界面处理事件
    fun initCommonWidgetEvent()

    //获取滑动状态
    fun fetchPageSlideStatus(): Int

    //对于数据处理统一的出口
    fun dataResponseObserver(datas: MutableList<T>?)

    //对于请求前事件处理的统一出口
    fun onDataResponseEvent(pageNumber: Int, pageSize: Int)

    //获取CommonRefreshView
    fun fetchRefreshView(): CommonRefreshView?

    //调用下拉刷新
    fun onRefreshEvent()

    //调用上拉加载更多
    fun onLoadMoreEvent()

    //处理刷新请求返回
    fun refreshDataResponse(datas: MutableList<T>?)

    //处理下拉更多请求返回
    fun loadMoreDataResponse(datas: MutableList<T>?)

    //处理默认的请求返回
    fun defaultDataResponse(datas: MutableList<T>?)

    //处理请求返回时调用的处理请求
    fun addRemoteDataResponse(datas: MutableList<T>?)

    //重置到默认状态
    fun resetDefaultStatus()

}