package com.treefrogapps.nearbydevicestest.app

import io.reactivex.Flowable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.processors.PublishProcessor
import io.reactivex.rxkotlin.plusAssign


abstract class BaseObservableModel<E> {

    private val disposables = CompositeDisposable()
    private val eventProcessor = PublishProcessor.create<E>()
    private val errorProcessor = PublishProcessor.create<ErrorEvent>()

    protected fun pushEvent(event: E) {
        eventProcessor.onNext(event)
    }

    protected fun pushErrorEvent(errorEvent: ErrorEvent) {
        errorProcessor.onNext(errorEvent)
    }

    protected fun addDisposable(d: Disposable) {
        disposables+=d
    }

    protected fun removeDisposable(d: Disposable) {
        disposables.remove(d)
    }

    fun observeEvents(): Flowable<E> = eventProcessor

    fun observeErrorEvents() : Flowable<ErrorEvent> = errorProcessor

    open fun onCleared() {
        disposables.clear()
    }
}