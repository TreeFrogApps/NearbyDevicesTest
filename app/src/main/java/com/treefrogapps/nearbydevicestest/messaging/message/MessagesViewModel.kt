package com.treefrogapps.nearbydevicestest.messaging.message

import android.arch.lifecycle.ViewModel
import io.reactivex.Observable
import timber.log.Timber

class MessagesViewModel constructor(private val messagesModel: MessagesModel) : ViewModel() {

    init {
        Timber.e("Mark : constructor : hashcode : %d", hashCode())
    }

    // TODO observe all parts of the model that make up the data model - pulbic function oberve and filter data model .. scan, start with empty data model
    data class DataModel(val endpointUser: String = "",
                         val messages: List<Message> = listOf(),
                         val currentMessage: String = "")


    override fun onCleared() {
        Timber.e("Mark : onCleared : hashcode : %d", hashCode())
    }

    fun endpointUser(): Observable<String> {
        return Observable.never()
    }

    fun messages(): Observable<List<Message>> {
        return Observable.never()

    }

    fun currentMessage(): Observable<String> {
        return Observable.never()
    }


}
