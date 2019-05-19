package com.treefrogapps.nearbydevicestest.app

import android.os.Bundle
import android.support.annotation.LayoutRes
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import dagger.android.support.DaggerFragment
import javax.inject.Inject


abstract class BaseViewModelInjectionFragment<VM : BaseViewModel<VDM, M, E>, VDM, M : BaseObservableModel<E>, E> : DaggerFragment() {

    @Inject protected lateinit var viewModel: VM

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(layout(), container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if(savedInstanceState == null) viewModel.onFirstLaunch()
    }

    @LayoutRes abstract fun layout(): Int
}