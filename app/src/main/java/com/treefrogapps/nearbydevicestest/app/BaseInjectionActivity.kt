package com.treefrogapps.nearbydevicestest.app

import android.os.Bundle
import android.support.annotation.LayoutRes
import dagger.android.support.DaggerAppCompatActivity


abstract class BaseInjectionActivity : DaggerAppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layout())
    }

    @LayoutRes abstract fun layout() : Int

}