package com.treefrogapps.nearbydevicestest.util


class KMap<out T>(private val input : T) {

    fun <R> map(function: (T) -> R): KMap<R> = KMap(function(input))

    fun <R> flatMap(function: (T) -> KMap<R>): KMap<R> = function(input)

    fun get() : T = input
}