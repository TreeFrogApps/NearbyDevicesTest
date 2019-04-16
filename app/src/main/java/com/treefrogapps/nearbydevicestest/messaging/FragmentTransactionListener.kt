package com.treefrogapps.nearbydevicestest.messaging

import android.support.v4.app.Fragment


interface FragmentTransactionListener {

    fun onReplaceTransaction(fragment : Fragment?)

    fun onAddTransaction(fragment: Fragment?)
}