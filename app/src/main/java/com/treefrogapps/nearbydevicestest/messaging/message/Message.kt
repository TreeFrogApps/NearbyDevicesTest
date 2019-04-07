package com.treefrogapps.nearbydevicestest.messaging.message


data class Message(val isLocal : Boolean,
                   val fromEndpoint : String,
                   val toEndpoint: String,
                   val content : String,
                   val timestamp: Long = 0)
