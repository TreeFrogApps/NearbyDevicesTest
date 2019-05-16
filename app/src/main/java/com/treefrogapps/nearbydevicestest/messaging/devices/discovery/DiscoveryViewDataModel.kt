package com.treefrogapps.nearbydevicestest.messaging.devices.discovery

import com.treefrogapps.nearbydevicestest.R
import com.treefrogapps.nearbydevicestest.app.NearbyDevicesResources
import com.treefrogapps.nearbydevicestest.messaging.devices.discovery.DiscoveryEvent.*
import com.treefrogapps.nearbydevicestest.nearby.DiscoverConnection.DiscoveredDevice

data class DiscoveryViewDataModel(val statusText: String = "",
                                  val remoteUsername: String = "",
                                  val connectedToRemoteUser: Boolean = false,
                                  val startAnimations : Boolean = false,
                                  val foundDevices: List<DiscoveredDevice> = listOf()) {

    fun fromDiscoveringEvent(event: DiscoveringEvent, resources: NearbyDevicesResources): DiscoveryViewDataModel =
            copy(statusText = if (event.isDiscovering) resources.getString(R.string.looking_for_devices) else "",
                 startAnimations = event.isDiscovering)

    fun fromConnectionEvent(event: ConnectionEvent, resources: NearbyDevicesResources): DiscoveryViewDataModel =
            if (event.connectionRequested) {
                this.copy(remoteUsername = String.format(resources.getString(R.string.connecting_to_remote_user), event.remoteUsername),
                          connectedToRemoteUser = event.connectionSuccess)
            } else {
                this.copy(remoteUsername = "",
                          connectedToRemoteUser = event.connectionSuccess)
            }

    fun fromDevicesEvent(event: DevicesEvent): DiscoveryViewDataModel =
            this.copy(foundDevices = event.foundDevices)
}