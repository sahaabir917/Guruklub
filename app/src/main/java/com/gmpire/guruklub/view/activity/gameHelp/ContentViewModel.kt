package com.gmpire.guruklub.view.activity.gameHelp

import androidx.lifecycle.LifecycleOwner
import com.gmpire.guruklub.data.DataManager
import com.gmpire.guruklub.view.base.BaseViewModel
import sslwireless.android.easy.loyal.merchant.viewmodel.util.ApiCallbackHelper
import javax.inject.Inject

class ContentViewModel @Inject constructor(dataManager: DataManager) : BaseViewModel() {

    val dataManager = dataManager

    fun apiGetContent(slug:String,lifecycleOwner: LifecycleOwner) {
        dataManager.apiHelper.apiGetContent(slug,ApiCallbackHelper(livedata(lifecycleOwner,"apiGetContent")))
    }


    fun apiGetFindUs(lifecycleOwner: LifecycleOwner) {
        dataManager.apiHelper.apiGetFindUs(ApiCallbackHelper(livedata(lifecycleOwner,"apiGetFindUs")))
    }


    fun apiContactUs(email: String,message:String,lifecycleOwner: LifecycleOwner) {
        dataManager.apiHelper.apiContactUs(email,message,ApiCallbackHelper(livedata(lifecycleOwner,"apiContactUs")))
    }

    fun apiInviteFriends(email: String,emails:String,lifecycleOwner: LifecycleOwner) {
        dataManager.apiHelper.apiInviteFriends(email,emails,ApiCallbackHelper(livedata(lifecycleOwner,"apiInviteFriends")))
    }




}