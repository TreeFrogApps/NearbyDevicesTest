package com.treefrogapps.nearbydevicestest.nearby

import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import io.reactivex.Completable
import io.reactivex.Single
import javax.inject.Inject


class TaskDelegate @Inject constructor() {

    fun <T> toBlockingResult(task: Task<T>): Boolean {
        await(task)
        return task.isSuccessful
    }

    fun <T> toBlocking(task: Task<T>): Task<T> {
        await(task)
        return task
    }

    fun <T> toCompletable(task: Task<T>): Completable {
        await(task)
        return if (task.isSuccessful) {
            Completable.complete()
        } else {
            Completable.error(task.exception ?: Exception("An error occurred with task $task"))
        }
    }

    fun <T> toSingle(task: Task<T>): Single<Boolean> {
        await(task)
        return if (task.isSuccessful) Single.just(true)
        else Single.error(task.exception ?: IllegalStateException())
    }

    private fun <T> await(task: Task<T>) {
        Tasks.await(task)
    }
}