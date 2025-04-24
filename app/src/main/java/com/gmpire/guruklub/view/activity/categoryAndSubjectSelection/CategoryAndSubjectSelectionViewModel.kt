package com.gmpire.guruklub.view.activity.categoryAndSubjectSelection

import androidx.lifecycle.LifecycleOwner
import com.gmpire.guruklub.data.DataManager
import com.gmpire.guruklub.view.base.BaseViewModel
import sslwireless.android.easy.loyal.merchant.viewmodel.util.ApiCallbackHelper
import javax.inject.Inject

class CategoryAndSubjectSelectionViewModel @Inject constructor(dataManager: DataManager) : BaseViewModel() {

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

}