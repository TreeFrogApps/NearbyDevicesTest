package com.treefrogapps.nearbydevicestest.messaging.message


sealed class MessagesEvent {

    class IncomingMessage(val message : Message)
}