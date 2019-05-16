package com.treefrogapps.nearbydevicestest.messaging.devices.discovery

import com.treefrogapps.nearbydevicestest.di.ApplicationScope
import com.treefrogapps.nearbydevicestest.nearby.ConnectionManager
import javax.inject.Inject


@ApplicationScope class AdvertisingModel
@Inject constructor(private val connectionManager: ConnectionManager) {

}