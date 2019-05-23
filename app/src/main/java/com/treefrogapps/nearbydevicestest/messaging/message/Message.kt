package com.treefrogapps.nearbydevicestest.messaging.message


data class Message(val fromUser : String,
                   val toUser: String,
                   val toEndpointId : String,
                   val content : String,
                   val timestamp: Long = 0)
