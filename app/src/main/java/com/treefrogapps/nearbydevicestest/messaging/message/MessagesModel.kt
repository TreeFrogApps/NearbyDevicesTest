package com.treefrogapps.nearbydevicestest.messaging.message

import android.os.SystemClock
import com.google.gson.Gson
import com.google.gson.JsonIOException
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import com.treefrogapps.nearbydevicestest.User
import com.treefrogapps.nearbydevicestest.app.BaseObservableModel
import com.treefrogapps.nearbydevicestest.di.ApplicationScope
import com.treefrogapps.nearbydevicestest.nearby.ConnectionManager
import com.treefrogapps.nearbydevicestest.rx.SchedulerSupplier
import io.reactivex.Flowable
import io.reactivex.rxkotlin.plusAssign
import timber.log.Timber
import java.nio.charset.Charset
import java.util.concurrent.TimeUnit
import java.util.function.Supplier
import javax.inject.Inject


@ApplicationScope class MessagesModel
@Inject constructor(private val manager: ConnectionManager,
                    private val gson: Gson,
                    private val schedulers: SchedulerSupplier,
                    @User private val username : Supplier<String>) : BaseObservableModel<MessagesEvent>() {

    override fun onCleared() {
        super.onCleared()
        manager.disconnectAllEndpoints()
    }

    fun listenToConnectedDevice(){
        disposables += manager.activeConnections()
                .filter { it.isNotEmpty() }
                .map { it.first() }
                .switchMap { device -> Flowable.interval(3L, TimeUnit.SECONDS, schedulers.computation())
                        .map { Message(fromUser = username.get(),
                                       toUser = device.username,
                                       toEndpointId = device.endpointId,
                                       content = "Message : $it",
                                       timestamp = SystemClock.currentThreadTimeMillis()) }
                }.subscribe(this::sendMessage, {Timber.e(it)})

        disposables += listenToMessages().subscribe { Timber.i("Mark : $it") }
    }

    fun sendMessage(message: Message) {
        manager.sendMessage(message.toEndpointId, message.let { gson.toBytes(it) })
    }

    fun listenToMessages() : Flowable<Message> = manager.payloadData()
            .doOnNext { Timber.i("Mark : message from ${it.first}")}
            .map { it.second }
            .filter { it.asBytes() != null }
            .map { gson.fromBytes<Message>(it.asBytes()!!) }
            .observeOn(schedulers.main())

    @Throws(JsonIOException::class)
    private inline fun <reified T> Gson.toBytes(t: T): ByteArray {
        return toJson(t, object : TypeToken<T>() {}.type).toByteArray(Charset.defaultCharset())
    }

    @Throws(JsonSyntaxException::class)
    private inline fun <reified T> Gson.fromBytes(input: ByteArray): T {
        return fromJson(String(input, Charset.defaultCharset()), T::class.java)
    }
}