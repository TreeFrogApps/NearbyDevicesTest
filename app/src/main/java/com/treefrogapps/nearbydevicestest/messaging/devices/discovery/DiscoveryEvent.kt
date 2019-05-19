package com.treefrogapps.nearbydevicestest.messaging.devices.discovery

import android.support.annotation.StringRes
import com.treefrogapps.nearbydevicestest.nearby.DiscoverConnection.DiscoveredDevice


sealed class DiscoveryEvent {

    data class DiscoveringEvent(val isDiscovering: Boolean = false) : DiscoveryEvent()

    data class ConnectionEvent(val remoteUsername: String = "",
                               val connectionRequested : Boolean = false,
                               val connectionSuccess: Boolean = false) : DiscoveryEvent()

    data class DevicesEvent(val foundDevices: List<DiscoveredDevice>) : DiscoveryEvent()

    data class ErrorEvent(@StringRes val reason : Int, val error : Throwable?) : DiscoveryEvent()
}