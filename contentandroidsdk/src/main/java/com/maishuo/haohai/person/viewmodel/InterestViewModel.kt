package com.maishuo.haohai.person.viewmodel

import androidx.lifecycle.MutableLiveData
import com.maishuo.haohai.api.bean.InterestResult
import com.maishuo.haohai.api.response.InterestResponse
import com.maishuo.haohai.api.retrofit.ApiService
import com.qichuang.commonlibs.basic.BaseViewModel
import com.qichuang.retrofitlibs.retrofit.CommonObserver


/**
 * author : luo
 * date : 3/24/22
 * description :
 */
class InterestViewModel : BaseViewModel() {

    var getInterestLiveData = MutableLiveData<MutableList<InterestResponse>?>()
    var putInterestLiveData = MutableLiveData<InterestResult>()

    fun getInterestTag() {
        ApiService.instance.getInterestTag()
            .subscribe(object : CommonObserver<MutableList<InterestResponse>>(true) {
                override fun onResponseSuccess(response: MutableList<InterestResponse>?) {
                    if (response?.isNullOrEmpty() == false) {
                        getInterestLiveData.postValue(response)
                    }
                }
            })
    }

    fun putInterestTag(tags: String?) {
        ApiService.instance.puInterestTag(tags)
            .subscribe(object : CommonObserver<Any>() {
                override fun onResponseSuccess(response: Any?) {
                    putInterestLiveData.postValue(InterestResult(true))
                }

                override fun onResponseError(message: String?, e: Throwable?, code: Int?) {
                    super.onResponseError(message, e, code)
                    putInterestLiveData.postValue(InterestResult(false, message))
                }
            })
    }

}