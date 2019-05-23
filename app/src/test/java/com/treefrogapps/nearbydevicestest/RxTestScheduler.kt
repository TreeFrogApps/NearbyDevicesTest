package com.treefrogapps.nearbydevicestest

import com.treefrogapps.nearbydevicestest.rx.SchedulerSupplier
import io.reactivex.Scheduler
import io.reactivex.schedulers.Schedulers


class RxTestScheduler(private val scheduler: Scheduler = Schedulers.trampoline()) : SchedulerSupplier {

    override fun io(): Scheduler = scheduler
    override fun computation(): Scheduler = scheduler
    override fun main(): Scheduler = scheduler
    override fun new(): Scheduler = scheduler
}