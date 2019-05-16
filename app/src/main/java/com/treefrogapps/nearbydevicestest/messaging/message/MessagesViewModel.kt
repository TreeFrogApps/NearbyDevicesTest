package com.treefrogapps.nearbydevicestest.messaging.message

import android.arch.lifecycle.ViewModel
import com.treefrogapps.nearbydevicestest.app.NearbyDevicesResources
import com.treefrogapps.nearbydevicestest.rx.SchedulerSupplier
import io.reactivex.Observable
import timber.log.Timber

class MessagesViewModel constructor(private val messagesModel: MessagesModel,
                                    private val resources: NearbyDevicesResources,
                                    schedulerSupplier: SchedulerSupplier) : ViewModel() {

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
