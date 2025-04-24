package com.gmpire.guruklub.view.activity.resetPassword

import androidx.lifecycle.LifecycleOwner
import com.gmpire.guruklub.data.DataManager
import com.gmpire.guruklub.view.base.BaseViewModel
import sslwireless.android.easy.loyal.merchant.viewmodel.util.ApiCallbackHelper
import javax.inject.Inject

class ResetPasswordViewModel @Inject constructor(dataManager: DataManager) : BaseViewModel() {

    val dataManager = dataManager

    fun apiChangePassword(email: String,password:String,confirm_password:String, lifecycleOwner: LifecycleOwner) {
        dataManager.apiHelper.apiChangePassword(email,password,confirm_password, ApiCallbackHelper(livedata(lifecycleOwner,"apiChangePassword")))
    }

}