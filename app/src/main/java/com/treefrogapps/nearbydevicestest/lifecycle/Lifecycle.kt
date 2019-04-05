package com.treefrogapps.nearbydevicestest.lifecycle

import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.LifecycleOwner


interface Lifecycle : LifecycleObserver {

    fun initialise(owner: LifecycleOwner)
}