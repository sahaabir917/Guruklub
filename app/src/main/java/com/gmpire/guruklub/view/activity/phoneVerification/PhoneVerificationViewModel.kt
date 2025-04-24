package com.gmpire.guruklub.view.activity.phoneVerification

import androidx.lifecycle.LifecycleOwner
import com.gmpire.guruklub.data.DataManager
import com.gmpire.guruklub.view.base.BaseViewModel
import sslwireless.android.easy.loyal.merchant.viewmodel.util.ApiCallbackHelper
import javax.inject.Inject

class PhoneVerificationViewModel @Inject constructor(dataManager: DataManager) : BaseViewModel() {

    val dataManager = dataManager

    fun apiPhoneVerification(phone : String, code: String, lifecycleOwner: LifecycleOwner) {
        dataManager.apiHelper.apiPhoneVerification(phone, code, ApiCallbackHelper(livedata(lifecycleOwner,"apiPhoneVerification")))
    }

    fun apiPhoneVerificationResend(phone: String, lifecycleOwner: LifecycleOwner) {
        dataManager.apiHelper.apiPhoneVerificationResend(phone, ApiCallbackHelper(livedata(lifecycleOwner,"apiPhoneVerificationResend")))
    }



}