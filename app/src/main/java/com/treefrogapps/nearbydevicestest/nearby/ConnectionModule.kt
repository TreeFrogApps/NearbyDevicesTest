package com.treefrogapps.nearbydevicestest.nearby

import android.content.Context
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.*
import com.treefrogapps.nearbydevicestest.Package
import com.treefrogapps.nearbydevicestest.di.ApplicationScope
import com.treefrogapps.nearbydevicestest.nearby.AdvertisingConnection.InboundDevice
import com.treefrogapps.nearbydevicestest.nearby.ConnectionType.*
import com.treefrogapps.nearbydevicestest.nearby.DiscoverConnection.DiscoveredDevice
import dagger.Binds
import dagger.Module
import dagger.Provides
import io.reactivex.Scheduler
import io.reactivex.processors.BehaviorProcessor
import io.reactivex.processors.PublishProcessor
import io.reactivex.schedulers.Schedulers
import java.util.*
import java.util.concurrent.Executors


@Module(includes = [ConnectionModule.Static::class]) abstract class ConnectionModule {

    @Module object Static {

        @Provides
        @ApplicationScope
        @JvmStatic
        fun connectionsClient(context: Context): ConnectionsClient =
                Nearby.getConnectionsClient(context)

        @Provides
        @ApplicationScope
        @NearbyConnection
        @JvmStatic
        fun endpointId(@Package packageName : String): String = "$packageName:${UUID.randomUUID()}"

        @Provides
        @ApplicationScope
        @NearbyConnection
        @JvmStatic
        fun errorProcessor(): PublishProcessor<ConnectionError> = PublishProcessor.create()

        @Provides
        @ApplicationScope
        @NearbyConnection
        @JvmStatic
        fun scheduler(): Scheduler = Schedulers.from(Executors.newSingleThreadExecutor { Thread(it, "Connection Manager Thread") })

        @Provides
        @ApplicationScope
        @NearbyConnection(ADVERTISING)
        @JvmStatic
        fun advertisingProcessor(): BehaviorProcessor<InboundDevice> = BehaviorProcessor.create()

        @Provides
        @ApplicationScope
        @NearbyConnection(ADVERTISING)
        @JvmStatic
        fun advertisingOptions(): AdvertisingOptions =
                AdvertisingOptions.Builder()
                        .setStrategy(Strategy.P2P_CLUSTER)
                        .build()

        @Provides
        @ApplicationScope
        @NearbyConnection(DISCOVER)
        @JvmStatic
        fun discoverProcessor(): BehaviorProcessor<DiscoveredDevice> = BehaviorProcessor.create()

        @Provides
        @ApplicationScope
        @NearbyConnection(DISCOVER)
        @JvmStatic
        fun discoveryOptions(): DiscoveryOptions =
                DiscoveryOptions.Builder()
                        .setStrategy(Strategy.P2P_CLUSTER)
                        .build()

        @Provides
        @NearbyConnection(PAYLOAD)
        @ApplicationScope
        @JvmStatic
        fun payloadProcessor(): PublishProcessor<Pair<String, Payload>> = PublishProcessor.create()
    }

    @Binds
    @ApplicationScope
    @NearbyConnection(ADVERTISING)
    abstract fun advertisingConnection(connection: AdvertisingConnection): Connection<ConnectionLifecycleCallback, AdvertisingOptions, InboundDevice>

    @Binds
    @ApplicationScope
    @NearbyConnection(DISCOVER)
    abstract fun discoverConnection(connection: DiscoverConnection): Connection<EndpointDiscoveryCallback, DiscoveryOptions, DiscoveredDevice>

    @Binds
    @ApplicationScope
    @NearbyConnection(PAYLOAD)
    abstract fun payloadConnection(connection: PayloadConnection): Connection<PayloadCallback, Unit, Pair<String, Payload>>
}