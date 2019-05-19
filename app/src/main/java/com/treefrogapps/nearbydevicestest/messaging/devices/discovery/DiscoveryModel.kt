package com.treefrogapps.nearbydevicestest.messaging.devices.discovery

import com.treefrogapps.nearbydevicestest.R
import com.treefrogapps.nearbydevicestest.app.BaseObservableModel
import com.treefrogapps.nearbydevicestest.di.ApplicationScope
import com.treefrogapps.nearbydevicestest.messaging.devices.discovery.DiscoveryEvent.ConnectionEvent
import com.treefrogapps.nearbydevicestest.messaging.devices.discovery.DiscoveryEvent.DiscoveringEvent
import com.treefrogapps.nearbydevicestest.nearby.ConnectionException
import com.treefrogapps.nearbydevicestest.nearby.ConnectionManager
import com.treefrogapps.nearbydevicestest.rx.SchedulerSupplier
import io.reactivex.Single
import io.reactivex.rxkotlin.plusAssign
import timber.log.Timber
import java.util.concurrent.TimeUnit.SECONDS
import javax.inject.Inject


@ApplicationScope class DiscoveryModel
@Inject constructor(private val manager: ConnectionManager,
                    private val schedulers: SchedulerSupplier) : BaseObservableModel<DiscoveryEvent>() {

    private companion object {
        @JvmStatic
        private val CONNECTION_TIMEOUT = 10L
        @JvmStatic
        private val connectionError = Single.error<Boolean>(ConnectionException("Connection Error"))
    }

    override fun onCleared() {
        super.onCleared()
        manager.stopDiscovery()

    }

    fun start() {
        disposables += manager.startDiscovery()
                .map { DiscoveringEvent(isDiscovering = it) }
                .observeOn(schedulers.main())
                .subscribe(this::onEvent, Timber::e)

        disposables += manager.discoveredDevices()
                .map(DiscoveryEvent::DevicesEvent)
                .observeOn(schedulers.main())
                .subscribe(this::onEvent)
    }

    /**
     * method to call when attempting to connect to a remote found end point
     * through discovered devices (other device will be in an advertising state)
     *
     * Steps taken :
     *
     * 1) Request connection initiation
     * 2) Listen for the success of initiation from initiated devices (callbacks in client are async),
     *    filtering by the remote endpoint. The connection is initiated on both sides, but not
     *    accepted automatically on advertising device (other device). The remote User
     *    will be prompted to accept this connection. If timeout reached or rejected by user
     *    an error is returned
     * 3) Listen for active connections that match the remote endpoint.  If timeout reached
     *    return error
     * 4) If successful have are now connected
     *
     * @param remoteUsername the readable user name of remote user device
     *
     * @param remoteEndpointId the unique id of the remote user device
     */
    fun requestConnection(remoteUsername: String, remoteEndpointId: String) {
        disposables += manager.requestConnectionInitiation(remoteEndpointId)
                .toObservable()
                .flatMapSingle { requestSuccess ->
                    if (requestSuccess) {
                        manager.initiatedConnections()
                                .map { devices -> devices.filter { d -> d.endpointId == remoteEndpointId } }
                                .filter { d -> d.isNotEmpty() }
                                .firstOrError()
                                .flatMap { manager.acceptConnection(remoteEndpointId) }
                    } else connectionError
                }.flatMapSingle { connectionSuccess ->
                    if (connectionSuccess) {
                        manager.activeConnections()
                                .map { devices -> devices.filter { d -> d.endpointId == remoteEndpointId } }
                                .map { d -> d.isNotEmpty() }
                                .filter { d -> d }
                                .firstOrError()
                    } else connectionError
                }.timeout(CONNECTION_TIMEOUT, SECONDS, schedulers.computation())
                .map(this::connectionResultToConnectionEvent)
                .startWith(connectionInitiatedEvent(remoteUsername))
                .observeOn(schedulers.main())
                .subscribe((this::onEvent), this::onConnectionError)
    }

    private fun connectionResultToConnectionEvent(isSuccess: Boolean) =
            ConnectionEvent(connectionSuccess = isSuccess,
                            connectionRequested = false,
                            remoteUsername = "")

    private fun connectionInitiatedEvent(remoteUsername: String) =
            ConnectionEvent(connectionSuccess = false,
                            connectionRequested = true,
                            remoteUsername = remoteUsername)

    private fun onConnectionError(e: Throwable) {
        onEvent(connectionResultToConnectionEvent(false))
        onEvent(DiscoveryEvent.ErrorEvent(R.string.discovery_error, e))
    }
}