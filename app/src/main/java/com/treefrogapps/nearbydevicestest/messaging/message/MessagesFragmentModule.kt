package com.treefrogapps.nearbydevicestest.messaging.message

import android.arch.lifecycle.ViewModelProviders
import com.treefrogapps.nearbydevicestest.di.FragmentScope
import com.treefrogapps.nearbydevicestest.lifecycle.ViewModelFactory
import dagger.Module
import dagger.Provides

@Module object MessagesFragmentModule {

    @JvmStatic
    @Provides
    @FragmentScope
    fun provideViewModel(fragment: MessagesFragment, viewModelFactory: ViewModelFactory): MessagesViewModel =
            ViewModelProviders.of(fragment, viewModelFactory).get(MessagesViewModel::class.java)
}