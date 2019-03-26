package com.treefrogapps.nearbydevicestest.nearby

import com.google.android.gms.nearby.connection.Payload
import com.google.android.gms.nearby.connection.PayloadCallback
import com.google.android.gms.nearby.connection.PayloadTransferUpdate
import com.treefrogapps.nearbydevicestest.di.ApplicationScope
import com.treefrogapps.nearbydevicestest.nearby.ConnectionType.PAYLOAD
import io.reactivex.Flowable
import io.reactivex.processors.PublishProcessor
import javax.inject.Inject


@ApplicationScope class PayloadConnection
@Inject constructor(@Connection(PAYLOAD) private val payloadSubject: PublishProcessor<Pair<String, Payload>>)
    : ObservableConnection<PayloadCallback, Unit, Flowable<Pair<@JvmSuppressWildcards String, @JvmSuppressWildcards Payload>>> {

    private val callback = object : PayloadCallback() {
        override fun onPayloadReceived(p0: String, p1: Payload) {
            payloadSubject.onNext(Pair(p0, p1))
        }

        override fun onPayloadTransferUpdate(p0: String, p1: PayloadTransferUpdate) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }
    }

    override fun callback(): PayloadCallback = callback

    override fun options() {
        throw IllegalStateException("No options for Payload")
    }

    override fun observe(): Flowable<Pair<String, Payload>> = payloadSubject
}