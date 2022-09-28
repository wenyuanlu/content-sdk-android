package com.maishuo.haohai.api.retrofit

import com.maishuo.haohai.api.param.BindPhoneParam
import com.maishuo.haohai.api.param.LoginParam
import com.maishuo.haohai.api.param.UMengLoginParam
import com.maishuo.haohai.api.param.VerifyCodeParam
import com.maishuo.haohai.api.response.*
import com.qichuang.retrofitlibs.bean.BasicResponse
import io.reactivex.Observable
import retrofit2.Response
import retrofit2.http.*

interface ApiServiceInterface {

    //初始化
    @GET("init/sdkinit")
    fun initIndex(
        @Query("mid") mid: String?,
        @Query("content_id") content_id: String?
    ): Observable<BasicResponse<InitIndexResponse>>

    //首页列表
    @GET("getList")
    fun fetMainList(
        @Query("album_type") albumType: Int?,
        @Query("program_name") programName: String?,
        @Query("page_size") pageSize: Int?,
        @Query("page") page: Int?,
    ): Observable<BasicResponse<MutableList<GetListResponse>>>

    //收藏/取消收藏
    @GET("keep")
    fun keep(
        @Query("album_id") albumId: Int?,
        @Query("program_id") programId: Int?
    ): Observable<BasicResponse<KeepResponse>>

    //获取章节详情列表
    @GET("album/getProgramList")
    fun getProgramList(
        @Query("album_id") albumId: Int?,
        @Query("page_size") pageSize: Int?,
        @Query("page") page: Int?
    ): Observable<BasicResponse<GetProgramListResponse>>

    //获取验证码
    @POST("user/phoneRegCode")
    fun getVerifyCode(@Body body: VerifyCodeParam): Observable<BasicResponse<VerifyCodeResponse>>

    //登录
    @POST("user/register")
    fun login(@Body body: LoginParam): Observable<BasicResponse<LoginResponse>>

    //退出登录
    @GET("user/loginOut")
    fun logout(@Query("phone") phone: String?): Observable<BasicResponse<Any>>

    //换绑手机号
    @POST("user/bindPhone")
    fun bindPhone(@Body body: BindPhoneParam): Observable<BasicResponse<Any>>

    //播放界面推荐
    @GET("album/otherRec")
    fun otherRec(
        @Query("author_name") author_name: String?,
        @Query("album_type") album_type: Int?,
        @Query("album_id") album_id: Int?,
        @Query("page") page: Int?
    ): Observable<BasicResponse<MutableList<GetListResponse>>>

    //播放界面推荐
    @GET("news/otherRec")
    fun newsOtherRec(
        @Query("company") company: String?,
        @Query("page") page: Int?
    ): Observable<BasicResponse<MutableList<GetListResponse>>>

    //我的收藏
    @GET("getKeepList")
    fun getMyFavorite(
        @Query("page") page: Int?,
        @Query("album_type") albumType: Int?
    ): Observable<BasicResponse<MyFavoriteListResponse>>

    //友盟一键登录
    @POST("user/umengLogin")
    fun uMengLogin(@Body body: UMengLoginParam): Observable<BasicResponse<LoginResponse>>

    //异常上报(不删除只上报)
    @GET("putAberrant")
    fun putAberrant(
        @Query("album_type") album_type: Int?,
        @Query("album_id") album_id: Int?,
        @Query("program_id") program_id: Int?
    ): Observable<BasicResponse<Any>>

    //收听记录上报
    @GET("recordHistory")
    fun putListenHistory(
        @Query("album_id") albumId: Int?,
        @Query("program_id") programId: Int?
    ): Observable<BasicResponse<Any>>

    //收听记录上报
    @GET("news/recordHistory")
    fun putNewsListenHistory(
        @Query("album_id") albumId: Int?
    ): Observable<BasicResponse<Any>>

    //曝光监听
    @GET
    fun ctracr(
        @Url url: String,
        @Query("mid") mid: String?,
        @Query("provider") provider: Int?,
        @Query("uid") uid: String?,
        @Query("type") type: Int?,
        @Query("chapterid") chapterId: String?,
        @Query("event") event: String?
    ): Observable<BasicResponse<Response<Void>>>

    //课程书籍推荐
    @GET("recommend/RecommendList")
    fun recommendList(
        @Query("albumType") albumType: Int?,
        @Query("album_lid") album_lid: String?
    ): Observable<BasicResponse<MutableList<RecommendListResponse>>>

    //获取兴趣标签列表
    @GET("user/interestTag")
    fun getInterestTag(): Observable<BasicResponse<MutableList<InterestResponse>>>

    //提交选择的兴趣标签
    @POST("user/subInterestTag")
    fun puInterestTag(
        @Query("tags") tags: String?
    ): Observable<BasicResponse<Any>>

    //获取播放历史列表
    @GET("history/historyList")
    fun getPlayHistoryList(
        @Query("type") type: Int?
    ): Observable<BasicResponse<MutableList<GetListResponse>>>

    //删除播放历史
    @GET("history/delhistory")
    fun deletePlayHistory(
        @Query("type") type: Int?,
        @Query("albumLids") albumLids: String?
    ): Observable<BasicResponse<Any>>

    //播放上报
    @GET("history/playReport")
    fun playReport(
        @Query("album_type") albumType: Int?,
        @Query("album_lid") albumLid: String?,
        @Query("program_lid") programLid: String?,
        @Query("play_second") playSecond: Int?
    ): Observable<BasicResponse<Any>>
}