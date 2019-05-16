package com.treefrogapps.nearbydevicestest.rx

import io.reactivex.Scheduler


interface SchedulerSupplier {

    fun io() : Scheduler

    fun computation() : Scheduler

    fun main() : Scheduler

    fun new() : Scheduler
}