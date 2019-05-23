package com.treefrogapps.nearbydevicestest.messaging

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.content.pm.PackageManager.PERMISSION_DENIED
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat.checkSelfPermission
import com.treefrogapps.nearbydevicestest.R
import com.treefrogapps.nearbydevicestest.app.BaseInjectionActivity
import com.treefrogapps.nearbydevicestest.app.createFragment
import com.treefrogapps.nearbydevicestest.messaging.start.StartFragment

class MessagingActivity : BaseInjectionActivity(), FragmentTransactionListener {

    companion object {
        private const val PERMISSION_REQUEST_CODE = R.id.permission_request_code
        private const val PERMISSION = ACCESS_COARSE_LOCATION
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState == null) {
            createFragment<StartFragment>(null)?.let { onAddTransaction(it) }
        }
    }

    override fun layout(): Int = R.layout.activity_main

    override fun onStart() {
        super.onStart()
        if (checkSelfPermission(this, PERMISSION) == PERMISSION_DENIED) {
            requestPermissions(arrayOf(PERMISSION), PERMISSION_REQUEST_CODE)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.any { it == PERMISSION_DENIED }) finish()
    }

    override fun onReplaceTransaction(fragment: Fragment?) {
        fragment?.let {
            supportFragmentManager.beginTransaction()
                    .setCustomAnimations(
                            android.R.animator.fade_in,
                            android.R.animator.fade_out,
                            android.R.animator.fade_in,
                            android.R.animator.fade_out)
                    .replace(R.id.container, it)
                    .addToBackStack(null)
                    .commit()
        }
    }

    override fun onAddTransaction(fragment: Fragment?) {
        fragment?.let {
            supportFragmentManager.beginTransaction()
                    .add(R.id.container, it)
                    .commit()
        }
    }
}
