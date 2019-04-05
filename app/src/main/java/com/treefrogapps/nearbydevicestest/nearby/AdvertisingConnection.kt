package com.treefrogapps.nearbydevicestest.nearby

import com.google.android.gms.nearby.connection.*
import com.google.android.gms.nearby.connection.ConnectionsStatusCodes.*
import com.treefrogapps.nearbydevicestest.Package
import com.treefrogapps.nearbydevicestest.di.ApplicationScope
import com.treefrogapps.nearbydevicestest.nearby.AdvertisingConnection.InboundDevice
import com.treefrogapps.nearbydevicestest.nearby.ConnectionState.*
import com.treefrogapps.nearbydevicestest.nearby.ConnectionType.ADVERTISING
import io.reactivex.Flowable
import io.reactivex.processors.PublishProcessor
import javax.inject.Inject

/**
 * [Connection] implementation wrapper class for [ConnectionLifecycleCallback].
 * Incoming activeConnections that have seen this device whilst Discovering and called [ConnectionsClient.requestConnection].
 *
 * Other devices will only see the advertising connection if they have called [ConnectionsClient.startDiscovery].
 * Also activeConnections will only be advertised if [ConnectionsClient.startAdvertising] has been called.
 *
 * If a current connection already exists then new incoming connection for same endpointId is ignored
 */
@ApplicationScope class AdvertisingConnection
@Inject constructor(@NearbyConnection(ADVERTISING) private val incomingConnectionsProcessor: PublishProcessor<InboundDevice>,
                    @NearbyConnection(ADVERTISING) private val advertisingOptions: AdvertisingOptions,
                    @NearbyConnection private val errorProcessor: PublishProcessor<ConnectionError>,
                    @Package private val packageName: String) : Connection<ConnectionLifecycleCallback, AdvertisingOptions, InboundDevice> {

    data class InboundDevice(val endpointId : String, val state: ConnectionState, val userName : String?)

    /**
     * Callback for [ConnectionsClient.requestConnection] from devices that have discovered this advertising device
     */
    private val callback = object : ConnectionLifecycleCallback() {

        override fun onConnectionInitiated(endpointId: String, info: ConnectionInfo) {
            if (validIncomingConnection(endpointId)) {
                nextEvent(endpointId, INITIATED, info.endpointName)
            }
        }

        override fun onConnectionResult(endpointId: String, resolution: ConnectionResolution) {
            if (validIncomingConnection(endpointId)) {
                when (resolution.status.statusCode) {
                    STATUS_OK                                -> CONNECTED
                    STATUS_CONNECTION_REJECTED, STATUS_ERROR -> DISCONNECTED
                    else                                     -> null
                }?.let { nextEvent(endpointId, it, null) }
            }
        }

        override fun onDisconnected(endpointId: String) {
            nextEvent(endpointId, DISCONNECTED, null)
        }
    }

    override fun callback(): ConnectionLifecycleCallback = callback

    override fun options(): AdvertisingOptions = advertisingOptions

    override fun observe(): Flowable<InboundDevice> = incomingConnectionsProcessor

    override fun observeErrors(): Flowable<ConnectionError> = errorProcessor

    private fun validIncomingConnection(endpointId: String): Boolean = endpointId.startsWith(packageName)

    private fun nextEvent(endpointId: String, state: ConnectionState, userName: String?) {
        incomingConnectionsProcessor.onNext(InboundDevice(endpointId, state, userName))
    }
}