package com.treefrogapps.nearbydevicestest.messaging.devices.discovery

import com.treefrogapps.nearbydevicestest.di.FragmentScope
import dagger.Module
import dagger.android.ContributesAndroidInjector


@Module abstract class DiscoveryFragmentBuilder {

    @ContributesAndroidInjector(modules = [DiscoveryFragmentModule::class])
    @FragmentScope
    abstract fun discoveryFragment() : DiscoveryFragment
}