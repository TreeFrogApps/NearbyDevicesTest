package com.treefrogapps.nearbydevicestest.lifecycle

import android.arch.lifecycle.ViewModel
import com.treefrogapps.nearbydevicestest.app.NearbyDevicesResources
import com.treefrogapps.nearbydevicestest.messaging.devices.discovery.AdvertisingModel
import com.treefrogapps.nearbydevicestest.messaging.devices.discovery.AdvertisingViewModel
import com.treefrogapps.nearbydevicestest.messaging.devices.discovery.DiscoveryModel
import com.treefrogapps.nearbydevicestest.messaging.devices.discovery.DiscoveryViewModel
import com.treefrogapps.nearbydevicestest.messaging.message.MessagesModel
import com.treefrogapps.nearbydevicestest.messaging.message.MessagesViewModel
import com.treefrogapps.nearbydevicestest.rx.SchedulerSupplier
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap

@Module object ViewModelModule {

    @JvmStatic
    @Provides
    @IntoMap
    @ViewModelKey(MessagesViewModel::class)
    fun messageViewModel(model: MessagesModel, scheduler: SchedulerSupplier, resources: NearbyDevicesResources): ViewModel =
            MessagesViewModel(model, resources, scheduler)

    @JvmStatic
    @Provides
    @IntoMap
    @ViewModelKey(DiscoveryViewModel::class)
    fun discoveryViewModel(model: DiscoveryModel, scheduler: SchedulerSupplier, resources: NearbyDevicesResources): ViewModel =
            DiscoveryViewModel(model, resources, scheduler)

    @JvmStatic
    @Provides
    @IntoMap
    @ViewModelKey(AdvertisingViewModel::class)
    fun advertisingViewModel(model: AdvertisingModel, scheduler: SchedulerSupplier, resources: NearbyDevicesResources): ViewModel =
            AdvertisingViewModel(model, resources, scheduler)
}