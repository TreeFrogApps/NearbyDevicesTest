package com.treefrogapps.nearbydevicestest.messaging.message

import com.treefrogapps.nearbydevicestest.di.FragmentScope
import dagger.Module
import dagger.android.ContributesAndroidInjector


@Module abstract class MessagesFragmentBuilder {

    @ContributesAndroidInjector(modules = [MessagesFragmentModule::class])
    @FragmentScope
    abstract fun fragment(): MessagesFragment
}