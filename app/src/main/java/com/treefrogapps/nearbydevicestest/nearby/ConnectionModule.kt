package com.treefrogapps.nearbydevicestest.nearby

import android.content.Context
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.*
import com.treefrogapps.nearbydevicestest.di.ApplicationScope
import com.treefrogapps.nearbydevicestest.nearby.ConnectionType.*
import dagger.Binds
import dagger.Module
import dagger.Provides
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.processors.PublishProcessor
import io.reactivex.subjects.PublishSubject


@Module(includes = [ConnectionModule.Static::class]) abstract class ConnectionModule {

    @Module object Static {

        @Provides
        @ApplicationScope
        @JvmStatic
        fun connectionsClient(context: Context): ConnectionsClient =
                Nearby.getConnectionsClient(context)

        @Provides
        @ApplicationScope
        @Connection
        @JvmStatic
        fun serviceName(context: Context): String = context.packageName

        @Provides
        @ApplicationScope
        @Connection(ADVERTISING)
        @JvmStatic
        fun advertisingMap(): MutableSet<String> = mutableSetOf()

        @Provides
        @ApplicationScope
        @Connection(ADVERTISING)
        @JvmStatic
        fun advertisingSubject(): PublishSubject<MutableMap<String, ConnectionState>> = PublishSubject.create()

        @Provides
        @ApplicationScope
        @Connection(ADVERTISING)
        @JvmStatic
        fun advertisingOptions(): AdvertisingOptions =
                AdvertisingOptions.Builder()
                        .setStrategy(Strategy.P2P_CLUSTER)
                        .build()

        @Provides
        @ApplicationScope
        @Connection(DISCOVER)
        @JvmStatic
        fun discoverSet(): MutableSet<String> = mutableSetOf()

        @Provides
        @ApplicationScope
        @Connection(DISCOVER)
        @JvmStatic
        fun discoverSubject(): PublishSubject<MutableSet<String>> = PublishSubject.create()

        @Provides
        @ApplicationScope
        @Connection(DISCOVER)
        @JvmStatic
        fun discoveryOptions(): DiscoveryOptions =
                DiscoveryOptions.Builder()
                        .setStrategy(Strategy.P2P_CLUSTER)
                        .build()

        @Provides
        @Connection(PAYLOAD)
        @ApplicationScope
        @JvmStatic
        fun payloadSubject(): PublishProcessor<Pair<String, Payload>> = PublishProcessor.create()
    }

    @Binds
    @ApplicationScope
    @Connection(ADVERTISING)
    abstract fun advertisingConnection(connection : AdvertisingConnection)
            : ObservableConnection<ConnectionLifecycleCallback, AdvertisingOptions, Observable<MutableMap<String, ConnectionState>>>

    @Binds
    @ApplicationScope
    @Connection(DISCOVER)
    abstract fun discoverConnection(connection : DiscoverConnection)
            : ObservableConnection<EndpointDiscoveryCallback, DiscoveryOptions, Observable<MutableSet<String>>>

    @Binds
    @ApplicationScope
    @Connection(PAYLOAD)
    abstract fun payloadConnection(connection: PayloadConnection)
            : ObservableConnection<PayloadCallback, Unit, Flowable<Pair<String, Payload>>>
}