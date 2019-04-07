package com.treefrogapps.nearbydevicestest.messaging.devices

import com.treefrogapps.nearbydevicestest.di.FragmentScope
import dagger.Module
import dagger.android.ContributesAndroidInjector


@Module abstract class DevicesFragmentBuilder {

    @ContributesAndroidInjector(modules = [DevicesFragmentModule::class])
    @FragmentScope
    abstract fun devicesFragment() : DevicesFragment
}