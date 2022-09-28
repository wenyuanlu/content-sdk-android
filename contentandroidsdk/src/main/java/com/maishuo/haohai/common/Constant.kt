package com.maishuo.haohai.common

import com.maishuo.haohai.api.response.GetListResponse

/**
 * author : xpSun
 * date : 11/11/21
 * description :
 */
object Constant {

    const val LIVE_DATA_STATUS_SUCCESS = 0x1001
    const val LIVE_DATA_STATUS_FAIL = 0x1002

    const val COMMON_ADID = "E6FFAF53179D5035A2B9045357AA42DD"
    const val COMMON_FIRST_AD_ID = "309CC398106D1821805A8CCD255260C5"

    //1小说 2头条 3课程
    const val VOICE_NOVEL_TAG: Int = 1
    const val HEADER_LINES_TAG: Int = 2
    const val VOICE_CURRICULUM_TAG: Int = 3

    //0.默认,1自动播放,2.上一页,3滑动到特定位置需要自动加载下一页
    const val PLAYER_STATUS_TAG_0 = 0
    const val PLAYER_STATUS_TAG_1 = 1
    const val PLAYER_STATUS_TAG_2 = 2
    const val PLAYER_STATUS_TAG_3 = 3

    //1.默认,2下载中,3下载完成
    const val COMMON_DOWNLOAD_STATUS_1 = 1
    const val COMMON_DOWNLOAD_STATUS_2 = 2
    const val COMMON_DOWNLOAD_STATUS_3 = 3

    var isOpenPlayerPager: Boolean? = null

    //当前正在播放的对象
    var response: GetListResponse? = null

    //当前正在播放的列表
    var responses: MutableList<GetListResponse>? = null

    var currentPlayerAdPositionId: Int? = null

    //当前子章节区间下标 从0 开始
    var currentChildPosition: Int? = null

    //当前子章节位于列表的下标 从0 开始
    var currentChildInPlayerListPosition: Int? = null

    const val ON_AD_SHOW_STATUS_0: Int = 0
    const val ON_AD_SHOW_STATUS_1: Int = 1
    const val ON_AD_SHOW_STATUS_2: Int = 2

    //当前广告展示的状态,0.未展示过,1.正在展示,2.展示结束(已展示过)
    var onADShowStatus: Int = ON_AD_SHOW_STATUS_0
}