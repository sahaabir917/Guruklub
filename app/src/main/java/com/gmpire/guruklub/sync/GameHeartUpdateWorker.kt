package com.gmpire.guruklub.sync

import android.content.Context
import android.util.Log
import androidx.work.ListenableWorker
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.gmpire.guruklub.data.DataManager
import com.gmpire.guruklub.data.local_db.dto.GameHeartDTO

import javax.inject.Inject
import javax.inject.Provider

class GameHeartUpdateWorker @Inject constructor(
    var dataManager: DataManager,
    context: Context,
    workerParameters: WorkerParameters
) :
    Worker(context, workerParameters) {

    override fun doWork(): Result {
        var gameHearts = getAllHeartsRow()
        Log.d("WorkManager:", "Hearts: ${gameHearts.size}")

        gameHearts.forEach {
            try {
                val hashMap = HashMap<String, String>()
                hashMap["heart_variable"] = it.heart_type.toString()
                if(it.practice != null)
                    hashMap["practice"] = it.practice.toString()
                val response =
                    dataManager.apiHelper.apiService
                        .postRequestSync("hearts-variation", hashMap)
                        .execute()
                if (response.isSuccessful && response.code() == 200) {
                    dataManager.roomHelper.getDatabase().gameHeartDao().delete(it)
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
                Log.d("WorkManager:", "Exception: ${ex.localizedMessage}")
            }
        }
        return Result.success()
    }

    private fun getAllHeartsRow(): List<GameHeartDTO> {
        val userId: String = dataManager.mPref.prefGetUserInfo().id
        var id = 0
        id = if (userId.isNullOrEmpty()) 0 else userId.toInt()

        return dataManager.roomHelper.getDatabase().gameHeartDao().getAll(id)
    }

    class Factory @Inject constructor(
        val dataManager: Provider<DataManager>
    ) : ChildWorkerFactory {

        override fun create(appContext: Context, params: WorkerParameters): ListenableWorker {
            return GameHeartUpdateWorker(dataManager.get(), appContext, params)
        }
    }

}