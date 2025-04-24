package com.gmpire.guruklub.view.activity.Registration

import androidx.lifecycle.LifecycleOwner
import com.gmpire.guruklub.data.DataManager
import com.gmpire.guruklub.view.base.BaseViewModel
import sslwireless.android.easy.loyal.merchant.viewmodel.util.ApiCallbackHelper
import javax.inject.Inject

class RegistrationViewModel @Inject constructor(dataManager: DataManager) : BaseViewModel() {


    val dataManager = dataManager

    fun apiRegistrationRequest(email: String, password: String,confirm_password:String,toc:Int, lifecycleOwner: LifecycleOwner) {
        dataManager.apiHelper.apiRegistrationRequest(email, password,confirm_password,toc, ApiCallbackHelper(livedata(lifecycleOwner,"apiRegistrationRequest")))
    }



}