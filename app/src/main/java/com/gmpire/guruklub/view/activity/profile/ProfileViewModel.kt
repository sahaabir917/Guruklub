package com.gmpire.guruklub.view.activity.profile

import androidx.lifecycle.LifecycleOwner
import com.gmpire.guruklub.data.DataManager
import com.gmpire.guruklub.util.IDatabaseCallBack
import com.gmpire.guruklub.view.base.BaseViewModel
import sslwireless.android.easy.loyal.merchant.viewmodel.util.ApiCallbackHelper
import javax.inject.Inject

class ProfileViewModel @Inject constructor(dataManager: DataManager) : BaseViewModel() {


    val dataManager = dataManager
    var iDatabaseCallBack: IDatabaseCallBack? = null

    fun fetchPerformanceHistory(lifecycleOwner: LifecycleOwner) {
        dataManager.apiHelper.apiPerformanceHistory(
            ApiCallbackHelper(livedata(lifecycleOwner, "fetchPerformanceHistory"))
        )
    }

    fun fetchPerformanceSummery(lifecycleOwner: LifecycleOwner) {
        dataManager.apiHelper.apiPerformanceSummery(
            ApiCallbackHelper(livedata(lifecycleOwner, "fetchPerformanceSummery"))
        )
    }

    fun fetchGetGameChallenges(
        lifecycleOwner: LifecycleOwner
    ) {
        dataManager.apiHelper.apiGetGameChallenges(
            "profile",
            ApiCallbackHelper(livedata(lifecycleOwner, "fetchGetGameChallenges"))
        )
    }

    fun apiGetPlayedModelTestItems(
        category_id: String,
        lifecycleOwner: LifecycleOwner
    ) {
        dataManager.apiHelper.apiGetPlayedModelTestItems(
            category_id,
            ApiCallbackHelper(livedata(lifecycleOwner, "apiGetPlayedModelTestItems"))
        )
    }

    fun apiGetLeaderboard(
        model_test_id: String,
        page: String,
        lifecycleOwner: LifecycleOwner
    ) {
        dataManager.apiHelper.apiGetLeaderboard(
            model_test_id,
            page,
            ApiCallbackHelper(livedata(lifecycleOwner, "apiGetLeaderboard"))
        )
    }

    fun apiGetOverallPerformance(
        user_id: Long,
        lifecycleOwner: LifecycleOwner
    ) {
        dataManager.apiHelper.apiGetOverallPerformance(
            user_id,
            ApiCallbackHelper(livedata(lifecycleOwner, "apiGetOverallPerformance"))
        )
    }

    fun getAllGameHeartData() {
        val userId = dataManager.mPref.prefGetUserInfo().id.toInt()
        dataManager.databaseHelper.executeDBOperation(
            dataManager.roomHelper.getDatabase().gameHeartDao().getAll(userId),
            "getAllGameHeartData",
            iDatabaseCallBack
        )
    }


}