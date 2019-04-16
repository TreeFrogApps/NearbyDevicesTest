package com.treefrogapps.nearbydevicestest.messaging.devices.discovery

import com.treefrogapps.nearbydevicestest.di.FragmentScope
import dagger.Module
import dagger.android.ContributesAndroidInjector


@Module abstract class AdvertisingFragmentBuilder {

    @ContributesAndroidInjector(modules = [AdvertisingFragmentModule::class])
    @FragmentScope
    abstract fun advertisingFragment() : AdvertisingFragment
}