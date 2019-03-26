package com.treefrogapps.nearbydevicestest.messaging

import com.treefrogapps.nearbydevicestest.di.ActivityScope
import dagger.Module
import dagger.android.ContributesAndroidInjector


@Module abstract class MessagingActivityBuilder {

    @ContributesAndroidInjector @ActivityScope abstract fun messagingActivity() : MessagingActivity
}