package com.treefrogapps.nearbydevicestest.messaging.devices.advertising


sealed class AdvertisingEvent {

    class Advertising(val isAdvertising : Boolean) : AdvertisingEvent()

    class Connection(val endpointId : String, val username : String, val isConnected : Boolean) : AdvertisingEvent()

    class Error(val t : Throwable) : AdvertisingEvent()
}