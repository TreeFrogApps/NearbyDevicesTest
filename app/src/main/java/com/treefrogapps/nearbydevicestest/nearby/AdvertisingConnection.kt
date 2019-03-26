package com.treefrogapps.nearbydevicestest.nearby

import com.google.android.gms.nearby.connection.*
import com.treefrogapps.nearbydevicestest.di.ApplicationScope
import com.treefrogapps.nearbydevicestest.nearby.ConnectionType.ADVERTISING
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import javax.inject.Inject


@ApplicationScope class AdvertisingConnection
@Inject constructor(@Connection(ADVERTISING) private val connections: MutableMap<String, ConnectionState>,
                    @Connection(ADVERTISING) private val connectionSubject: PublishSubject<MutableMap<String, ConnectionState>>,
                    @Connection(ADVERTISING) private val advertisingOptions: AdvertisingOptions,
                    @Connection private val serviceId: String)
    : ObservableConnection<ConnectionLifecycleCallback, AdvertisingOptions, Observable<MutableMap<String, ConnectionState>>> {

    private val callback = object : ConnectionLifecycleCallback() {

        override fun onConnectionInitiated(p0: String, p1: ConnectionInfo) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun onConnectionResult(p0: String, p1: ConnectionResolution) {
            when (p1.status.statusCode) {
                ConnectionsStatusCodes.STATUS_OK -> { }
                ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED -> { }
                ConnectionsStatusCodes.STATUS_ERROR -> { }
            }

            // We're connected! Can now start sending and receiving data.
            // The connection was rejected by one or both sides.
            // The connection broke before it was able to be accepted.
            // Unknown status code
        }

        override fun onDisconnected(p0: String) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }
    }

    override fun callback(): ConnectionLifecycleCallback = callback

    override fun options(): AdvertisingOptions = advertisingOptions

    override fun observe(): Observable<MutableMap<String, ConnectionState>> = connectionSubject.startWith(HashMap(connections))
}