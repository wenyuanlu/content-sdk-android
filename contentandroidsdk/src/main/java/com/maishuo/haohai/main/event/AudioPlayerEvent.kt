package com.maishuo.haohai.main.event

import com.maishuo.haohai.api.response.GetListResponse
import com.maishuo.haohai.api.response.GetProgramListResponse

data class GetProgramListResponseEvent(
    var data: GetProgramListResponse? = null,
    var playerStatus: Int? = null
)

data class GetProgramListParamEvent(
    var album_id: Int? = null,
    var page_size: Int? = null,
    var page: Int? = null,
    var playerStatus: Int? = null//0.默认,1自动播放,2.上一页,3滑动到特定位置需要自动加载下一页
)

data class OnAdProgressEvent(
    var progress: Int? = null,
    var interaction: Int? = null,
    var isCompletion: Boolean? = null
)

data class SynchronizationCurrentChildPositionEvent(
    var currentChildPosition: Int? = null
)

data class CatalogueClickEvent(
    var item: GetListResponse? = null,
    var responses: MutableList<GetListResponse>? = null,
    var position: Int? = null
)

data class RecommendClickEvent(
    var item: GetListResponse? = null
)