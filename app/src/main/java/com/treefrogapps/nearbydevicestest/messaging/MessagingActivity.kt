package com.treefrogapps.nearbydevicestest.messaging

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.content.pm.PackageManager.PERMISSION_DENIED
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat.checkSelfPermission
import android.support.v7.app.AppCompatActivity
import com.treefrogapps.nearbydevicestest.R
import com.treefrogapps.nearbydevicestest.messaging.message.MessagesFragment
import com.treefrogapps.nearbydevicestest.util.createFragment
import dagger.android.AndroidInjection
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import javax.inject.Inject

class MessagingActivity : AppCompatActivity(), HasSupportFragmentInjector {

    @Inject lateinit var injector: DispatchingAndroidInjector<Fragment>

    companion object {
        private const val PERMISSION_REQUEST_CODE = 4096
        private const val PERMISSION = ACCESS_COARSE_LOCATION
    }

    override fun supportFragmentInjector(): AndroidInjector<Fragment> = injector

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidInjection.inject(this)
        setContentView(R.layout.activity_main)

        if(savedInstanceState == null){
            createFragment<MessagesFragment>(null)?.let {
                supportFragmentManager.beginTransaction()
                        .add(R.id.container, it)
                        .commit()
            }
        }
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
