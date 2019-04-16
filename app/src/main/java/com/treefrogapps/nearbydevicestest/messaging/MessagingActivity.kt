package com.treefrogapps.nearbydevicestest.messaging

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.content.pm.PackageManager.PERMISSION_DENIED
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat.checkSelfPermission
import android.support.v7.app.AppCompatActivity
import com.treefrogapps.nearbydevicestest.R
import com.treefrogapps.nearbydevicestest.app.createFragment
import com.treefrogapps.nearbydevicestest.messaging.start.StartFragment
import com.treefrogapps.nearbydevicestest.rx.dispose
import dagger.android.AndroidInjection
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import io.reactivex.disposables.Disposable
import javax.inject.Inject

class MessagingActivity : AppCompatActivity(), HasSupportFragmentInjector, FragmentTransactionListener {

    @Inject lateinit var injector: DispatchingAndroidInjector<Fragment>

    private var disposable: Disposable? = null

    companion object {
        private const val PERMISSION_REQUEST_CODE = 4096
        private const val PERMISSION = ACCESS_COARSE_LOCATION
    }

    override fun supportFragmentInjector(): AndroidInjector<Fragment> = injector

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (savedInstanceState == null) {
            createFragment<StartFragment>(null)?.let { onAddTransaction(it) }
        }
    }

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

    override fun onDestroy() {
        super.onDestroy()
        dispose(disposable)
    }

    override fun onReplaceTransaction(fragment: Fragment?) {
        fragment?.let {
            supportFragmentManager.beginTransaction()
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
