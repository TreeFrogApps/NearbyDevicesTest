package com.treefrogapps.nearbydevicestest.messaging

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.content.pm.PackageManager.PERMISSION_DENIED
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Bundle
import android.support.v4.content.ContextCompat.checkSelfPermission
import android.support.v7.app.AppCompatActivity
import com.treefrogapps.nearbydevicestest.R
import com.treefrogapps.nearbydevicestest.nearby.ConnectionManager
import dagger.android.AndroidInjection
import javax.inject.Inject

class MessagingActivity : AppCompatActivity() {

    companion object {
        private const val PERMISSION_REQUEST_CODE = 4096
        private const val PERMISSION = ACCESS_COARSE_LOCATION
    }

    @Inject lateinit var connectionManager: ConnectionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onStart() {
        super.onStart()
        if (checkSelfPermission(this, PERMISSION) == PERMISSION_DENIED) {
            requestPermissions(arrayOf(PERMISSION), PERMISSION_REQUEST_CODE)
        } else {

        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if(requestCode == PERMISSION_REQUEST_CODE
            && grantResults.all { it == PERMISSION_GRANTED }) {

        } else finish()
    }
}
