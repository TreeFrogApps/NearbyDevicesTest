package com.treefrogapps.nearbydevicestest.messaging.devices.discovery

import android.arch.lifecycle.ViewModel
import com.treefrogapps.nearbydevicestest.app.NearbyDevicesResources
import com.treefrogapps.nearbydevicestest.nearby.AdvertisingConnection.InboundDevice
import com.treefrogapps.nearbydevicestest.rx.SchedulerSupplier

class AdvertisingViewModel(private val advertisingModel: AdvertisingModel,
                           private val resources: NearbyDevicesResources,
                           scheduler: SchedulerSupplier) : ViewModel() {

    data class DataModel(val connectedDevice: InboundDevice,
                         val isConnected: Boolean,
                         val foundDevices: List<InboundDevice>)


    override fun onCleared() {
        super.onCleared()
    }

}
