package com.treefrogapps.nearbydevicestest.nearby


class ConnectionException : Exception {

    constructor() : super("Connection Error")
    constructor(message: String) : super(message)
    constructor(e : Throwable) : super(e)
}