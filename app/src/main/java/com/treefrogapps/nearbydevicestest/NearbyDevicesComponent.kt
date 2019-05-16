package com.treefrogapps.nearbydevicestest

import com.treefrogapps.nearbydevicestest.di.ApplicationScope
import com.treefrogapps.nearbydevicestest.lifecycle.ViewModelModule
import com.treefrogapps.nearbydevicestest.messaging.MessagingActivityBuilder
import com.treefrogapps.nearbydevicestest.nearby.ConnectionModule
import com.treefrogapps.nearbydevicestest.rx.RxModule
import com.treefrogapps.nearbydevicestest.service.ConnectionServiceBuilder
import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjector
import dagger.android.support.AndroidSupportInjectionModule


@Component(modules = [
    AndroidSupportInjectionModule::class,
    NearbyDevicesAppModule::class,
    MessagingActivityBuilder::class,
    ConnectionServiceBuilder::class,
    ViewModelModule::class,
    ConnectionModule::class,
    RxModule::class])
@ApplicationScope interface NearbyDevicesComponent : AndroidInjector<NearbyDevicesApp> {

    @Component.Builder interface Builder {

        @BindsInstance fun addApp(app: NearbyDevicesApp): Builder

        fun build(): NearbyDevicesComponent
    }
}