package com.treefrogapps.nearbydevicestest.messaging.message

import com.treefrogapps.nearbydevicestest.app.BaseViewModel
import com.treefrogapps.nearbydevicestest.app.NearbyDevicesResources
import com.treefrogapps.nearbydevicestest.rx.SchedulerSupplier
import io.reactivex.Observable

class MessagesViewModel constructor(model: MessagesModel,
                                    private val resources: NearbyDevicesResources,
                                    private val schedulerSupplier: SchedulerSupplier)

    : BaseViewModel<MessagesViewDataModel, MessagesModel, MessagesEvent>(model, MessagesViewDataModel()) {

    override fun onFirstLaunch() {
        super.onFirstLaunch()
        model.listenToConnectedDevice()
    }

    override fun reduce(previousVDM: MessagesViewDataModel, event: MessagesEvent): MessagesViewDataModel {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
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
