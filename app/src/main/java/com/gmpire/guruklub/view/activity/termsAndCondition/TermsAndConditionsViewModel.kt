package com.gmpire.guruklub.view.activity.termsAndCondition

import androidx.lifecycle.LifecycleOwner
import com.gmpire.guruklub.data.DataManager
import com.gmpire.guruklub.view.base.BaseViewModel
import sslwireless.android.easy.loyal.merchant.viewmodel.util.ApiCallbackHelper
import javax.inject.Inject

class TermsAndConditionsViewModel @Inject constructor(dataManager: DataManager) : BaseViewModel() {

    val dataManager = dataManager

    fun apiGetTermsAndCondition(lifecycleOwner: LifecycleOwner) {
        dataManager.apiHelper.apiGetTermsAndCondition(ApiCallbackHelper(livedata(lifecycleOwner,"apiGetTermsAndCondition")))
    }



}