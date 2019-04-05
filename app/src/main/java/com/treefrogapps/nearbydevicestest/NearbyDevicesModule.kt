package com.treefrogapps.nearbydevicestest

import android.content.Context
import com.treefrogapps.nearbydevicestest.di.ApplicationScope
import dagger.Module
import dagger.Provides


@Module object NearbyDevicesModule {

    @Provides @ApplicationScope @JvmStatic fun context(app: NearbyDevicesApp) : Context = app.applicationContext

    @Provides @ApplicationScope @Package @JvmStatic fun packageName(context: Context) : String = context.packageName
}