package com.treefrogapps.nearbydevicestest.messaging.message

import com.google.gson.Gson
import com.google.gson.JsonIOException
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import com.treefrogapps.nearbydevicestest.app.BaseObservableModel
import com.treefrogapps.nearbydevicestest.di.ApplicationScope
import com.treefrogapps.nearbydevicestest.nearby.ConnectionManager
import java.nio.charset.Charset
import javax.inject.Inject


@ApplicationScope class MessagesModel
@Inject constructor(private val connectionManager: ConnectionManager,
                    private val gson: Gson) : BaseObservableModel<MessagesEvent>() {


    fun sendMessage(message: Message) {
        connectionManager.sendMessage(message.toEndpoint, message.let { gson.toBytes(it) })
    }

    @Throws(JsonIOException::class)
    private inline fun <reified T> Gson.toBytes(t: T): ByteArray {
        return toJson(t, object : TypeToken<T>() {}.type).toByteArray(Charset.defaultCharset())
    }

    @Throws(JsonSyntaxException::class)
    private inline fun <reified T> Gson.fromBytes(input: ByteArray): T {
        return fromJson(String(input, Charset.defaultCharset()), T::class.java)
    }
}