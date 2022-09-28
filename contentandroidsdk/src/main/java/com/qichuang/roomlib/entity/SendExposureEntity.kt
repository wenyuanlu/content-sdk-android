package com.qichuang.roomlib.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * author : xpSun
 * date : 12/20/21
 * description : 发送曝光
 */
@Entity(tableName = "send_exposure")
class SendExposureEntity {
    @PrimaryKey(autoGenerate = true)
    var id: Long? = null
    var voice_type: Int? = null//音频类型：1-首听，2-渲染模板，3-内容
    var mid: String? = null
    var provider: Int? = null
    var uid: String? = null
    var type: Int? = null
    var chapterid: String? = null
    var event: String? = null
    var url: String? = null
}