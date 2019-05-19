package com.treefrogapps.nearbydevicestest.messaging.devices.discovery

import android.view.View
import com.treefrogapps.nearbydevicestest.app.BaseViewModel
import com.treefrogapps.nearbydevicestest.app.NearbyDevicesResources
import com.treefrogapps.nearbydevicestest.messaging.devices.discovery.DiscoveryEvent.*
import com.treefrogapps.nearbydevicestest.nearby.DiscoverConnection.DiscoveredDevice
import io.reactivex.Flowable

class DiscoveryViewModel(model: DiscoveryModel,
                         private val resources: NearbyDevicesResources)
    : BaseViewModel<DiscoveryViewDataModel, DiscoveryModel, DiscoveryEvent>(model, DiscoveryViewDataModel()) {

    override fun onFirstLaunch() {
        super.onFirstLaunch()
        model.start()
    }

    fun connectToDevice(discoveredDevice: DiscoveredDevice) {
        model.requestConnection(discoveredDevice.info?.endpointName.orEmpty(), discoveredDevice.endpointId)
    }

    fun observeFoundDevices(): Flowable<List<DiscoveredDevice>> =
            dataModelObservable
                    .map(DiscoveryViewDataModel::foundDevices)
                    .distinctUntilChanged()

    fun observeRecyclerViewVisibility(): Flowable<Int> =
            dataModelObservable
                    .map(DiscoveryViewDataModel::foundDevices)
                    .map { if (it.isNotEmpty()) View.VISIBLE else View.GONE }
                    .distinctUntilChanged()

    fun observeEmptyRecyclerViewTextVisibility(): Flowable<Int> =
            dataModelObservable
                    .map(DiscoveryViewDataModel::foundDevices)
                    .map { if (it.isEmpty()) View.VISIBLE else View.GONE }
                    .distinctUntilChanged()

    fun observeRemoteUsername(): Flowable<String> =
            dataModelObservable
                    .map { it.remoteUsername }

    fun observeConnectionSuccess(): Flowable<Boolean> =
            dataModelObservable
                    .map(DiscoveryViewDataModel::connectedToRemoteUser)

    fun observeStartAnimating(): Flowable<Boolean> =
            dataModelObservable.map(DiscoveryViewDataModel::startAnimations)
                    .distinctUntilChanged()

    override fun reduce(previousVDM: DiscoveryViewDataModel, event: DiscoveryEvent): DiscoveryViewDataModel {
        return when (event) {
            is DiscoveringEvent -> previousVDM.fromDiscoveringEvent(event, resources)
            is ConnectionEvent  -> previousVDM.fromConnectionEvent(event, resources)
            is DevicesEvent     -> previousVDM.fromDevicesEvent(event)
            is ErrorEvent       -> previousVDM.fromErrorEvent(event, resources)
        }
    }

}
