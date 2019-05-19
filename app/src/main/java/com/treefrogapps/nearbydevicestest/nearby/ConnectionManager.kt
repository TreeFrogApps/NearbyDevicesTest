package com.treefrogapps.nearbydevicestest.nearby

import com.google.android.gms.nearby.connection.*
import com.treefrogapps.nearbydevicestest.User
import com.treefrogapps.nearbydevicestest.di.ApplicationScope
import com.treefrogapps.nearbydevicestest.nearby.AdvertisingConnection.InboundDevice
import com.treefrogapps.nearbydevicestest.nearby.ConnectionState.*
import com.treefrogapps.nearbydevicestest.nearby.ConnectionType.*
import com.treefrogapps.nearbydevicestest.nearby.DiscoverConnection.DiscoveredDevice
import com.treefrogapps.nearbydevicestest.nearby.DiscoveryState.FOUND
import com.treefrogapps.nearbydevicestest.nearby.DiscoveryState.LOST
import io.reactivex.Flowable
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.flowables.ConnectableFlowable
import io.reactivex.rxkotlin.withLatestFrom
import java.util.function.Supplier
import javax.inject.Inject


@ApplicationScope
class ConnectionManager
@Inject constructor(
        private val connectionsClient: ConnectionsClient,
        @NearbyConnection(DISCOVER) private val discoveryConnection: Connection<EndpointDiscoveryCallback, DiscoveryOptions, DiscoveredDevice>,
        @NearbyConnection(ADVERTISING) private val advertisingConnection: Connection<ConnectionLifecycleCallback, AdvertisingOptions, InboundDevice>,
        @NearbyConnection(PAYLOAD) private val payloadConnection: Connection<PayloadCallback, Unit, Pair<String, Payload>>,
        @NearbyConnection private val endpointId: String,
        @NearbyConnection private val scheduler: Scheduler,
        @User private val username: Supplier<String>,
        private val taskWrapper: TaskDelegate,
        private val payloadDelegate: PayloadDelegate) {

    companion object {

        private fun reduceInboundConnections(
                connections: Map<String, InboundDevice>,
                device: InboundDevice
        ): Map<String, InboundDevice> {
            val updatedConnections = HashMap(connections)
            when (device.state) {
                CONNECTED,
                INITIATED    -> updatedConnections[device.endpointId] = device
                DISCONNECTED -> updatedConnections.remove(device.endpointId)
            }
            return updatedConnections
        }

        private fun reduceDiscoveredDevices(
                devices: Map<String, DiscoveredDevice>,
                device: DiscoveredDevice
        ): Map<String, DiscoveredDevice> {
            val updatedDevices = HashMap(devices)
            when (device.state) {
                FOUND -> updatedDevices[device.endpointId] = device
                LOST  -> updatedDevices.remove(device.endpointId)
            }
            return updatedDevices
        }
    }

    private val discoveredDevices: ConnectableFlowable<Map<String, DiscoveredDevice>> =
            discoveryConnection.observe()
                    .observeOn(scheduler)
                    .scan(mutableMapOf(), ::reduceDiscoveredDevices)
                    .distinctUntilChanged()
                    .publish()

    private val connectedDevices: ConnectableFlowable<Map<String, InboundDevice>> =
            advertisingConnection.observe()
                    .observeOn(scheduler)
                    .scan(mutableMapOf(), ::reduceInboundConnections)
                    .distinctUntilChanged()
                    .publish()

    private val payloadData: ConnectableFlowable<Pair<String, Payload>> =
            payloadConnection.observe()
                    .observeOn(scheduler)
                    .publish()

    private val callbackErrors: ConnectableFlowable<ConnectionError> =
            Flowable.merge(
                    advertisingConnection.observeErrors(),
                    discoveryConnection.observeErrors(),
                    payloadConnection.observeErrors()
            ).publish()

    fun startDiscovery(): Single<Boolean> =
            Single.defer {
                taskWrapper.toSingle(
                        connectionsClient.startDiscovery(
                                endpointId,
                                discoveryConnection.callback(),
                                discoveryConnection.options()))
            }.subscribeOn(scheduler)

    fun stopDiscovery() {
        connectionsClient.stopDiscovery()
    }

    fun startAdvertising(): Single<Boolean> =
            Single.defer {
                taskWrapper.toSingle(
                        connectionsClient.startAdvertising(
                                username.get(),
                                endpointId,
                                advertisingConnection.callback(),
                                advertisingConnection.options()))
            }.subscribeOn(scheduler)

    fun stopAdvertising() {
        connectionsClient.stopAdvertising()
    }

    fun reset() {
        connectionsClient.stopAllEndpoints()
    }

    fun requestConnectionInitiation(remoteEndpointId: String): Single<Boolean> =
            withDiscoveredDevices(
                    { devices ->
                        devices[remoteEndpointId]
                                ?.takeIf { it.state == FOUND }
                                ?.let { it.endpointId }
                                ?.let {
                                    taskWrapper.toSingle(connectionsClient.requestConnection(username.get(), it, advertisingConnection.callback()))
                                } ?: Single.error(ConnectionException("Device endpoint $remoteEndpointId not found"))
                    }, false
            ).subscribeOn(scheduler)

    fun acceptConnection(endpointId: String): Single<Boolean> =
            withConnectedDevices({ devices ->
                                     devices[endpointId]
                                             ?.takeIf { it.state == INITIATED }
                                             ?.let { it.endpointId }
                                             ?.let { taskWrapper.toSingle(connectionsClient.acceptConnection(it, payloadConnection.callback())) }
                                     ?: Single.error(ConnectionException())
                                 }, false).subscribeOn(scheduler)

    fun rejectConnection(endpointId: String): Single<Boolean> =
            withConnectedDevices({ devices ->
                                     devices[endpointId]
                                             ?.takeIf { it.state == INITIATED }
                                             ?.let { it.endpointId }
                                             ?.let { taskWrapper.toSingle(connectionsClient.rejectConnection(it)) }
                                     ?: Single.error(ConnectionException())
                                 }, false).subscribeOn(scheduler)

    fun sendMessage(endpointId: String, bytes: ByteArray): Single<Boolean> =
            withConnectedDevices({ devices ->
                                     devices[endpointId]
                                             ?.takeIf { it.state == CONNECTED }
                                             ?.let { it.endpointId }
                                             ?.let { taskWrapper.toSingle(connectionsClient.sendPayload(it, payloadDelegate.fromBytes(bytes))) }
                                     ?: Single.error(ConnectionException())
                                 }, false).subscribeOn(scheduler)

    fun activeConnections(): Flowable<List<InboundDevice>> =
            connectedDevices
                    .autoConnect()
                    .map { it.values.toList().filter { d -> d.state == CONNECTED } }

    fun initiatedConnections(): Flowable<List<InboundDevice>> =
            connectedDevices
                    .autoConnect()
                    .map { it.values.toList().filter { d -> d.state == INITIATED } }

    fun discoveredDevices(): Flowable<List<DiscoveredDevice>> =
            discoveredDevices.autoConnect()
                    .map { it.values.toList() }

    fun payloadData(): Flowable<Pair<String, Payload>> = payloadData

    private fun <T> withConnectedDevices(fn: (devices: Map<String, InboundDevice>) -> Single<T>, default: T): Single<T> =
            Flowable.just(Unit)
                    .observeOn(scheduler)
                    .withLatestFrom(connectedDevices.autoConnect())
                    .flatMapSingle { devicesMap -> fn.invoke(devicesMap.second) }
                    .first(default)

    private fun <T> withDiscoveredDevices(fn: (devices: Map<String, DiscoveredDevice>) -> Single<T>, default: T): Single<T> =
            Flowable.just(Unit)
                    .observeOn(scheduler)
                    .withLatestFrom(discoveredDevices.autoConnect())
                    .flatMapSingle { devicesMap -> fn.invoke(devicesMap.second) }
                    .first(default)
}