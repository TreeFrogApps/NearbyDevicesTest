package com.treefrogapps.nearbydevicestest.nearby

import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo
import com.google.android.gms.nearby.connection.DiscoveryOptions
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback
import com.treefrogapps.nearbydevicestest.Package
import com.treefrogapps.nearbydevicestest.di.ApplicationScope
import com.treefrogapps.nearbydevicestest.nearby.ConnectionType.DISCOVER
import com.treefrogapps.nearbydevicestest.nearby.DiscoverConnection.DiscoveredDevice
import com.treefrogapps.nearbydevicestest.nearby.DiscoveryState.FOUND
import com.treefrogapps.nearbydevicestest.nearby.DiscoveryState.LOST
import io.reactivex.Flowable
import io.reactivex.processors.PublishProcessor
import timber.log.Timber
import javax.inject.Inject

/**
 * [Connection] implementation wrapper class for [EndpointDiscoveryCallback].
 * Discovered advertising Devices with call [EndpointDiscoveryCallback.onEndpointFound].
 *
 * To initiate connection with the Advertiser the client must call
 * [com.google.android.gms.nearby.connection.ConnectionsClient.requestConnection]
 */
@ApplicationScope class DiscoverConnection
@Inject constructor(@NearbyConnection(DISCOVER) private val connectionProcessor: PublishProcessor<DiscoveredDevice>,
                    @NearbyConnection(DISCOVER) private val connectionOptions: DiscoveryOptions,
                    @NearbyConnection private val errorProcessor: PublishProcessor<ConnectionError>,
                    @Package private val packageName: String) : Connection<EndpointDiscoveryCallback, DiscoveryOptions, DiscoveredDevice> {

    data class DiscoveredDevice(val endpointId: String, val state: DiscoveryState, val info: DiscoveredEndpointInfo?)

    private val callback = object : EndpointDiscoveryCallback() {

        override fun onEndpointFound(endpointId: String, info: DiscoveredEndpointInfo) {
            Timber.i("Mark : onEndpointFound : %s, %s", endpointId, info.endpointName)
            if (validAdvertiser(info.serviceId)) {
                connectionProcessor.onNext(DiscoveredDevice(endpointId, FOUND, info))
            }
        }

        override fun onEndpointLost(endpointId: String) {
            Timber.i("Mark : onEndpointLost : %s", endpointId)
            connectionProcessor.onNext(DiscoveredDevice(endpointId, LOST, null))
        }
    }

    override fun callback(): EndpointDiscoveryCallback = callback

    override fun options(): DiscoveryOptions = connectionOptions

    override fun observe(): Flowable<DiscoveredDevice> = connectionProcessor

    override fun observeErrors(): Flowable<ConnectionError> = errorProcessor

    private fun validAdvertiser(serviceId: String): Boolean = serviceId == packageName
}