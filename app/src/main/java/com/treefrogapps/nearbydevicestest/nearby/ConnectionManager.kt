package com.treefrogapps.nearbydevicestest.nearby

import android.support.annotation.WorkerThread
import com.google.android.gms.nearby.connection.*
import com.google.android.gms.tasks.Tasks
import com.treefrogapps.nearbydevicestest.di.ApplicationScope
import com.treefrogapps.nearbydevicestest.nearby.ConnectionType.*
import io.reactivex.Flowable
import io.reactivex.Observable
import javax.inject.Inject


@ApplicationScope class ConnectionManager
@Inject constructor(private val connectionsClient: ConnectionsClient,
                    @Connection(ADVERTISING) private val advertisingConnection:
                    ObservableConnection<ConnectionLifecycleCallback, AdvertisingOptions, Observable<MutableMap<String, ConnectionState>>>,
                    @Connection(DISCOVER) private val discoveryConnection:
                    ObservableConnection<EndpointDiscoveryCallback, DiscoveryOptions, Observable<MutableSet<String>>>,
                    @Connection(PAYLOAD) private val payloadConnection:
                    ObservableConnection<PayloadCallback, Unit, Flowable<Pair<String, Payload>>>,
                    @Connection private val serviceName: String) {

    companion object {
        private val lock = Any()
    }

    @Volatile private var isDiscovering: Boolean = false
    @Volatile private var isAdvertising: Boolean = false

    @WorkerThread fun startDiscovery(): Boolean =
            synchronized(lock) {
                if (!isDiscovering) {
                    connectionsClient.startDiscovery(
                            serviceName,
                            discoveryConnection.callback(),
                            discoveryConnection.options())
                            .let {
                                Tasks.await(it)
                                isDiscovering = it.isSuccessful
                                return isDiscovering
                            }
                } else false
            }

    @WorkerThread @Synchronized fun startAdvertising(name: String): Boolean =
            synchronized(lock) {
                if (!isAdvertising) {
                    connectionsClient.startAdvertising(
                            name,
                            serviceName,
                            advertisingConnection.callback(),
                            advertisingConnection.options())
                            .let {
                                Tasks.await(it)
                                isAdvertising = it.isSuccessful
                                return isAdvertising
                            }
                } else false
            }
}