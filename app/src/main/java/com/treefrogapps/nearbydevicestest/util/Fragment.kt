package com.treefrogapps.nearbydevicestest.util

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.Fragment.InstantiationException
import timber.log.Timber


inline fun <reified T : Fragment> createFragment(extras : Bundle?) : Fragment? {
   return of(T::class.java)?.apply { arguments = extras }
}


fun <T : Fragment> of(t : Class<T>) : T? {
    val ex : Exception?
    try {
        return t.newInstance()
    } catch (e : InstantiationException) {
        ex = e
    } catch (e : IllegalAccessException) {
        ex = e
    }
    ex?.let { Timber.e(it) }
    return null
}