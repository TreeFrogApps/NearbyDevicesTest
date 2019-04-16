package com.treefrogapps.nearbydevicestest.messaging.start

import com.treefrogapps.nearbydevicestest.di.FragmentScope
import dagger.Module
import dagger.android.ContributesAndroidInjector


@Module abstract class StartFragmentBuilderModule {

    @ContributesAndroidInjector
    @FragmentScope
    abstract fun fragment(): StartFragment
}