package com.gmpire.guruklub.sync

import androidx.work.*
import java.util.concurrent.TimeUnit

object GameWorkRequest {

    fun getGameHeartWorkRequest(): WorkRequest {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        return PeriodicWorkRequestBuilder<GameHeartUpdateWorker>(
            15,
            TimeUnit.MINUTES
        ).addTag("Game Worker").setConstraints(constraints).build()
    }

    fun startOneTimeWorking(workerClass: Class<out Worker>): WorkRequest {
        val oneTimeWorkRequestBuilder = OneTimeWorkRequest.Builder(workerClass)

        return oneTimeWorkRequestBuilder.build()
    }

}