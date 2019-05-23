package com.treefrogapps.nearbydevicestest.messaging.devices.discovery.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.treefrogapps.nearbydevicestest.R
import com.treefrogapps.nearbydevicestest.app.BaseRecyclerAdapter
import com.treefrogapps.nearbydevicestest.nearby.DiscoverConnection.DiscoveredDevice
import kotlinx.android.synthetic.main.discovered_device_row.view.*


class DiscoveryAdapter : BaseRecyclerAdapter<DiscoveredDevice, DiscoveryAdapter.DiscoveryViewHolder>() {

    override fun onCreateViewHolder(group: ViewGroup, p1: Int): DiscoveryViewHolder =
            DiscoveryViewHolder(LayoutInflater.from(group.context).inflate(R.layout.discovered_device_row, group, false))

    override fun onViewRecycled(holder: DiscoveryViewHolder) {
        super.onViewRecycled(holder)
        holder.unbind()
    }

    class DiscoveryViewHolder(itemView: View) : BaseViewHolder<DiscoveredDevice>(itemView) {

        override fun onBind(t: DiscoveredDevice) {
            itemView.deviceEndpointId.text = t.endpointId
            itemView.deviceUsername.text = t.info?.endpointName ?: "unknown user"
            itemView.deviceConnect.setOnClickListener(this)
        }

        override fun onUnbind() {
            itemView.deviceConnect.setOnClickListener(null)
        }
    }
}