package com.treefrogapps.nearbydevicestest.messaging.devices

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.treefrogapps.nearbydevicestest.R
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

class DevicesFragment : Fragment() {

    @Inject lateinit var viewModel: DevicesViewModel

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.devices_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        AndroidSupportInjection.inject(this)

        // TODO: Use the ViewModel
    }

}
