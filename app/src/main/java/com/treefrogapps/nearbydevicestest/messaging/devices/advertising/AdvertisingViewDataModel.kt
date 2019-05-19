package com.treefrogapps.nearbydevicestest.messaging.devices.advertising

import android.graphics.drawable.Drawable
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import com.treefrogapps.nearbydevicestest.R
import com.treefrogapps.nearbydevicestest.app.NearbyDevicesResources
import com.treefrogapps.nearbydevicestest.messaging.devices.advertising.AdvertisingEvent.Advertising
import com.treefrogapps.nearbydevicestest.messaging.devices.advertising.AdvertisingEvent.Connection

data class AdvertisingViewDataModel(
        val statusText: String = "",
        val statusDrawable: Drawable? = null,
        val isConnected: Boolean? = null,
        val deviceText: String = "",
        val connectVisibility: Int = INVISIBLE,
        val requestedConnectionDeviceId: String = "") {

    fun fromAdvertisingEvent(event: Advertising, resources: NearbyDevicesResources) =
            event.isAdvertising.let {
                copy(statusText = if (it) resources.getString(R.string.waiting_for_connections) else "",
                     statusDrawable = if (it) resources.getDrawable(R.drawable.avd_searching_for_devices) else null)
            }

    fun fromConnectionEvent(event: Connection) =
            event.isConnected.let {
                copy(requestedConnectionDeviceId = event.endpointId,
                     isConnected = it,
                     deviceText = event.username,
                     connectVisibility = if (!it) VISIBLE else INVISIBLE)
            }

    fun fromError(resources: NearbyDevicesResources) =
            copy(statusText = resources.getString(R.string.advertising_error),
                 deviceText = "",
                 requestedConnectionDeviceId = "",
                 connectVisibility = INVISIBLE)
}