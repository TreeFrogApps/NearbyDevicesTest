package com.treefrogapps.nearbydevicestest.rx

import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.TextView
import io.reactivex.*
import io.reactivex.BackpressureStrategy.BUFFER
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import timber.log.Timber

fun dispose(vararg disposables: Disposable?) {
    disposables.forEach {
        it?.let { d ->
            if (!d.isDisposed) d.dispose()
            if (d is CompositeDisposable) d.clear()
        }
    }
}

fun <T> Observable<T>.withSchedulers(subscribe: Scheduler, observe: Scheduler): Observable<T> =
        compose { it.subscribeOn(subscribe).observeOn(observe) }

fun <T> Flowable<T>.withSchedulers(subscribe: Scheduler, observe: Scheduler): Flowable<T> =
        compose { it.subscribeOn(subscribe).observeOn(observe) }

fun <T> Single<T>.withSchedulers(subscribe: Scheduler, observe: Scheduler): Single<T> =
        compose { it.subscribeOn(subscribe).observeOn(observe) }

fun <T> Maybe<T>.withSchedulers(subscribe: Scheduler, observe: Scheduler): Maybe<T> =
        compose { it.subscribeOn(subscribe).observeOn(observe) }

fun Flowable<String>.rxTextViewSubscriber(textView: TextView): Disposable {
    return subscribe({ textView.text = it }, { Timber.e(it, "Error setting text on $textView") })
}

fun Observable<String>.rxTextViewSubscriber(textView: TextView): Disposable {
    return subscribe({ textView.text = it }, { Timber.e(it, "Error setting text on $textView") })
}

fun rxFlowableClickListener(view: View): Flowable<View> {
    return Flowable.create({ e ->
                               val listener = View.OnClickListener { v -> v?.let { e.onNext(it) } }
                               view.setOnClickListener(listener)
                               e.setCancellable { view.setOnClickListener(null); }
                           }, BUFFER)
}

fun rxObservableClickListener(view: View): Observable<View> {
    return Observable.create { e ->
        val listener = View.OnClickListener { v -> v?.let { e.onNext(it) } }
        view.setOnClickListener(listener)
        e.setCancellable { view.setOnClickListener(null); }
    }
}

fun rxFlowableAfterTextChangedWatcher(editText: EditText): Flowable<String> {
    return Flowable.create(
            { e ->
                val watcher = object : TextWatcher {
                    override fun afterTextChanged(s: Editable?) {
                        s?.let { e.onNext(it.toString()) }
                    }

                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                }
                editText.addTextChangedListener(watcher)
                e.setCancellable { editText.removeTextChangedListener(watcher) }
            }, BUFFER)
}

