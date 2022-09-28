package com.qichuang.roomlib.entity

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

/**
 * author ：Seven
 * date : 11/26/21
 * description :收听历史表
 */
@Entity(tableName = "listen_common")
class ListenCommonEntity {
    var album_id: Int? = null//小说、头条、课程 ID
    @PrimaryKey()
    var program_id: Int? = null//章节id
    var album_type: Int? = null//听小说，听头条，听课程
    var album_name: String? = null
    var album_cover: String? = null
    var program_au: Int? = null//章节数
    var program_name: String? = null//章节名
    var program_num: Int? = null//章节总数
    var summary: String? = null
    var author_name: String? = null//作者
    var anchor_name: String? = null//朗读者
    var mp3_url: String? = null
    var keep_status: Int? = null//收藏
    var ad_json: String? = null//广告类json
    var createTime: Long? = null
    //是否下载选中 默认：1.选中下载,2.下载中,3下载完成
    var download_status: Int? = 1
    @Ignore
    var count: Int? = null//记录章节数量
    @Ignore
    var showCheck: Boolean? = false//用于标记是否显示
    @Ignore
    var isCheck: Boolean? = false//用于标记是否选中

}
