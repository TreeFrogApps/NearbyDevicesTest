package com.treefrogapps.nearbydevicestest.nearby

import com.google.android.gms.nearby.connection.ConnectionsClient
import com.google.android.gms.nearby.connection.Payload
import com.google.android.gms.nearby.connection.Payload.Type.*
import com.google.android.gms.nearby.connection.PayloadCallback
import com.google.android.gms.nearby.connection.PayloadTransferUpdate
import com.google.android.gms.nearby.connection.PayloadTransferUpdate.Status.*
import com.treefrogapps.nearbydevicestest.di.ApplicationScope
import com.treefrogapps.nearbydevicestest.nearby.ConnectionType.PAYLOAD
import io.reactivex.Flowable
import io.reactivex.processors.PublishProcessor
import timber.log.Timber
import javax.inject.Inject

/**
 * [Connection] implementation wrapper class around [PayloadCallback] for incoming [Payload]s from remote device.
 * Sent from other device using [ConnectionsClient.sendPayload]
 */
@ApplicationScope class PayloadConnection
@Inject constructor(@NearbyConnection(PAYLOAD) private val payloadSubject: PublishProcessor<Pair<String, Payload>>,
                    @NearbyConnection private val endpointId: String,
                    @NearbyConnection private val errorProcessor: PublishProcessor<ConnectionError>)
    : Connection<PayloadCallback, Unit, Pair<@JvmSuppressWildcards String, @JvmSuppressWildcards Payload>> {

    private val callback = object : PayloadCallback() {

        /**
         * Called when a [Payload] is received from a remote endpoint.
         *
         * @param payload [Payload] A Payload sent between devices. Payloads sent as a particular type will
         * be received as that same type on the other device, e.g. the data for a Payload of type [Payload.Type.STREAM]
         * must be received by reading from the [java.io.InputStream] returned by [Payload.asStream]
         */
        override fun onPayloadReceived(endpointId: String, payload: Payload) {
            Timber.d("PayloadReceived from endpoint %s : %s", endpointId, payload.info())
            if (this@PayloadConnection.endpointId == endpointId) {
                payloadSubject.onNext(Pair(endpointId, payload))
            }
        }

        /**
         * Called with progress information about an active Payload transfer, either incoming or outgoing.
         *
         * @param endpointId endpoint id
         * @param transferUpdate [PayloadTransferUpdate] Describes the status for an active Payload transfer,
         * either incoming or outgoing.
         */
        override fun onPayloadTransferUpdate(endpointId: String, transferUpdate: PayloadTransferUpdate) {
            Timber.d("PayloadTransferUpdate from endpoint %s : %s", endpointId, transferUpdate.info())
        }
    }

    override fun callback(): PayloadCallback = callback

    override fun options() {
        throw IllegalStateException("No options for Payload")
    }

    override fun observe(): Flowable<Pair<String, Payload>> = payloadSubject

    override fun observeErrors(): Flowable<ConnectionError> = errorProcessor

    private fun Payload.info(): String =
            "[ Payload : id : $id, type : ${typeAsString(type)} ]"

    private fun PayloadTransferUpdate.info(): String =
            "[ PayloadTransferUpdate : id $payloadId, status : ${statusAsString(status)}, transferred $bytesTransferred of $totalBytes bytes ]"

    private fun statusAsString(status: Int): String = when (status) {
        CANCELED    -> "Canceled"
        FAILURE     -> "Failed"
        IN_PROGRESS -> "In Progress"
        SUCCESS     -> "Success"
        else        -> "Unknown"
    }

    private fun typeAsString(type: Int): String = when (type) {
        BYTES  -> "Bytes"
        FILE   -> "File"
        STREAM -> "Stream"
        else   -> "Unknown"
    }
}