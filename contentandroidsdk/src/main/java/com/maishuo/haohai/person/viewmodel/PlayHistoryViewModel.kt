package com.maishuo.haohai.person.viewmodel

import androidx.lifecycle.MutableLiveData
import com.maishuo.haohai.api.response.GetListResponse
import com.maishuo.haohai.api.retrofit.ApiService
import com.maishuo.haohai.common.Constant
import com.qichuang.commonlibs.basic.BaseViewModel
import com.qichuang.commonlibs.utils.ToastUtil
import com.qichuang.retrofitlibs.retrofit.CommonObserver


/**
 * author : luo
 * date : 3/28/22
 * description :
 */
class PlayHistoryViewModel : BaseViewModel() {

    var getPlayHistoryLiveData = MutableLiveData<MutableList<GetListResponse>?>()
    var deletePlayHistoryLiveData = MutableLiveData<Boolean?>()

    fun getPlayHistoryList(type: Int?) {
        ApiService.instance.getPlayHistoryList(type)
            .subscribe(object : CommonObserver<MutableList<GetListResponse>>(false) {
                override fun onResponseSuccess(response: MutableList<GetListResponse>?) {
                    getPlayHistoryLiveData.postValue(response)
                }

                override fun onResponseError(message: String?, e: Throwable?, code: Int?) {
                    super.onResponseError(message, e, code)
                    ToastUtil.showToast(message)
                }
            })
    }

    fun deletePlayHistory(
        type: Int?,
        albumLids: String?
    ) {
        ApiService.instance.deletePlayHistory(type, albumLids)
            .subscribe(object : CommonObserver<Any>() {
                override fun onResponseSuccess(response: Any?) {
                    deletePlayHistoryLiveData.postValue(true)
                }

                override fun onResponseError(message: String?, e: Throwable?, code: Int?) {
                    super.onResponseError(message, e, code)
                    ToastUtil.showToast(message)
                }
            })
    }

    fun playReport(playSecond: Int?) {
        val response = Constant.response
        ApiService.instance.playReport(
            response?.album_type,
            response?.album_lid,
            response?.program_lid,
            playSecond
        )
            .subscribe(object : CommonObserver<Any>(true) {
                override fun onResponseSuccess(response: Any?) {}
            })
    }

}