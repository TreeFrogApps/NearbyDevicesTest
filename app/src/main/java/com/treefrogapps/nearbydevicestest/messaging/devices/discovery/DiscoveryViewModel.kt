package com.treefrogapps.nearbydevicestest.messaging.devices.discovery

import android.arch.lifecycle.ViewModel
import android.view.View
import com.treefrogapps.nearbydevicestest.app.NearbyDevicesResources
import com.treefrogapps.nearbydevicestest.messaging.devices.discovery.DiscoveryEvent.*
import com.treefrogapps.nearbydevicestest.nearby.DiscoverConnection.DiscoveredDevice
import com.treefrogapps.nearbydevicestest.rx.SchedulerSupplier
import io.reactivex.Flowable
import io.reactivex.Single

class DiscoveryViewModel(private val discoveryModel: DiscoveryModel,
                         private val resources: NearbyDevicesResources,
                         scheduler: SchedulerSupplier) : ViewModel() {

    private val dataModelObservable =
            discoveryModel.observeEvents()
                    .scan(initialViewDataModel(), ::reduce)
                    .observeOn(scheduler.main())
                    .replay(1)

    override fun onCleared() {
        discoveryModel.onCleared()
    }

    fun startDiscovery() {
        discoveryModel.listenForDiscoveredDevices()
        discoveryModel.startDiscoveringDevices()
    }

    fun connectToDevice(discoveredDevice: DiscoveredDevice) {
        discoveryModel.requestConnection(discoveredDevice.info?.endpointName.orEmpty(), discoveredDevice.endpointId)
    }

    fun observeFoundDevices(): Flowable<List<DiscoveredDevice>> =
            dataModelObservable.autoConnect()
                    .map(DiscoveryViewDataModel::foundDevices)
                    .distinctUntilChanged()

    fun observeRecyclerViewVisibility(): Flowable<Int> =
            dataModelObservable.autoConnect()
                    .map(DiscoveryViewDataModel::foundDevices)
                    .map { if (it.isNotEmpty()) View.VISIBLE else View.GONE }
                    .distinctUntilChanged()

    fun observeEmptyRecyclerViewTextVisibility(): Flowable<Int> =
            dataModelObservable.autoConnect()
                    .map(DiscoveryViewDataModel::foundDevices)
                    .map { if (it.isEmpty()) View.VISIBLE else View.GONE }
                    .distinctUntilChanged()

    fun observeRemoteUsername(): Flowable<String> =
            dataModelObservable.autoConnect()
                    .map { it.remoteUsername }

    fun observeConnectionSuccess(): Flowable<Boolean> =
            dataModelObservable.autoConnect()
                    .map(DiscoveryViewDataModel::connectedToRemoteUser)

    fun observeStartAnimating() : Single<Boolean> =
            dataModelObservable.autoConnect().map(DiscoveryViewDataModel::startAnimations)
                    .filter { it }
                    .first(false)

    private fun reduce(previousDataModel: DiscoveryViewDataModel, event: DiscoveryEvent): DiscoveryViewDataModel {
        return when (event) {
            is DiscoveringEvent -> previousDataModel.fromDiscoveringEvent(event, resources)
            is ConnectionEvent  -> previousDataModel.fromConnectionEvent(event, resources)
            is DevicesEvent     -> previousDataModel.fromDevicesEvent(event)
        }
    }

    private fun initialViewDataModel(): DiscoveryViewDataModel = DiscoveryViewDataModel()
}
