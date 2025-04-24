package com.gmpire.guruklub.view.activity.forgetPassword

import androidx.lifecycle.LifecycleOwner
import com.gmpire.guruklub.data.DataManager
import com.gmpire.guruklub.view.base.BaseViewModel
import sslwireless.android.easy.loyal.merchant.viewmodel.util.ApiCallbackHelper
import javax.inject.Inject

class ForgetPasswordViewModel @Inject constructor(dataManager: DataManager) : BaseViewModel() {

    val dataManager = dataManager

    fun apiForgetPassword(email: String, lifecycleOwner: LifecycleOwner) {
        dataManager.apiHelper.apiForgetPassword(email, ApiCallbackHelper(livedata(lifecycleOwner,"apiForgetPassword")))
    }

}