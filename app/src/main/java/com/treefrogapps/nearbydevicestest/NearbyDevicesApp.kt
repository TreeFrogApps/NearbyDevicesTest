package com.treefrogapps.nearbydevicestest

import android.app.Activity
import android.app.Application
import android.app.Service
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasActivityInjector
import dagger.android.HasServiceInjector
import io.reactivex.plugins.RxJavaPlugins
import timber.log.Timber
import javax.inject.Inject


class NearbyDevicesApp : Application(), HasActivityInjector, HasServiceInjector {

    @Inject lateinit var activityInjector: DispatchingAndroidInjector<Activity>
    @Inject lateinit var serviceInjector: DispatchingAndroidInjector<Service>

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) Timber.plant(Timber.DebugTree())

        RxJavaPlugins.setErrorHandler { e -> Timber.e(e, "undeliverable rx exception") }

        DaggerNearbyDevicesComponent.builder()
                .addApp(this)
                .build()
                .inject(this)
    }

    override fun activityInjector(): AndroidInjector<Activity> = activityInjector
    override fun serviceInjector(): AndroidInjector<Service> = serviceInjector
}