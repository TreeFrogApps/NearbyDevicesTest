package com.treefrogapps.nearbydevicestest

import android.app.Activity
import android.app.Application
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasActivityInjector
import io.reactivex.plugins.RxJavaPlugins
import timber.log.Timber
import javax.inject.Inject


class NearbyDevicesApp : Application(), HasActivityInjector {

    @Inject lateinit var activityInjector: DispatchingAndroidInjector<Activity>

    override fun onCreate() {
        super.onCreate()

        if(BuildConfig.DEBUG) Timber.plant(Timber.DebugTree())

        RxJavaPlugins.setErrorHandler { e -> Timber.e(e, "undeliverable rx exception") }

        DaggerNearbyDevicesComponent.builder()
            .addApp(this)
            .build()
            .inject(this)
    }

    override fun activityInjector(): AndroidInjector<Activity> = activityInjector
}