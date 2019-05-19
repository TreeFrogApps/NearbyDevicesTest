package com.treefrogapps.nearbydevicestest.messaging.message

data class MessagesViewDataModel(val endpointUser: String = "",
                                 val messages: List<Message> = listOf(),
                                 val currentMessage: String = "")