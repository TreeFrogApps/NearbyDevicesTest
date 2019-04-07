package com.treefrogapps.nearbydevicestest

import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.treefrogapps.nearbydevicestest.di.ApplicationScope
import dagger.Module
import dagger.Provides


@Module object NearbyDevicesModule {

    @Provides @ApplicationScope @JvmStatic fun context(app: NearbyDevicesApp) : Context = app.applicationContext

    @Provides @ApplicationScope @Package @JvmStatic fun packageName(context: Context) : String = context.packageName

    @Provides @ApplicationScope @JvmStatic fun gson() : Gson =
        GsonBuilder()
            .setPrettyPrinting()
            .setLenient()
            .create()


}