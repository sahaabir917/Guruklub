package com.gmpire.guruklub.view.activity.main

import androidx.lifecycle.LifecycleOwner
import com.gmpire.guruklub.data.DataManager
import com.gmpire.guruklub.util.IDatabaseCallBack
import com.gmpire.guruklub.view.base.BaseViewModel
import com.google.firebase.auth.FirebaseUser
import sslwireless.android.easy.loyal.merchant.viewmodel.util.ApiCallbackHelper
import javax.inject.Inject

class MainViewModel @Inject constructor(dataManager: DataManager) : BaseViewModel() {
    val dataManager = dataManager
    var iDatabaseCallBack: IDatabaseCallBack? = null

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

    fun fetchPerformanceSummery(lifecycleOwner: LifecycleOwner) {
        dataManager.apiHelper.apiPerformanceSummery(
            ApiCallbackHelper(livedata(lifecycleOwner, "fetchPerformanceSummery"))
        )
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

    fun apiGetFindUs(lifecycleOwner: LifecycleOwner) {
        dataManager.apiHelper.apiGetFindUs(
            ApiCallbackHelper(
                livedata(
                    lifecycleOwner,
                    "apiGetFindUs"
                )
            )
        )
    }

    fun apiContactUs(email: String, message: String, lifecycleOwner: LifecycleOwner) {
        dataManager.apiHelper.apiContactUs(
            email,
            message,
            ApiCallbackHelper(livedata(lifecycleOwner, "apiContactUs"))
        )
    }

    fun apiInviteFriends(email: String, emails: String, lifecycleOwner: LifecycleOwner) {
        dataManager.apiHelper.apiInviteFriends(
            email,
            emails,
            ApiCallbackHelper(livedata(lifecycleOwner, "apiInviteFriends"))
        )
    }

    fun apiSendPromoCode(lifeCycleOwner: LifecycleOwner, promoCode: String, deviceUniqueId: String) {
        dataManager.apiHelper.apiSendPromoCode(
            promoCode,
            deviceUniqueId,
            ApiCallbackHelper(livedata(lifeCycleOwner, "apiSendPromoCode"))
        )
    }

    fun getRefToken(lifecycleOwner: LifecycleOwner) {
        dataManager.apiHelper.getRefToken(
            ApiCallbackHelper(livedata(lifecycleOwner, "getRefToken"))
        )
    }

    fun apiGetNotices(lifecycleOwner: LifecycleOwner) {
        dataManager.apiHelper.apiGetNoticeList(
            ApiCallbackHelper(
                livedata(
                    lifecycleOwner,
                    "apiNoticeBoard"
                )
            )
        )
    }

    fun apiGetUserInfo(lifeCycleOwner: LifecycleOwner) {
        dataManager.apiHelper.apiGetUserInfo(
            ApiCallbackHelper(livedata(lifeCycleOwner, "apiGetUserInfo"))
        )
    }

    fun dataGetUserHearts() {
        val userId = dataManager.mPref.prefGetUserInfo().id
        if(userId.isNotEmpty()) {
            dataManager.databaseHelper.executeDBOperation(
                dataManager.roomHelper.getDatabase().gameHeartDao().getAll(userId.toInt()),
                "dataGetUserHearts",
                iDatabaseCallBack
            )
        }
    }
}