package com.treefrogapps.nearbydevicestest

import com.treefrogapps.nearbydevicestest.rx.SchedulerSupplier
import io.reactivex.Scheduler


class RxTestScheduler(private val scheduler: Scheduler) : SchedulerSupplier {

    override fun io(): Scheduler = scheduler
    override fun computation(): Scheduler = scheduler
    override fun main(): Scheduler = scheduler
    override fun new(): Scheduler = scheduler
}