package com.treefrogapps.nearbydevicestest.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import dagger.android.AndroidInjection


class ConnectionService : Service() {

    override fun onCreate() {
        AndroidInjection.inject(this)
        super.onCreate()
    }
    override fun onBind(intent: Intent?): IBinder? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}