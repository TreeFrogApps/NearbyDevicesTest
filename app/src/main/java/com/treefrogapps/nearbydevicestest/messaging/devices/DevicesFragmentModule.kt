package com.treefrogapps.nearbydevicestest.messaging.devices

import android.arch.lifecycle.ViewModelProviders
import com.treefrogapps.nearbydevicestest.di.FragmentScope
import com.treefrogapps.nearbydevicestest.lifecycle.ViewModelFactory
import dagger.Module
import dagger.Provides

@Module object DevicesFragmentModule {

    @JvmStatic
    @Provides
    @FragmentScope
    fun provideViewModel(fragment: DevicesFragment, viewModelFactory: ViewModelFactory): DevicesViewModel =
            ViewModelProviders.of(fragment, viewModelFactory).get(DevicesViewModel::class.java)
}