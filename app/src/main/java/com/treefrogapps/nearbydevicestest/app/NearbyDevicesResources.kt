package com.treefrogapps.nearbydevicestest.app

import android.graphics.drawable.Drawable
import android.support.annotation.DrawableRes
import android.support.annotation.StringRes


interface NearbyDevicesResources {

    fun getString(@StringRes id : Int) : String

    fun getDrawable(@DrawableRes id : Int) : Drawable?
}