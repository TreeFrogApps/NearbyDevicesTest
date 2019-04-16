package com.treefrogapps.nearbydevicestest.messaging.devices.discovery

import android.arch.lifecycle.ViewModelProviders
import com.treefrogapps.nearbydevicestest.di.FragmentScope
import com.treefrogapps.nearbydevicestest.lifecycle.ViewModelFactory
import dagger.Module
import dagger.Provides

@Module object DiscoveryFragmentModule {

    @JvmStatic
    @Provides
    @FragmentScope
    fun provideViewModel(fragment: DiscoveryFragment, viewModelFactory: ViewModelFactory): DiscoveryViewModel =
            ViewModelProviders.of(fragment, viewModelFactory).get(DiscoveryViewModel::class.java)
}