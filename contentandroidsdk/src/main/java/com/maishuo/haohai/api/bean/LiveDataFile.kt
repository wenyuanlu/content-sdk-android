package com.maishuo.haohai.api.bean

import com.maishuo.haohai.api.response.GetListResponse

data class CustomLiveDataBean<T>(
    var status: Int? = null,
    var data: T? = null
)

data class LoginResult(
    var success: Boolean = false,
    var errorMsg: String? = ""
)

data class MyFavoriteResult(
    var success: Boolean = false,
    var message: String? = "",
    var result: MutableList<GetListResponse>?
)

data class SearchResult(
    var success: Boolean = false,
    var message: String? = "",
    var result: MutableList<GetListResponse>?
)

data class InterestResult(
    var success: Boolean = false,
    var message: String? = ""
)