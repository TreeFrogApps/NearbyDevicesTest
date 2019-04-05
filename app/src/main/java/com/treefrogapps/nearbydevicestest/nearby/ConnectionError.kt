package com.treefrogapps.nearbydevicestest.nearby


sealed class ConnectionError {

    object Unknown : ConnectionError()
}