package com.treefrogapps.nearbydevicestest.messaging.devices.discovery

import android.os.Bundle
import android.view.View
import com.treefrogapps.nearbydevicestest.R
import com.treefrogapps.nearbydevicestest.app.BaseViewModelInjectionFragment
import com.treefrogapps.nearbydevicestest.messaging.devices.discovery.AdvertisingViewModel.DataModel

class AdvertisingFragment : BaseViewModelInjectionFragment<AdvertisingViewModel, DataModel>() {

    override fun layout(): Int = R.layout.advertising_fragment

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

}
