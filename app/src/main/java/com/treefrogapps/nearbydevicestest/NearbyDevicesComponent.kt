package com.treefrogapps.nearbydevicestest

import com.treefrogapps.nearbydevicestest.di.ApplicationScope
import com.treefrogapps.nearbydevicestest.messaging.MessagingActivityBuilder
import com.treefrogapps.nearbydevicestest.nearby.ConnectionModule
import dagger.BindsInstance
import dagger.Component
import dagger.android.support.AndroidSupportInjectionModule


@Component(
    modules = [
        AndroidSupportInjectionModule::class,
        NearbyDevicesModule::class,
        MessagingActivityBuilder::class,
        ConnectionModule::class]
)
@ApplicationScope interface NearbyDevicesComponent {

    @Component.Builder interface Builder {

        @BindsInstance fun addApp(app: NearbyDevicesApp): Builder

        fun build(): NearbyDevicesComponent
    }

    fun inject(app: NearbyDevicesApp)
}