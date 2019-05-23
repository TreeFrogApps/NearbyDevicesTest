package com.treefrogapps.nearbydevicestest.messaging.devices.advertising

import com.treefrogapps.nearbydevicestest.app.BaseObservableModel
import com.treefrogapps.nearbydevicestest.di.ApplicationScope
import com.treefrogapps.nearbydevicestest.messaging.devices.advertising.AdvertisingEvent.*
import com.treefrogapps.nearbydevicestest.nearby.ConnectionException
import com.treefrogapps.nearbydevicestest.nearby.ConnectionManager
import com.treefrogapps.nearbydevicestest.rx.SchedulerSupplier
import io.reactivex.Single
import io.reactivex.rxkotlin.plusAssign
import java.util.concurrent.TimeUnit.SECONDS
import javax.inject.Inject


@ApplicationScope class AdvertisingModel
@Inject constructor(private val manager: ConnectionManager,
                    private val schedulers: SchedulerSupplier) : BaseObservableModel<AdvertisingEvent>() {

    private companion object {
        @JvmStatic
        private val CONNECTION_TIMEOUT = 30L
        @JvmStatic
        private val connectionError = Single.error<Boolean>(ConnectionException("Connection Error"))
    }

    /**
     * start advertising and wait for the FIRST connection that is initiated - this occurs when
     * a remote device sees this device and requests connection, user will be asked if they which to
     * accpet of deny the connection
     */
    fun start() {
        disposables += manager.start()
                .map(::Advertising)
                .observeOn(schedulers.main())
                .subscribe(this::onEvent, this::onError)

        disposables += manager.initiatedConnections()
                .filter { it.isNotEmpty() }
                .map { it.first() }
                .firstElement()
                .map { Connection(it.endpointId, it.username, false) }
                .observeOn(schedulers.main())
                .subscribe(this::onEvent, this::onError)
    }

    /**
     * Accept the device connection, if successful this should transition from
     * initiated to connected (wait for up to 7 seconds for this to occur, otherwise timeout)
     */
    fun acceptConnection(endpointId: String, username : String) {
        disposables += manager.acceptConnection(endpointId)
                .flatMap { connectionSuccess ->
                    if (connectionSuccess) {
                        manager.activeConnections()
                                .map { devices -> devices.filter { d -> d.endpointId == endpointId } }
                                .map { d -> d.isNotEmpty() }
                                .filter { d -> d }
                                .firstOrError()
                    } else connectionError
                }.timeout(CONNECTION_TIMEOUT, SECONDS, schedulers.computation())
                .map { Connection(endpointId, username, it) }
                .observeOn(schedulers.main())
                .subscribe(this::onEvent, this::onError)
    }

    private fun onError(t: Throwable) {
        onEvent(Error(t))
    }
}