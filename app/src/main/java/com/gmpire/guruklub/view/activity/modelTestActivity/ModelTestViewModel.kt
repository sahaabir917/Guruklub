package com.gmpire.guruklub.view.activity.modelTestActivity

import androidx.lifecycle.LifecycleOwner
import com.gmpire.guruklub.data.DataManager
import com.gmpire.guruklub.view.base.BaseViewModel
import sslwireless.android.easy.loyal.merchant.viewmodel.util.ApiCallbackHelper
import javax.inject.Inject

class ModelTestViewModel @Inject constructor(dataManager: DataManager) : BaseViewModel() {

    val dataManager = dataManager

    fun apiGetTime(
        lifecycleOwner: LifecycleOwner
    ) {
        dataManager.apiHelper.apiGetTime(
            ApiCallbackHelper(livedata(lifecycleOwner, "apiGetTime"))
        )
    }

    fun apiModelTestParticipation(
        modelTestId: String,
        userId: String,
        lifecycleOwner: LifecycleOwner
    ) {
        dataManager.apiHelper.apiModelTestParticipation(
            modelTestId, userId,
            ApiCallbackHelper(livedata(lifecycleOwner, "apiModelTestParticipation")))
    }

    fun apiModelTestRegister(
        user_id: String,
        model_test_id: String,
        exam_status : Boolean,
        lifecycleOwner: LifecycleOwner
    ) {
        dataManager.apiHelper.apiModelTestRegister(
            user_id,
            model_test_id,
            exam_status.toString(),
            ApiCallbackHelper(livedata(lifecycleOwner, "apiModelTestRegister"))
        )
    }

}