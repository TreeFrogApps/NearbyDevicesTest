package com.treefrogapps.nearbydevicestest.messaging.devices.advertising

import android.graphics.drawable.Drawable
import com.treefrogapps.nearbydevicestest.app.BaseViewModel
import com.treefrogapps.nearbydevicestest.app.NearbyDevicesResources
import io.reactivex.Flowable
import io.reactivex.Maybe
import java.util.*

class AdvertisingViewModel(model: AdvertisingModel,
                           private val resources: NearbyDevicesResources)
    : BaseViewModel<AdvertisingViewDataModel, AdvertisingModel, AdvertisingEvent>(model, AdvertisingViewDataModel()) {

    override fun onFirstLaunch() {
        super.onFirstLaunch()
        model.start()
    }

    override fun reduce(previousVDM: AdvertisingViewDataModel, event: AdvertisingEvent): AdvertisingViewDataModel =
            when (event) {
                is AdvertisingEvent.Advertising -> previousVDM.fromAdvertisingEvent(event, resources)
                is AdvertisingEvent.Connection  -> previousVDM.fromConnectionEvent(event)
                is AdvertisingEvent.Error       -> previousVDM.fromError(resources)
            }

    fun observeDrawable(): Maybe<Drawable> =
            dataModelObservable.filter { Objects.nonNull(it.statusDrawable) }
                    .map { it.statusDrawable!! }
                    .firstElement()

    fun observeStatusText(): Flowable<String> =
            dataModelObservable
                    .map { it.statusText }

    fun observeDeviceText(): Flowable<String> =
            dataModelObservable
                    .map { it.deviceText }
                    .distinctUntilChanged()

    fun observeDeviceId(): Flowable<String> =
            dataModelObservable
                    .map { it.requestedConnectionDeviceId }
                    .distinctUntilChanged()

    fun observeButtonVisibility(): Flowable<Int> =
            dataModelObservable.map { it.connectVisibility }
                    .distinctUntilChanged()

    fun observeConnection(): Maybe<Boolean> =
            dataModelObservable.filter { it.isConnected != null }
                    .map { it.isConnected!! }
                    .filter { it }
                    .firstElement()

    fun onConnectDevice(endpointId : String, username : String) {
        if(endpointId.isNotEmpty() && username.isNotEmpty()) {
            model.acceptConnection(endpointId, username)
        }
    }
}
