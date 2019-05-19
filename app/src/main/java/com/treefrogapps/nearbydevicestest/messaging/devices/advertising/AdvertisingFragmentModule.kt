package com.treefrogapps.nearbydevicestest.messaging.devices.discovery

import android.arch.lifecycle.ViewModelProviders
import com.treefrogapps.nearbydevicestest.di.FragmentScope
import com.treefrogapps.nearbydevicestest.lifecycle.ViewModelFactory
import com.treefrogapps.nearbydevicestest.messaging.devices.advertising.AdvertisingViewModel
import dagger.Module
import dagger.Provides

@Module object AdvertisingFragmentModule {

    @JvmStatic
    @Provides
    @FragmentScope
    fun provideViewModel(fragment: AdvertisingFragment, viewModelFactory: ViewModelFactory): AdvertisingViewModel =
            ViewModelProviders.of(fragment, viewModelFactory).get(AdvertisingViewModel::class.java)
}