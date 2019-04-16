package com.treefrogapps.nearbydevicestest.messaging.start

import android.content.Context
import android.os.Bundle
import android.view.View
import com.treefrogapps.nearbydevicestest.R
import com.treefrogapps.nearbydevicestest.app.BaseInjectionFragment
import com.treefrogapps.nearbydevicestest.app.createFragment
import com.treefrogapps.nearbydevicestest.messaging.FragmentTransactionListener
import com.treefrogapps.nearbydevicestest.messaging.devices.discovery.AdvertisingFragment
import com.treefrogapps.nearbydevicestest.messaging.devices.discovery.DiscoveryFragment
import com.treefrogapps.nearbydevicestest.rx.dispose
import com.treefrogapps.nearbydevicestest.rx.rxFlowableClickListener
import io.reactivex.Flowable
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.start_fragment.*


class StartFragment : BaseInjectionFragment() {

    private var disposable: Disposable? = null
    private var listener: FragmentTransactionListener? = null

    override fun layout(): Int = R.layout.start_fragment

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (context is FragmentTransactionListener) listener = context
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        disposable = Flowable.merge(
                rxFlowableClickListener(startDiscovery),
                rxFlowableClickListener(startAdvertising))
                .subscribe(this::start)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        dispose(disposable)
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    private fun start(view: View) {
        when (view.id) {
            R.id.startAdvertising -> createFragment<AdvertisingFragment>()
            R.id.startDiscovery   -> createFragment<DiscoveryFragment>()
            else                  -> null
        }?.let { listener?.onReplaceTransaction(it) }
    }

}