package com.treefrogapps.nearbydevicestest.rx

import com.treefrogapps.nearbydevicestest.di.ApplicationScope
import dagger.Module
import dagger.Provides
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers


@Module object RxModule {

    @Provides
    @ApplicationScope
    @JvmStatic
    fun schedulers(): SchedulerSupplier = object : SchedulerSupplier {
        override fun io() = Schedulers.io()
        override fun computation() = Schedulers.computation()
        override fun main() = AndroidSchedulers.mainThread()
        override fun new() = Schedulers.newThread()
    }
}