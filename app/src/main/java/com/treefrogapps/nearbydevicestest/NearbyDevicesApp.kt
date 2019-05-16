package com.treefrogapps.nearbydevicestest

import dagger.android.AndroidInjector
import dagger.android.support.DaggerApplication
import io.reactivex.plugins.RxJavaPlugins
import timber.log.Timber


class NearbyDevicesApp : DaggerApplication(){

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) Timber.plant(Timber.DebugTree())

        RxJavaPlugins.setErrorHandler { e -> Timber.e(e, "undeliverable rx exception") }
    }

    override fun applicationInjector(): AndroidInjector<out DaggerApplication> =
            DaggerNearbyDevicesComponent.builder()
                    .addApp(this)
                    .build()
}