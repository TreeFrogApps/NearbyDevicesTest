package com.treefrogapps.nearbydevicestest.lifecycle

import android.arch.lifecycle.ViewModel
import com.treefrogapps.nearbydevicestest.messaging.devices.DevicesModel
import com.treefrogapps.nearbydevicestest.messaging.devices.DevicesViewModel
import com.treefrogapps.nearbydevicestest.messaging.message.MessagesModel
import com.treefrogapps.nearbydevicestest.messaging.message.MessagesViewModel
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap

@Module object ViewModelModule {

    @JvmStatic
    @Provides
    @IntoMap
    @ViewModelKey(MessagesViewModel::class)
    fun messageViewModel(model: MessagesModel) : ViewModel = MessagesViewModel(model)

    @JvmStatic
    @Provides
    @IntoMap
    @ViewModelKey(DevicesViewModel::class)
    fun devicesViewModel(model: DevicesModel) : ViewModel = DevicesViewModel(model)
}