package com.maishuo.haohai.person.viewmodel

import androidx.lifecycle.MutableLiveData
import com.maishuo.haohai.api.bean.SearchResult
import com.maishuo.haohai.api.response.GetListResponse
import com.maishuo.haohai.api.retrofit.ApiService
import com.qichuang.commonlibs.basic.BaseViewModel
import com.qichuang.commonlibs.basic.BasicCommonRefreshListener.Companion.DEFAULT_DATA_LIST_PAGE
import com.qichuang.retrofitlibs.retrofit.CommonObserver


/**
 * author : luo
 * date : 11/15/21
 * description :
 */
class SearchViewModel : BaseViewModel() {

    var searchLiveData = MutableLiveData<SearchResult>()

    fun fetchMainList(
        albumType: Int?,
        programName: String?,
        pageSize: Int?,
        page: Int?
    ) {
        ApiService.instance.fetMainList(albumType, programName, pageSize, page)
            .subscribe(object : CommonObserver<MutableList<GetListResponse>>(true) {
                override fun onResponseSuccess(response: MutableList<GetListResponse>?) {
                    if (response?.isEmpty() == true && page == DEFAULT_DATA_LIST_PAGE) {
                        searchLiveData.postValue(SearchResult(false, "暂无数据", null))
                    } else {
                        searchLiveData.postValue(
                            SearchResult(
                                true,
                                "",
                                response
                            )
                        )
                    }
                }

                override fun onResponseError(message: String?, e: Throwable?, code: Int?) {
                    super.onResponseError(message, e, code)
                    searchLiveData.postValue(SearchResult(false, message, null))
                }
            })
    }

}