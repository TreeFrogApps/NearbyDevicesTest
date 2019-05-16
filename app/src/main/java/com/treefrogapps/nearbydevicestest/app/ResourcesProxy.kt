package com.treefrogapps.nearbydevicestest.app

import android.content.Context
import android.graphics.drawable.Drawable
import javax.inject.Inject


class ResourcesProxy @Inject constructor(private val context: Context) : NearbyDevicesResources {

    override fun getString(id: Int): String = context.getString(id)

    override fun getDrawable(id: Int): Drawable? = context.getDrawable(id)
}