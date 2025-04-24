package com.gmpire.guruklub.view.activity.gamesubscribe

import androidx.lifecycle.LifecycleOwner
import com.gmpire.guruklub.data.DataManager
import com.gmpire.guruklub.view.base.BaseViewModel
import sslwireless.android.easy.loyal.merchant.viewmodel.util.ApiCallbackHelper
import javax.inject.Inject

class SubscribeActivityViewModel @Inject constructor(dataManager: DataManager) : BaseViewModel() {
    val dataManager = dataManager

    fun getLimitedHeartPackage(lifecycleOwner: LifecycleOwner) {
        dataManager.apiHelper.getLimitedHeartPackage(
                ApiCallbackHelper(livedata(lifecycleOwner, "getLimitedHeartPackage"))
        )
    }

    fun getUnlimitedHeartPackage(lifecycleOwner: LifecycleOwner) {
        dataManager.apiHelper.getUnlimitedHeartPackage(
                ApiCallbackHelper(livedata(lifecycleOwner, "getUnlimitedHeartPackage"))
        )
    }

    fun getHeartAddPayment(lifeCycleOwner: LifecycleOwner, packageId: String) {
        dataManager.apiHelper.getHeartAddPayment(packageId, ApiCallbackHelper(livedata(lifeCycleOwner, "getHeartAddPayment")))
    }

    fun getPaymentExecution(lifeCycleOwner: LifecycleOwner, status: String?, tranDate: String?, tranId: String?, valId: String?, amount: String?, storeAmount: String?, bankTranId: String?, cardType: String?, cardNo: String?) {
        dataManager.apiHelper.getPaymentExecution(status ?: "", tranDate ?: "", tranId ?: "", valId
                ?: "", amount ?: "", storeAmount ?: "", bankTranId ?: "", cardType ?: "", cardNo
                ?: "", ApiCallbackHelper(livedata(lifeCycleOwner, "getPaymentExecution")))
    }

    fun getFailedTransaction(status: String, errorMsg : String, tnxId: String, lifeCycleOwner: LifecycleOwner) {
        dataManager.apiHelper.getFailedTransaction(status, errorMsg, tnxId,ApiCallbackHelper(livedata(lifeCycleOwner,"getFailedTransaction")))
    }

}