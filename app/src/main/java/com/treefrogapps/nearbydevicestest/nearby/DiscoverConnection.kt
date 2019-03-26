package com.treefrogapps.nearbydevicestest.nearby

import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo
import com.google.android.gms.nearby.connection.DiscoveryOptions
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback
import com.treefrogapps.nearbydevicestest.di.ApplicationScope
import com.treefrogapps.nearbydevicestest.nearby.ConnectionType.DISCOVER
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import javax.inject.Inject


@ApplicationScope class DiscoverConnection
@Inject constructor(@Connection(DISCOVER) private val connections: MutableSet<String>,
                    @Connection(DISCOVER) private val connectionSubject: PublishSubject<MutableSet<String>>,
                    @Connection(DISCOVER) private val connectionOptions : DiscoveryOptions,
                    @Connection private val serviceId: String)
    : ObservableConnection<EndpointDiscoveryCallback, DiscoveryOptions, Observable<MutableSet<String>>> {

    private val callback = object : EndpointDiscoveryCallback() {

        override fun onEndpointFound(p0: String, p1: DiscoveredEndpointInfo) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun onEndpointLost(p0: String) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }
    }

    override fun callback(): EndpointDiscoveryCallback = callback

    override fun options(): DiscoveryOptions = connectionOptions

    override fun observe(): Observable<MutableSet<String>> = connectionSubject.startWith(connections)
}