package com.gmpire.guruklub.view.activity.emailVerification

import androidx.lifecycle.LifecycleOwner
import com.gmpire.guruklub.data.DataManager
import com.gmpire.guruklub.view.base.BaseViewModel
import sslwireless.android.easy.loyal.merchant.viewmodel.util.ApiCallbackHelper
import javax.inject.Inject

class EmailVerificationViewModel @Inject constructor(dataManager: DataManager) : BaseViewModel() {

    val dataManager = dataManager

    fun apiEmailVerification(email: String, code: String, lifecycleOwner: LifecycleOwner) {
        dataManager.apiHelper.apiEmailVerification(email, code, ApiCallbackHelper(livedata(lifecycleOwner,"apiEmailVerification")))
    }


    fun apiEmailVerificationResend(email: String, lifecycleOwner: LifecycleOwner) {
        dataManager.apiHelper.apiEmailVerificationResend(email, ApiCallbackHelper(livedata(lifecycleOwner,"apiEmailVerificationResend")))
    }


    fun apiForgetPasswordCodeVerify(email: String,code:String, lifecycleOwner: LifecycleOwner) {
        dataManager.apiHelper.apiForgetPasswordCodeVerify(email,code, ApiCallbackHelper(livedata(lifecycleOwner,"apiForgetPasswordCodeVerify")))
    }


    fun apiForgetPassword(email: String, lifecycleOwner: LifecycleOwner) {
        dataManager.apiHelper.apiForgetPassword(email, ApiCallbackHelper(livedata(lifecycleOwner,"apiForgetPassword")))
    }


}