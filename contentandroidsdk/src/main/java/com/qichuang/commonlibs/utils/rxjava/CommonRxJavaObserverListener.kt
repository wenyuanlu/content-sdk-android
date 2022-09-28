package com.qichuang.commonlibs.utils.rxjava

import io.reactivex.disposables.Disposable

interface CommonRxJavaObserverListener<T>  {

    fun onComplete() {

    }

    fun onSubscribe(d: Disposable?) {

    }

    fun onNext(t: T)

    fun onError(e: Throwable?) {

    }
}