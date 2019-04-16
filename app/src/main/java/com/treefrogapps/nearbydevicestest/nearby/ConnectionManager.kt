package com.treefrogapps.nearbydevicestest.nearby

import android.support.annotation.WorkerThread
import com.google.android.gms.nearby.connection.*
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.treefrogapps.nearbydevicestest.Package
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
import javax.inject.Inject


@ApplicationScope class ConnectionManager
@Inject constructor(private val connectionsClient: ConnectionsClient,
                    @NearbyConnection(ADVERTISING) private val advertisingConnection: Connection<ConnectionLifecycleCallback, AdvertisingOptions, InboundDevice>,
                    @NearbyConnection(DISCOVER) private val discoveryConnection: Connection<EndpointDiscoveryCallback, DiscoveryOptions, DiscoveredDevice>,
                    @NearbyConnection(PAYLOAD) private val payloadConnection: Connection<PayloadCallback, Unit, Pair<String, Payload>>,
                    @NearbyConnection private val endpointId: String,
                    @NearbyConnection private val scheduler: Scheduler,
                    @Package private val packageName: String) {

    companion object {

        private fun reduceInboundConnections(connections: Map<String, InboundDevice>, device: InboundDevice): Map<String, InboundDevice> {
            val updatedConnections = HashMap(connections)
            when (device.state) {
                CONNECTED,
                INITIATED    -> updatedConnections[device.endpointId] = device
                DISCONNECTED -> updatedConnections.remove(device.endpointId)
            }
            return updatedConnections
        }

        private fun reduceDiscoveredDevices(devices: Map<String, DiscoveredDevice>, device: DiscoveredDevice): Map<String, DiscoveredDevice> {
            val updatedDevices = HashMap(devices)
            when (device.state) {
                FOUND -> updatedDevices[device.endpointId] = device
                LOST  -> updatedDevices.remove(device.endpointId)
            }
            return updatedDevices
        }

        private fun <T> Task<T>.toBlockingResult(): Boolean {
            Tasks.await(this)
            return this.isSuccessful
        }

        private fun <T> Task<T>.toBlocking(): Task<T> {
            Tasks.await(this)
            return this
        }
    }

    private val connectedDevices: ConnectableFlowable<Map<String, InboundDevice>> =
            advertisingConnection.observe()
                    .observeOn(scheduler)
                    .scan(mutableMapOf(), ::reduceInboundConnections)
                    .distinctUntilChanged()
                    .replay(1)

    private val discoveredDevices: ConnectableFlowable<Map<String, DiscoveredDevice>> =
            discoveryConnection.observe()
                    .observeOn(scheduler)
                    .scan(mutableMapOf(), ::reduceDiscoveredDevices)
                    .distinctUntilChanged()
                    .replay(1)

    private val payloadData: ConnectableFlowable<Pair<String, Payload>> =
            payloadConnection.observe()
                    .observeOn(scheduler)
                    .publish()

    private val callbackErrors: ConnectableFlowable<ConnectionError> =
            Flowable.merge(
                    advertisingConnection.observeErrors(),
                    discoveryConnection.observeErrors(),
                    payloadConnection.observeErrors())
                    .publish()

    @WorkerThread
    fun startDiscovery(): Boolean =
            connectionsClient.startDiscovery(
                    endpointId,
                    discoveryConnection.callback(),
                    discoveryConnection.options())
                    .toBlockingResult()

    fun stopDiscovery() {
        connectionsClient.stopDiscovery()
    }

    @WorkerThread
    fun startAdvertising(name: String): Boolean =
            connectionsClient.startAdvertising(
                    name,
                    endpointId,
                    advertisingConnection.callback(),
                    advertisingConnection.options())
                    .toBlockingResult()

    fun stopAdvertising() {
        connectionsClient.stopAdvertising()
    }

    fun reset() {
        connectionsClient.stopAllEndpoints()
    }

    @WorkerThread
    fun acceptConnection(endpointId: String): Single<Boolean> =
            withConnectedDevices(endpointId, { id, devices ->
                devices[id]
                        ?.takeIf { it.state == INITIATED }
                        ?.let { it.endpointId }
                        ?.let { connectionsClient.acceptConnection(it, payloadConnection.callback()).toBlockingResult() }
                        ?.let { Single.just(it) }
                        ?: Single.error(ConnectionException())
            }, false)

    @WorkerThread
    fun rejectConnection(endpointId: String): Single<Boolean> =
            withConnectedDevices(endpointId, { id, devices ->
                devices[id]
                        ?.takeIf { it.state == INITIATED }
                        ?.let { it.endpointId }
                        ?.let { connectionsClient.rejectConnection(it).toBlockingResult() }
                        ?.let { Single.just(it) }
                        ?: Single.error(ConnectionException())
            }, false)

    @WorkerThread
    fun requestConnection(username: String): Single<Boolean> =
            withDiscoveredDevices(endpointId, { id, devices ->
                devices[id]
                        ?.takeIf { it.state == FOUND }
                        ?.let { it.endpointId }
                        ?.let { connectionsClient.requestConnection(username, endpointId, advertisingConnection.callback()).toBlockingResult() }
                        ?.let { Single.just(it) }
                        ?: Single.error(ConnectionException())
            }, false)

    @WorkerThread
    fun sendMessage(endpointId: String, bytes: ByteArray): Single<Boolean> =
            withConnectedDevices(endpointId, { id, devices ->
                devices[id]
                        ?.takeIf { it.state == CONNECTED }
                        ?.let { it.endpointId }
                        ?.let { connectionsClient.sendPayload(it, Payload.fromBytes(bytes)).toBlockingResult() }
                        ?.let { Single.just(it) }
                        ?: Single.error(ConnectionException())
            }, false)

    fun activeConnections(): Flowable<List<InboundDevice>> =
            connectedDevices
                    .autoConnect()
                    .map { it.values.toList().filter { d -> d.state == CONNECTED } }

    fun inboundConnections(): Flowable<List<InboundDevice>> =
            connectedDevices
                    .autoConnect()
                    .map { it.values.toList().filter { d -> d.state == INITIATED } }

    fun discoveredDevices(): Flowable<List<DiscoveredDevice>> =
            discoveredDevices.autoConnect().map { it.values.toList() }

    fun payloadData(): Flowable<Pair<String, Payload>> = payloadData

    private fun <T> withConnectedDevices(endpointId: String, fn: (id: String, devices: Map<String, InboundDevice>) -> Single<T>, default: T): Single<T> =
            connectedDevices.let { devices ->
                Flowable.just(endpointId)
                        .observeOn(scheduler)
                        .withLatestFrom(devices)
                        .flatMapSingle { devicesMap -> fn.invoke(devicesMap.first, devicesMap.second) }
                        .first(default)
            } ?: Single.error(IllegalStateException("No available connections"))

    private fun <T> withDiscoveredDevices(username: String, fn: (s: String, devices: Map<String, DiscoveredDevice>) -> Single<T>, default: T): Single<T> =
            discoveredDevices.let { devices ->
                Flowable.just(username)
                        .observeOn(scheduler)
                        .withLatestFrom(devices)
                        .flatMapSingle { devicesMap -> fn.invoke(devicesMap.first, devicesMap.second) }
                        .first(default)
            } ?: Single.error(IllegalStateException("No available connections"))

}