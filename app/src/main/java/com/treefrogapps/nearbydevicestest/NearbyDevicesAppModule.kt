package com.treefrogapps.nearbydevicestest

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.treefrogapps.nearbydevicestest.NearbyDevicesAppModule.NearbyDevicesAppModuleCompanion
import com.treefrogapps.nearbydevicestest.app.NearbyDevicesResources
import com.treefrogapps.nearbydevicestest.app.ResourcesProxy
import com.treefrogapps.nearbydevicestest.di.ApplicationScope
import dagger.Binds
import dagger.Module
import dagger.Provides
import java.util.*
import java.util.function.Consumer
import java.util.function.Supplier


@Module(includes = [NearbyDevicesAppModuleCompanion::class])
abstract class NearbyDevicesAppModule {

    @Module object NearbyDevicesAppModuleCompanion {

        private const val USERNAME_KEY = "remoteUsername"

        @JvmStatic
        @Provides
        @ApplicationScope
        fun context(app: NearbyDevicesApp): Context = app.applicationContext

        @JvmStatic
        @Provides
        @ApplicationScope
        @Package
        fun packageName(context: Context): String = context.packageName

        @JvmStatic
        @Provides
        @ApplicationScope
        fun gson(): Gson =
                GsonBuilder()
                        .setPrettyPrinting()
                        .setLenient()
                        .create()

        @Provides
        @ApplicationScope
        @JvmStatic
        fun preferences(context: Context): SharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(context)

        @JvmStatic
        @Provides
        @ApplicationScope
        @User
        fun usernameSupplier(preferences: SharedPreferences): Supplier<String> {
            val randomUser = Random().nextInt()
            return Supplier { preferences.getString(USERNAME_KEY, "Test User :: $randomUser") }
        }

        @JvmStatic
        @Provides
        @ApplicationScope
        @User
        fun usernameConsumer(preferences: SharedPreferences): Consumer<String> =
                Consumer { preferences.edit().putString(USERNAME_KEY, it).apply() }
    }

    @Binds
    @ApplicationScope
    abstract fun resources(resourcesProxy: ResourcesProxy): NearbyDevicesResources

}