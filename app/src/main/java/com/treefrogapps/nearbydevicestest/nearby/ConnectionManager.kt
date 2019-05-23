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
import com.treefrogapps.nearbydevicestest.rx.SchedulerSupplier
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.flowables.ConnectableFlowable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.withLatestFrom
import timber.log.Timber
import java.util.function.Supplier
import javax.inject.Inject


@ApplicationScope
class ConnectionManager
@Inject constructor(
        @NearbyConnection private val connectionsClient: ConnectionsClient,
        @NearbyConnection(DISCOVER) private val discoveryConnection: Connection<EndpointDiscoveryCallback, DiscoveryOptions, DiscoveredDevice>,
        @NearbyConnection(ADVERTISING) private val advertisingConnection: Connection<ConnectionLifecycleCallback, AdvertisingOptions, InboundDevice>,
        @NearbyConnection(PAYLOAD) private val payloadConnection: Connection<PayloadCallback, Unit, Pair<String, Payload>>,
        @NearbyConnection private val endpointId: String,
        private val schedulers: SchedulerSupplier,
        @User private val username: Supplier<String>,
        private val taskWrapper: TaskDelegate,
        private val payloadDelegate: PayloadDelegate) {

    private val disposables: CompositeDisposable = CompositeDisposable()

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
                    .subscribeOn(schedulers.io())
                    .scan(mutableMapOf(), ::reduceDiscoveredDevices)
                    .distinctUntilChanged()
                    .replay(1)

    private val connectedDevices: ConnectableFlowable<Map<String, InboundDevice>> =
            advertisingConnection.observe()
                    .subscribeOn(schedulers.io())
                    .scan(mutableMapOf(), ::reduceInboundConnections)
                    .distinctUntilChanged()
                    .replay(1)

    private val payloadData: ConnectableFlowable<Pair<String, Payload>> =
            payloadConnection.observe()
                    .subscribeOn(schedulers.io())
                    .publish()

    private val callbackErrors: ConnectableFlowable<ConnectionError> =
            Flowable.merge(
                    advertisingConnection.observeErrors(),
                    discoveryConnection.observeErrors(),
                    payloadConnection.observeErrors()
            ).publish()

    fun start(): Single<Boolean> {
        disposables.addAll(discoveredDevices.connect(),
                           connectedDevices.connect(),
                           payloadData.connect(),
                           callbackErrors.connect())

        return Single.defer {
            taskWrapper.toSingle(
                    connectionsClient.startAdvertising(
                            username.get(),
                            endpointId,
                            advertisingConnection.callback(),
                            advertisingConnection.options()))
        }.flatMap {
            taskWrapper.toSingle(
                    connectionsClient.startDiscovery(
                            endpointId,
                            discoveryConnection.callback(),
                            discoveryConnection.options()))
        }.subscribeOn(schedulers.io())
    }

    fun stop() {
        disposables.clear()
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
            ).subscribeOn(schedulers.io())

    fun acceptConnection(endpointId: String): Single<Boolean> =
            withConnectedDevices({ devices ->
                                     devices[endpointId]
                                             ?.takeIf { it.state == INITIATED }
                                             ?.let { it.endpointId }
                                             ?.let { taskWrapper.toSingle(connectionsClient.acceptConnection(it, payloadConnection.callback())) }
                                     ?: Single.error(ConnectionException())
                                 }, false).subscribeOn(schedulers.io())

    fun rejectConnection(endpointId: String): Single<Boolean> =
            withConnectedDevices({ devices ->
                                     devices[endpointId]
                                             ?.takeIf { it.state == INITIATED }
                                             ?.let { it.endpointId }
                                             ?.let { taskWrapper.toSingle(connectionsClient.rejectConnection(it)) }
                                     ?: Single.error(ConnectionException())
                                 }, false).subscribeOn(schedulers.io())

    fun disconnectFromEndpoint(endpointId: String) {
        connectionsClient.disconnectFromEndpoint(endpointId)
    }

    fun disconnectAllEndpoints() {
        disposables += withConnectedDevices({ Single.just(it) }, mapOf())
                .flatMapCompletable { devices ->
                    devices.filter { device -> device.value.state == CONNECTED }
                            .forEach { connectionsClient.disconnectFromEndpoint(it.key) }
                    Completable.complete()
                }.subscribeOn(schedulers.io())
                .subscribe { Timber.i("Disconnected All Endpoints") }
    }

    fun sendMessage(endpointId: String, bytes: ByteArray): Single<Boolean> =
            withConnectedDevices({ devices ->
                                     devices[endpointId]
                                             ?.takeIf { it.state == CONNECTED }
                                             ?.let { it.endpointId }
                                             ?.let { taskWrapper.toSingle(connectionsClient.sendPayload(it, payloadDelegate.fromBytes(bytes))) }
                                     ?: Single.error(ConnectionException())
                                 }, false).subscribeOn(schedulers.io())

    fun activeConnections(): Flowable<List<InboundDevice>> =
            connectedDevices.map { it.values.toList().filter { d -> d.state == CONNECTED } }
                    .subscribeOn(schedulers.io())


    fun initiatedConnections(): Flowable<List<InboundDevice>> =
            connectedDevices.map { it.values.toList().filter { d -> d.state == INITIATED } }
                    .subscribeOn(schedulers.io())

    fun discoveredDevices(): Flowable<List<DiscoveredDevice>> =
            discoveredDevices.map { it.values.toList() }
                    .subscribeOn(schedulers.io())

    fun payloadData(): Flowable<Pair<String, Payload>> = payloadData.subscribeOn(schedulers.io())

    private fun <T> withConnectedDevices(fn: (devices: Map<String, InboundDevice>) -> Single<T>, default: T): Single<T> =
            Flowable.just(Unit)
                    .withLatestFrom(connectedDevices)
                    .flatMapSingle { devicesMap -> fn.invoke(devicesMap.second) }
                    .first(default)

    private fun <T> withDiscoveredDevices(fn: (devices: Map<String, DiscoveredDevice>) -> Single<T>, default: T): Single<T> =
            Flowable.just(Unit)
                    .withLatestFrom(discoveredDevices)
                    .flatMapSingle { devicesMap -> fn.invoke(devicesMap.second) }
                    .first(default)
}