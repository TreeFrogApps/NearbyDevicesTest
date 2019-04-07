package com.treefrogapps.nearbydevicestest.messaging

import com.treefrogapps.nearbydevicestest.di.ActivityScope
import com.treefrogapps.nearbydevicestest.messaging.devices.DevicesFragmentBuilder
import com.treefrogapps.nearbydevicestest.messaging.message.MessagesFragmentBuilder
import dagger.Module
import dagger.android.ContributesAndroidInjector


@Module abstract class MessagingActivityBuilder {

    @ContributesAndroidInjector(modules = [MessagesFragmentBuilder::class, DevicesFragmentBuilder::class])
    @ActivityScope
    abstract fun messagingActivity(): MessagingActivity
}