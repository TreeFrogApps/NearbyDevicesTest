package com.treefrogapps.nearbydevicestest.messaging.devices.discovery

import android.arch.lifecycle.ViewModel
import com.treefrogapps.nearbydevicestest.nearby.AdvertisingConnection.InboundDevice

class AdvertisingViewModel(private val advertisingModel: AdvertisingModel) : ViewModel() {

    data class AdvertisingDataModel(val connectedDevice: InboundDevice,
                                val isConnected: Boolean,
                                val foundDevices: List<InboundDevice>)

    override fun onCleared() {
        super.onCleared()
    }
    // TODO: Implement the ViewModel
}
