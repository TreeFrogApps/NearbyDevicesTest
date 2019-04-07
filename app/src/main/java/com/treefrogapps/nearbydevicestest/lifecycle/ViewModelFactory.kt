package com.treefrogapps.nearbydevicestest.lifecycle

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import com.treefrogapps.nearbydevicestest.di.ApplicationScope
import javax.inject.Inject
import javax.inject.Provider


@ApplicationScope class ViewModelFactory
@Inject constructor(private val viewModels: Map<Class<out ViewModel>, @JvmSuppressWildcards Provider<ViewModel>>) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T =
            viewModels[modelClass]?.get() as T
                    ?: throw IllegalArgumentException("unknown model class $modelClass")
}