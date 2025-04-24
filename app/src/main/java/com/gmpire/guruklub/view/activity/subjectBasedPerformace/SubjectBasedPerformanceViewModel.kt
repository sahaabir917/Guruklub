package com.gmpire.guruklub.view.activity.subjectBasedPerformace

import androidx.lifecycle.LifecycleOwner
import com.gmpire.guruklub.data.DataManager
import com.gmpire.guruklub.view.base.BaseViewModel
import sslwireless.android.easy.loyal.merchant.viewmodel.util.ApiCallbackHelper
import javax.inject.Inject

class SubjectBasedPerformanceViewModel @Inject constructor(dataManager: DataManager) : BaseViewModel() {

    val dataManager = dataManager

    fun apiSubjectPerformance(gameId: String, lifecycleOwner: LifecycleOwner) {
        dataManager.apiHelper.apiSubjectPerformance(gameId, ApiCallbackHelper(livedata(lifecycleOwner,"apiSubjectPerformance")))
    }

}