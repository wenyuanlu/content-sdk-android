package com.qichuang.roomlib.entity

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

/**
 * author ：Seven
 * date : 12/21/21
 * description :广告表
 */
@Entity(tableName = "ad")
class AdEntity {
    @PrimaryKey()
    var program_id: Int? = null//章节id
    var mp3_url: String? = null//广告音频地址
    var ad_json: String? = null//广告类json
    var createTime: Long? = null
}
