package com.treefrogapps.nearbydevicestest.service

import com.treefrogapps.nearbydevicestest.di.ServiceScope
import dagger.Module
import dagger.android.ContributesAndroidInjector


@Module abstract class ConnectionServiceBuilder {

    @ContributesAndroidInjector @ServiceScope abstract fun connectionService() : ConnectionService
}