package com.treefrogapps.nearbydevicestest.messaging

import com.treefrogapps.nearbydevicestest.di.ActivityScope
import com.treefrogapps.nearbydevicestest.messaging.devices.discovery.AdvertisingFragmentBuilder
import com.treefrogapps.nearbydevicestest.messaging.devices.discovery.DiscoveryFragmentBuilder
import com.treefrogapps.nearbydevicestest.messaging.message.MessagesFragmentBuilder
import com.treefrogapps.nearbydevicestest.messaging.start.StartFragmentBuilderModule
import dagger.Module
import dagger.android.ContributesAndroidInjector


@Module
abstract class MessagingActivityBuilder {

    @ContributesAndroidInjector(modules = [
        StartFragmentBuilderModule::class,
        MessagesFragmentBuilder::class,
        AdvertisingFragmentBuilder::class,
        DiscoveryFragmentBuilder::class
    ])
    @ActivityScope
    abstract fun messagingActivity(): MessagingActivity
}