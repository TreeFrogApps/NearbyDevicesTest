package com.treefrogapps.nearbydevicestest.app

import android.arch.lifecycle.ViewModel
import com.treefrogapps.nearbydevicestest.rx.dispose
import io.reactivex.disposables.Disposable
import io.reactivex.flowables.ConnectableFlowable


abstract class BaseViewModel<VDM, M : BaseObservableModel<E>, E>(protected val model: M, initialVDM: VDM) : ViewModel() {

    private var disposable : Disposable? = null

    protected val dataModelObservable: ConnectableFlowable<VDM> =
            model.observeEvents()
                    .scan(initialVDM, ::reduce)
                    .replay(1)

    open fun onFirstLaunch() {
        disposable = dataModelObservable.connect()
    }

    override fun onCleared() {
        dispose(disposable)
        model.onCleared()

    }

    abstract fun reduce(previousVDM: VDM, event: E): VDM
}