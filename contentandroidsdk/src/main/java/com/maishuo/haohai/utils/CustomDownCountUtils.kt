package com.maishuo.haohai.utils

import android.os.CountDownTimer

/**
 * author : xpSun
 * date : 1/11/22
 * description :
 */
class CustomDownCountUtils {

    private var downCount: CountDownTimer? = null
    var millisInFuture: Long? = null
    var currentProgress: Long? = null
    var isRunning: Boolean = false

    companion object {
        val instance: CustomDownCountUtils by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            CustomDownCountUtils()
        }

        const val DOWN_COUNT_FINISH: Long = -9999L

        var duration: Int? = null
        var interaction: Int? = null
        var adJson: String? = null
    }

    private var listener: ((millisUntilFinished: Long) -> Unit)? = null

    fun setDownCountListener(mListener: ((millisUntilFinished: Long) -> Unit)?) {
        listener = mListener
    }

    fun initDownCount(
        millisInFuture: Long
    ) {
        this.millisInFuture = millisInFuture
        downCount?.cancel()
        downCount = object : CountDownTimer(millisInFuture, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                currentProgress = millisInFuture - millisUntilFinished
                listener?.invoke(millisUntilFinished)
            }

            override fun onFinish() {
                currentProgress = 0
                listener?.invoke(DOWN_COUNT_FINISH)
            }
        }
        downCount?.start()
        isRunning = true
    }

    fun cancel() {
        downCount?.cancel()
        currentProgress = 0
        isRunning = false
        duration = null
        interaction = null
        adJson = null
    }

    fun stop(){
        downCount?.cancel()
    }
}