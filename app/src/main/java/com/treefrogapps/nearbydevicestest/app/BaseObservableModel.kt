package com.treefrogapps.nearbydevicestest.app

import io.reactivex.Flowable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.processors.PublishProcessor


abstract class BaseObservableModel<E> {

    protected val disposables = CompositeDisposable()
    private val eventProcessor = PublishProcessor.create<E>()

    protected fun onEvent(event: E) {
        eventProcessor.onNext(event)
    }

    fun observeEvents(): Flowable<E> = eventProcessor

    open fun onCleared() {
        disposables.clear()
    }
}