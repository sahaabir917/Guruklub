package com.gmpire.guruklub.view.activity.login

import androidx.lifecycle.LifecycleOwner
import com.gmpire.guruklub.data.DataManager
import com.gmpire.guruklub.view.base.BaseViewModel
import com.google.firebase.auth.FirebaseUser
import sslwireless.android.easy.loyal.merchant.viewmodel.util.ApiCallbackHelper
import javax.inject.Inject

class LoginViewModel @Inject constructor(dataManager: DataManager) : BaseViewModel() {


    val dataManager = dataManager

    fun apiLoginRequest(email: String, password: String, lifecycleOwner: LifecycleOwner) {
        dataManager.apiHelper.apiLoginRequest(email, password, ApiCallbackHelper(livedata(lifecycleOwner,"apiLoginRequest")))
    }

    fun fetchFbLogin(currentUser: FirebaseUser, lifecycleOwner: LifecycleOwner) {
        dataManager.apiHelper.apiFbLogin(
            currentUser,
            ApiCallbackHelper(livedata(lifecycleOwner, "fetchFbLogin"))
        )
    }

    fun fetchGoogleLogin(currentUser: FirebaseUser, lifecycleOwner: LifecycleOwner) {
        dataManager.apiHelper.apiGoogleLogin(
            currentUser,
            ApiCallbackHelper(livedata(lifecycleOwner, "fetchGoogleLogin"))
        )
    }

    fun fetchDeviceFcmToken(
        device_id: String,
        model: String,
        manufacture: String,
        version: String?,
        deviceUniqueId : String,
        fcm_token: String,
        lifecycleOwner: LifecycleOwner
    ) {
        dataManager.apiHelper.apiDeviceFcmToken(
            device_id,
            model,
            manufacture,
            version,
            deviceUniqueId,
            fcm_token,
            ApiCallbackHelper(livedata(lifecycleOwner, "fetchDeviceFcmToken"))
        )
    }

    fun userVersionCheck(
        app_release: String,
        lifecycleOwner: LifecycleOwner
    ){
        dataManager.apiHelper.apiGetCheckUpdateVersion(
            app_release,
            ApiCallbackHelper(livedata(lifecycleOwner, "userVersionCheck"))
        )
    }



}