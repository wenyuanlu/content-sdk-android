package com.maishuo.haohai.person.viewmodel

import androidx.lifecycle.MutableLiveData
import com.maishuo.haohai.api.bean.KeepResponseEvent
import com.maishuo.haohai.api.bean.MyFavoriteCountEvent
import com.maishuo.haohai.api.bean.MyFavoriteResult
import com.maishuo.haohai.api.response.KeepResponse
import com.maishuo.haohai.api.response.MyFavoriteListResponse
import com.maishuo.haohai.api.retrofit.ApiService
import com.qichuang.commonlibs.basic.BaseViewModel
import com.qichuang.commonlibs.basic.BasicCommonRefreshListener.Companion.DEFAULT_DATA_LIST_PAGE
import com.qichuang.commonlibs.utils.ToastUtil
import com.qichuang.retrofitlibs.retrofit.CommonObserver
import org.greenrobot.eventbus.EventBus


/**
 * author : luo
 * date : 11/15/21
 * description :
 */
class MyFavoriteViewModel : BaseViewModel() {

    var myFavoriteLiveData = MutableLiveData<MyFavoriteResult>()


    fun getMyFavorite(page: Int, albumId: Int) {
        ApiService.instance.myFavorite(page, albumId)
            .subscribe(object : CommonObserver<MyFavoriteListResponse>(true) {
                override fun onResponseSuccess(response: MyFavoriteListResponse?) {
                    if (response?.data?.isEmpty() == true && page == DEFAULT_DATA_LIST_PAGE) {
                        myFavoriteLiveData.postValue(MyFavoriteResult(false, "暂无数据", null))
                    } else {
                        myFavoriteLiveData.postValue(
                            MyFavoriteResult(
                                true,
                                response?.total?.toString(),
                                response?.data
                            )
                        )
                    }
                }

                override fun onResponseError(message: String?, e: Throwable?, code: Int?) {
                    super.onResponseError(message, e, code)
                    myFavoriteLiveData.postValue(MyFavoriteResult(false, message, null))
                }
            })
    }

    fun keep(albumType: Int?, albumId: Int?, programId: Int?) {
        ApiService.instance.keep(albumId, programId)
            .subscribe(object : CommonObserver<KeepResponse>() {
                override fun onResponseSuccess(response: KeepResponse?) {
                    if (programId ?: 0 == 0) {
                        val event = KeepResponseEvent()
                        event.channelType = albumType
                        event.album_id = albumId
                        event.status = response?.status
                        EventBus.getDefault().post(event)
                        EventBus.getDefault().post(MyFavoriteCountEvent(0))
                    }
                }

                override fun onResponseError(message: String?, e: Throwable?, code: Int?) {
                    super.onResponseError(message, e, code)
                    ToastUtil.showToast(message)
                }
            })
    }

}

