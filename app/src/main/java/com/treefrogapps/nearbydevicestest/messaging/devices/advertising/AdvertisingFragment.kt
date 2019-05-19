package com.treefrogapps.nearbydevicestest.messaging.devices.discovery

import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import com.treefrogapps.nearbydevicestest.R
import com.treefrogapps.nearbydevicestest.app.BaseViewModelInjectionFragment
import com.treefrogapps.nearbydevicestest.graphics.VectorAnimator
import com.treefrogapps.nearbydevicestest.graphics.VectorAnimator.Companion.of
import com.treefrogapps.nearbydevicestest.messaging.FragmentTransactionListener
import com.treefrogapps.nearbydevicestest.messaging.devices.advertising.AdvertisingEvent
import com.treefrogapps.nearbydevicestest.messaging.devices.advertising.AdvertisingModel
import com.treefrogapps.nearbydevicestest.messaging.devices.advertising.AdvertisingViewDataModel
import com.treefrogapps.nearbydevicestest.messaging.devices.advertising.AdvertisingViewModel
import com.treefrogapps.nearbydevicestest.rx.rxFlowableClickListener
import com.treefrogapps.nearbydevicestest.rx.rxTextViewSubscriber
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import kotlinx.android.synthetic.main.advertising_fragment.*

class AdvertisingFragment : BaseViewModelInjectionFragment<AdvertisingViewModel, AdvertisingViewDataModel, AdvertisingModel, AdvertisingEvent>() {

    private val disposables = CompositeDisposable()
    private var animator: VectorAnimator? = null
    private var listener: FragmentTransactionListener? = null

    override fun layout(): Int = R.layout.advertising_fragment

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (context is FragmentTransactionListener) listener = context
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        disposables += rxFlowableClickListener(advertisingConnectButton)
                .map {
                    Pair(advertisingDeviceIdTV.text.toString(),
                         advertisingDeviceTV.text.toString())
                }
                .subscribe { viewModel.onConnectDevice(it.first, it.second) }

        connectDataModel()
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        disposables.clear()
        animator?.clear()
    }

    private fun connectDataModel() {
        disposables.addAll(
                viewModel.observeDrawable().subscribe(this::startAnimation),
                viewModel.observeStatusText().rxTextViewSubscriber(advertisingTV),
                viewModel.observeDeviceText().rxTextViewSubscriber(advertisingDeviceTV),
                viewModel.observeDeviceId().rxTextViewSubscriber(advertisingDeviceIdTV),
                viewModel.observeButtonVisibility().subscribe { advertisingConnectButton.visibility = it },
                viewModel.observeConnection().subscribe { onConnected() })
    }

    private fun startAnimation(d: Drawable) {
        advertisingImage.setImageDrawable(d)
        animator = of(VectorAnimator.Repeat.INFINITE, d).apply { start() }
    }

    private fun onConnected() {
        // Todo replace fragment via listener to meesages fragment
    }
}
