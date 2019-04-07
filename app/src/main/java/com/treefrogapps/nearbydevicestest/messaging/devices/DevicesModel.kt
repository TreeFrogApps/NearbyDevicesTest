package com.treefrogapps.nearbydevicestest.messaging.devices

import com.treefrogapps.nearbydevicestest.di.ApplicationScope
import com.treefrogapps.nearbydevicestest.nearby.ConnectionManager
import javax.inject.Inject


@ApplicationScope class DevicesModel
@Inject constructor(private val connectionManager: ConnectionManager) {
}