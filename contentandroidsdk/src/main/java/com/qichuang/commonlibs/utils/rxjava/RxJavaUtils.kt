package com.qichuang.commonlibs.utils.rxjava

import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

object RxJavaUtils {

    fun <T> commonObservable(observableEmitterListener: (observableEmitter: ObservableEmitter<T>) -> Unit): Observable<T> {
        return Observable.create {
            observableEmitterListener.invoke(it)
        }
    }

    fun <T> commonObserver(listener: CommonRxJavaObserverListener<T>?): Observer<T> {
        return object : Observer<T> {
            override fun onComplete() {
                listener?.onComplete()
            }

            override fun onSubscribe(d: Disposable) {
                listener?.onSubscribe(d)
            }

            override fun onNext(t: T) {
                listener?.onNext(t)
            }

            override fun onError(e: Throwable) {
                listener?.onError(e)
            }
        }
    }

    fun <T> commonObservable(observable: Observable<T>): Observable<T> {
        return observable.subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
    }

    fun <T> commonSubscribe(observable: Observable<T>, observer: Observer<T>) {
        observable.subscribe(observer)
    }

    fun <T> rxJavaToMain(observableEmitterListener: (observableEmitter: ObservableEmitter<T>) -> Unit,
                         listener: CommonRxJavaObserverListener<T>?) {
        val observable: Observable<T> = commonObservable(observableEmitterListener)
        val observer: Observer<T> = commonObserver(listener)

        commonSubscribe(commonObservable(observable), observer)
    }
}