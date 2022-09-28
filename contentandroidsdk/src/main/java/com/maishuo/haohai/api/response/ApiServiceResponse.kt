package com.maishuo.haohai.api.response

import android.os.Parcelable
import com.maishuo.haohai.api.bean.DownloadingStatus
import kotlinx.parcelize.Parcelize

data class InitIndexResponse(
    var ad_information_interval: Int? = null,
    var ad_search_interval: Int? = null,
    var agreement_url: String? = null,
    var agreement_v: Int? = null,
    var android_a_sw: Int? = null,
    var company: Company? = null,
    var ios_a_sw: Int? = null,
    var nav_switch: List<NavSwitch>? = null,
    var personal_information_assistance_right: String? = null,
    var toutiao_play_num_interval: Int? = null,
    var toutiao_position_ad_interval: Int? = null,
    var token: String? = null,
    var current_content_show_ad_position: Int? = null,
    var a_list: AdBean? = null
)

data class AdBean(
    var first_listen: String? = null,
    var information_flow: String? = null
)

data class Company(
    var `1`: String? = null,
    var `2`: String? = null
)

data class NavSwitch(
    var id: Int? = null,
    var name: String? = null,
    var nav_data_view_type: Int? = null
)

@Parcelize
data class GetListResponse(
    var album_cover: String? = null,
    var album_id: Int? = null,
    var album_lid: String? = null,
    var album_name: String? = null,
    var album_status: Int? = null,
    var album_type: Int? = null,
    var anchor_name: String? = null,
    var author_name: String? = null,
    var company: Int? = null,
    var keep_status: Int? = null,
    var play_volume: String? = null,
    var program_num: Int? = null,
    var summary: String? = null,
    var update_status: Int? = null,
    var upprogram_num: Int? = null,
    var program_id: Int? = null,
    var program_lid: String? = null,
    var program_name: String? = null,
    var mp3_url: String? = null,
    var program_au: Int? = null,
    var duration: Long? = null,
    //一下为自定义字段,非后台返回
    //是否为广告
    var isAd: Int? = null,
    //是否下载选中 默认：1.选中下载,2.下载中,3下载完成
    var download_status: Int? = 1,
    //下载中页面id
    var downloading_id: Long? = -1,
    //下载中页面进度
    var downloading_progress: Int? = 0,
    //下载中页面状态
    var downloading_status: Int? = DownloadingStatus.STATE_FAIL,
    //广告类json字符串
    var ad_json: String? = null,
    //播放历史新加
    var last_program_id: String? = null,
    var play_second: String? = null,
    var last_listen_time: String? = null,
    var showCheck: Boolean? = false,//用于标记是否显示
    var isCheck: Boolean? = false,//用于标记是否选中
    var isCeiling: Boolean? = false//用于标记是否是吸顶item
) : Parcelable

data class VerifyCodeResponse(
    var code: String? = null
)

data class LoginResponse(
    var id: Int? = 0,
    var name: String? = null,
    var email: String? = null,
    var phone: String? = null,
    var avatar: String? = null,
    var api_token: String? = null,
    var balance: Int? = null,
    var created_at: String? = null,
    var updated_at: String? = null,
    var lid: String? = null
)

data class LogoutResponse(
    var msg: String? = null,
)

data class GetProgramListResponse(
    var current_page: Int? = null,
    var data: MutableList<GetListResponse>? = null,
    var section_number_list: MutableList<String>? = null
)

data class KeepResponse(
    var status: Int? = null
)

data class MyFavoriteListResponse(
    var current_page: Int? = null,
    var total: Int? = null,
    var data: MutableList<GetListResponse>? = null,
)

data class InterestResponse(
    var id: Int? = 0,
    var tags_name: String? = null,
    var status: Int? = null,
)

@Parcelize
data class RecommendListResponse(
    var topic_name: String? = null,
    var list: MutableList<GetListResponse>? = null
) : Parcelable