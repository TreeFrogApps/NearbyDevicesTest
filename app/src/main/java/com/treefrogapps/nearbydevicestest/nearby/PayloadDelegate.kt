package com.treefrogapps.nearbydevicestest.nearby

import com.google.android.gms.nearby.connection.Payload
import javax.inject.Inject


class PayloadDelegate @Inject constructor() {

    fun fromBytes(bytes : ByteArray) : Payload = Payload.fromBytes(bytes)
}