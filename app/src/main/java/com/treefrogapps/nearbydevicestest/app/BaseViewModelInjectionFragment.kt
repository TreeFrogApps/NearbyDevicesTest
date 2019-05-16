package com.treefrogapps.nearbydevicestest.app

import android.arch.lifecycle.ViewModel
import android.os.Bundle
import android.support.annotation.LayoutRes
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import dagger.android.support.DaggerFragment
import javax.inject.Inject


abstract class BaseViewModelInjectionFragment<VM : ViewModel, T> : DaggerFragment() {

    @Inject protected lateinit var viewModel : VM

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(layout(), container, false)
    }

    @LayoutRes abstract fun layout() : Int
}