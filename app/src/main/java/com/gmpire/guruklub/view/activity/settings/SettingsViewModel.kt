package com.gmpire.guruklub.view.activity.settings

import androidx.lifecycle.LifecycleOwner
import com.gmpire.guruklub.data.DataManager
import com.gmpire.guruklub.view.base.BaseViewModel
import sslwireless.android.easy.loyal.merchant.viewmodel.util.ApiCallbackHelper
import javax.inject.Inject

class SettingsViewModel @Inject constructor(dataManager: DataManager) : BaseViewModel() {

    val dataManager = dataManager

    fun apiGetCategoryList(lifecycleOwner: LifecycleOwner) {
        dataManager.apiHelper.apiGetCategoryList(ApiCallbackHelper(livedata(lifecycleOwner,"apiGetCategoryList")))
    }

    fun apiGetSubjectListByCategory(categoryId: String, lifecycleOwner: LifecycleOwner) {
        dataManager.apiHelper.apiGetSubjectListByCategory(categoryId,"profile", ApiCallbackHelper(livedata(lifecycleOwner,"apiGetSubjectListByCategory")))
    }

    fun apiSetCategoryAndSubject(categoryId: String,subjectIds:String,notification:String, lifecycleOwner: LifecycleOwner) {
        dataManager.apiHelper.apiSetCategoryAndSubject(categoryId,subjectIds,notification,ApiCallbackHelper(livedata(lifecycleOwner,"apiSetCategoryAndSubject")))
    }

    fun apiChangePassword(email: String,password:String,confirm_password:String, lifecycleOwner: LifecycleOwner) {
        dataManager.apiHelper.apiChangePassword(email,password,confirm_password, ApiCallbackHelper(livedata(lifecycleOwner,"apiChangePassword")))
    }

}