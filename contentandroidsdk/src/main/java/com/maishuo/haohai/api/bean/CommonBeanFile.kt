package com.maishuo.haohai.api.bean

import android.os.Parcelable
import com.maishuo.haohai.api.response.GetListResponse
import kotlinx.parcelize.Parcelize

data class DialogBottomMoreBean(
    var type: Int? = null,
    var text: String? = null,
    var reportType: Int? = null,
    var isSelect: Boolean? = null,
    var time: Int? = null
)

@Parcelize
data class GetListResponsePackBean(
    var datas: MutableList<GetListResponse>? = null
) : Parcelable

data class KeepResponseEvent(
    var channelType: Int? = null,
    var album_id: Int? = null,
    var status: Int? = null
)

data class MyFavoriteCountEvent(
    var count: Int? = null,
)

class DownloadingStatus {
    companion object {
        /**
         * 其它状态
         */
        val STATE_OTHER: Int = -1

        /**
         * 失败状态
         */
        val STATE_FAIL: Int = 0

        /**
         * 完成状态
         */
        val STATE_COMPLETE: Int = 1

        /**
         * 停止状态
         */
        val STATE_STOP: Int = 2

        /**
         * 等待状态
         */
        val STATE_WAIT: Int = 3

        /**
         * 正在执行
         */
        val STATE_RUNNING: Int = 4

        /**
         * 预处理
         */
        val STATE_PRE: Int = 5

        /**
         * 预处理完成
         */
        val STATE_POST_PRE: Int = 6

        /**
         * 删除任务
         */
        val STATE_CANCEL: Int = 7
    }
}