package com.treefrogapps.nearbydevicestest.messaging.devices.discovery

import android.content.Context
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import com.treefrogapps.nearbydevicestest.R
import com.treefrogapps.nearbydevicestest.app.BaseRecyclerAdapter
import com.treefrogapps.nearbydevicestest.app.BaseViewModelInjectionFragment
import com.treefrogapps.nearbydevicestest.app.createFragment
import com.treefrogapps.nearbydevicestest.graphics.VectorAnimator
import com.treefrogapps.nearbydevicestest.graphics.VectorAnimator.Companion.of
import com.treefrogapps.nearbydevicestest.messaging.FragmentTransactionListener
import com.treefrogapps.nearbydevicestest.messaging.devices.discovery.adapter.DiscoveryAdapter
import com.treefrogapps.nearbydevicestest.messaging.devices.discovery.adapter.DiscoveryAdapter.DiscoveryViewHolder
import com.treefrogapps.nearbydevicestest.messaging.message.MessagesFragment
import com.treefrogapps.nearbydevicestest.nearby.DiscoverConnection.DiscoveredDevice
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import kotlinx.android.synthetic.main.discovery_fragment.*

class DiscoveryFragment : BaseViewModelInjectionFragment<DiscoveryViewModel, DiscoveryViewDataModel, DiscoveryModel, DiscoveryEvent>() {

    private lateinit var discoveryAdapter: BaseRecyclerAdapter<DiscoveredDevice, DiscoveryViewHolder>
    private lateinit var animator: VectorAnimator
    private val disposables = CompositeDisposable()
    private var listener: FragmentTransactionListener? = null

    override fun layout(): Int = R.layout.discovery_fragment

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (context is FragmentTransactionListener) listener = context
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupView()
        connectDataModel()
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    private fun setupView() {
        discoveryAdapter = DiscoveryAdapter()
        disposables += discoveryAdapter.observeClickEvents().subscribe(viewModel::connectToDevice)

        discoveryRecyclerView.layoutManager = LinearLayoutManager(context)
        discoveryRecyclerView.adapter = discoveryAdapter
        animator = of(VectorAnimator.Repeat.INFINITE, discoveryDotsImageView.drawable, discoveryDeviceImageView.drawable)
    }

    private fun connectDataModel() {
        disposables.addAll(viewModel.observeFoundDevices().subscribe(this::updateFoundDevices),
                           viewModel.observeStartAnimating().subscribe({ if(it) animator.start() else animator.stop() }, { }),
                           viewModel.observeRecyclerViewVisibility().subscribe(discoveryRecyclerView::setVisibility),
                           viewModel.observeEmptyRecyclerViewTextVisibility().subscribe(discoveryFoundTextView::setVisibility),
                           viewModel.observeRemoteUsername().subscribe(discoveryRemoteUserTextView::setText),
                           viewModel.observeConnectionSuccess().subscribe(this::onConnection))
    }

    private fun updateFoundDevices(devices: List<DiscoveredDevice>) {
        discoveryAdapter.updateList(devices)
    }

    private fun onConnection(success: Boolean) {
        if(success) listener?.onReplaceTransaction(createFragment<MessagesFragment>())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        disposables.clear()
        animator.clear()
    }
}
