package com.maishuo.haohai.api.retrofit

import com.maishuo.haohai.api.param.BindPhoneParam
import com.maishuo.haohai.api.param.LoginParam
import com.maishuo.haohai.api.param.UMengLoginParam
import com.maishuo.haohai.api.param.VerifyCodeParam
import com.maishuo.haohai.api.response.*
import com.qichuang.commonlibs.utils.rxjava.RxJavaUtils
import com.qichuang.retrofitlibs.bean.BasicResponse
import com.qichuang.retrofitlibs.retrofit.ApiConstants
import com.qichuang.retrofitlibs.retrofit.ApiServiceFactory
import io.reactivex.Observable
import retrofit2.Response
import java.util.concurrent.TimeUnit

class ApiService private constructor() {

    companion object {
        val instance: ApiService by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            ApiService()
        }

        private const val MAX_RETRY_WHEN_COUNT = 1
    }

    private fun getApi(): ApiServiceInterface {
        return ApiServiceFactory.instance.getRetrofit()
            .create(ApiServiceInterface::class.java)
    }

    private var currentRetryWhenCount = MAX_RETRY_WHEN_COUNT

    private fun <T> commonObservable(observable: Observable<T>): Observable<T> {
        return RxJavaUtils.commonObservable(observable).retryWhen { throwableObservable ->
            throwableObservable.flatMap { throwable ->
                return@flatMap if (currentRetryWhenCount <= MAX_RETRY_WHEN_COUNT) {
                    currentRetryWhenCount = currentRetryWhenCount.inc()
                    Observable.just(1).delay(1000, TimeUnit.MILLISECONDS)
                } else {
                    currentRetryWhenCount = MAX_RETRY_WHEN_COUNT
                    Observable.error(throwable)
                }
            }
        }
    }

    //初始化
    fun initIndex(mid: String?, content_id: String?): Observable<BasicResponse<InitIndexResponse>> {
        return commonObservable(getApi().initIndex(mid, content_id))
    }

    //首页列表
    fun fetMainList(
        albumType: Int?,
        programName: String?,
        pageSize: Int?,
        page: Int?,
    ): Observable<BasicResponse<MutableList<GetListResponse>>> {
        return commonObservable(getApi().fetMainList(albumType, programName, pageSize, page))
    }

    //收藏/取消收藏
    fun keep(albumId: Int?, programId: Int?): Observable<BasicResponse<KeepResponse>> {
        return commonObservable(getApi().keep(albumId, programId))
    }

    //获取章节详情列表
    fun getProgramList(
        albumId: Int?,
        pageSize: Int?,
        page: Int?
    ): Observable<BasicResponse<GetProgramListResponse>> {
        return commonObservable(getApi().getProgramList(albumId, pageSize, page))
    }

    //获取验证码
    fun getVerifyCode(params: VerifyCodeParam): Observable<BasicResponse<VerifyCodeResponse>> {
        return commonObservable(getApi().getVerifyCode(params))
    }

    //登录
    fun login(params: LoginParam): Observable<BasicResponse<LoginResponse>> {
        return commonObservable(getApi().login(params))
    }

    //退出登录
    fun logout(phone: String): Observable<BasicResponse<Any>> {
        return commonObservable(getApi().logout(phone))
    }

    //换绑手机号
    fun bindPhone(params: BindPhoneParam): Observable<BasicResponse<Any>> {
        return commonObservable(getApi().bindPhone(params))
    }

    //其他推荐
    fun otherRec(
        author_name: String?,
        album_type: Int?,
        album_id: Int?,
        page: Int?
    ): Observable<BasicResponse<MutableList<GetListResponse>>> {
        return commonObservable(getApi().otherRec(author_name, album_type, album_id, page))
    }

    //其他推荐
    fun newsOtherRec(
        company: String?,
        page: Int?
    ): Observable<BasicResponse<MutableList<GetListResponse>>> {
        return commonObservable(getApi().newsOtherRec(company, page))
    }

    //我的收藏列表
    fun myFavorite(page: Int, albumId: Int): Observable<BasicResponse<MyFavoriteListResponse>> {
        return commonObservable(getApi().getMyFavorite(page, albumId))
    }

    //友盟一键登录
    fun uMengLogin(params: UMengLoginParam): Observable<BasicResponse<LoginResponse>> {
        return commonObservable(getApi().uMengLogin(params))
    }

    //异常上报(不删除只上报)
    fun putAberrant(
        album_type: Int?,
        album_id: Int?,
        program_id: Int?
    ): Observable<BasicResponse<Any>> {
        return commonObservable(getApi().putAberrant(album_type, album_id, program_id))
    }

    //收听记录上报
    fun putListenHistory(
        albumId: Int?,
        programId: Int?
    ): Observable<BasicResponse<Any>> {
        return commonObservable(getApi().putListenHistory(albumId, programId))
    }

    //收听记录上报
    fun putNewsListenHistory(
        albumId: Int?
    ): Observable<BasicResponse<Any>> {
        return commonObservable(getApi().putNewsListenHistory(albumId))
    }

    //上报播放进度
    fun contentCtrace(
        mid: String?,
        provider: Int?,
        uid: String?,
        type: Int?,
        chapterId: String?,
        event: String?
    ): Observable<BasicResponse<Response<Void>>> {
        val url = ApiConstants.SEND_CONTENT_PLAYER_REQUEST
        return commonObservable(
            getApi()
                .ctracr(
                    url,
                    mid,
                    provider,
                    uid,
                    type,
                    chapterId,
                    event
                )
        )
    }

    fun recommendList(
        albumType: Int?,
        album_lid: String?
    ): Observable<BasicResponse<MutableList<RecommendListResponse>>> {
        return commonObservable(getApi().recommendList(albumType, album_lid))
    }

    //获取兴趣标签列表
    fun getInterestTag(): Observable<BasicResponse<MutableList<InterestResponse>>> {
        return commonObservable(getApi().getInterestTag())
    }

    //提交选择的兴趣标签
    fun puInterestTag(
        tags: String?
    ): Observable<BasicResponse<Any>> {
        return commonObservable(getApi().puInterestTag(tags))
    }

    //获取播放历史列表
    fun getPlayHistoryList(
        type: Int?
    ): Observable<BasicResponse<MutableList<GetListResponse>>> {
        return commonObservable(getApi().getPlayHistoryList(type))
    }

    //删除播放历史
    fun deletePlayHistory(
        type: Int?,
        albumLids: String?
    ): Observable<BasicResponse<Any>> {
        return commonObservable(getApi().deletePlayHistory(type, albumLids))
    }

    //播放上报
    fun playReport(
        albumType: Int?,
        albumLid: String?,
        programLid: String?,
        playSecond: Int?
    ): Observable<BasicResponse<Any>> {
        return commonObservable(getApi().playReport(albumType, albumLid, programLid, playSecond))
    }

}