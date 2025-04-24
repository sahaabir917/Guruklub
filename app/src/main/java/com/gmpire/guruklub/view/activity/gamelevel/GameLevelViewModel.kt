package com.gmpire.guruklub.view.activity.gamelevel

import androidx.lifecycle.LifecycleOwner
import com.gmpire.guruklub.data.DataManager
import com.gmpire.guruklub.util.IDatabaseCallBack
import com.gmpire.guruklub.view.activity.friendrequest.AddFriendFragment
import com.gmpire.guruklub.view.activity.friendrequest.MyFriendFragment
import com.gmpire.guruklub.view.activity.friendrequest.RecivedPendingFriendRequest
import com.gmpire.guruklub.view.activity.friendrequest.RequestFriendFragment
import com.gmpire.guruklub.view.adapter.IAdapterListener
import com.gmpire.guruklub.view.base.BaseViewModel
import sslwireless.android.easy.loyal.merchant.viewmodel.util.ApiCallbackHelper
import javax.inject.Inject

class GameLevelViewModel @Inject constructor(dataManager: DataManager) : BaseViewModel() {

    val dataManager = dataManager
    var iDatabaseCallBack: IDatabaseCallBack? = null

    fun apiGetGameLevel(
        lifecycleOwner: LifecycleOwner
    ) {
        dataManager.apiHelper.apiGetGameLevel(
            ApiCallbackHelper(livedata(lifecycleOwner, "apiGetGameLevel"))
        )
    }

    fun getLimitedHeartPackage(lifecycleOwner: LifecycleOwner) {
        dataManager.apiHelper.getLimitedHeartPackage(
            ApiCallbackHelper(livedata(lifecycleOwner, "getLimitedHeartPackage"))
        )
    }

    fun getRefToken(lifecycleOwner: LifecycleOwner) {
        dataManager.apiHelper.getRefToken(
            ApiCallbackHelper(livedata(lifecycleOwner, "getRefToken"))
        )
    }

    fun getHeartSettings(lifecycleOwner: LifecycleOwner) {
        dataManager.apiHelper.getHeartSettings(
            ApiCallbackHelper(
                livedata(lifecycleOwner, "getHeartSettings")
            )
        )
    }

    fun updateAdHearts(lifecycleOwner: LifecycleOwner) {
        dataManager.apiHelper.updateAdHearts(
            ApiCallbackHelper(
                livedata(lifecycleOwner, "updateAdHearts")
            )
        )
    }

    fun getGameRules(lifecycleOwner: LifecycleOwner) {
        dataManager.apiHelper.getGameRules(
            ApiCallbackHelper(
                livedata(lifecycleOwner, "getGameRules")
            )
        )
    }

    fun apiGetAdFreePaymentInfo(lifecycleOwner: LifecycleOwner) {
        dataManager.apiHelper.apiGetAdFreePaymentInfo(
            ApiCallbackHelper(
                livedata(
                    lifecycleOwner,
                    "apiGetAdFreePaymentInfo"
                )
            )
        )
    }

    fun getAdsPricingList(lifecycleOwner: LifecycleOwner) {
        dataManager.apiHelper.getAdsPricingList(
            ApiCallbackHelper(livedata(lifecycleOwner, "getAdsPricingList"))
        )
    }

    fun dataGetUserHearts() {
        val userId: Int = dataManager.mPref.prefGetUserInfo().id.toInt()
        dataManager.databaseHelper.executeDBOperation(
            dataManager.roomHelper.getDatabase().gameHeartDao().getAll(userId),
            "dataGetUserHearts",
            iDatabaseCallBack
        )
    }

    fun getHeartAddPayment(lifeCycleOwner: LifecycleOwner, packageId: String) {
        dataManager.apiHelper.getHeartAddPayment(
            packageId,
            ApiCallbackHelper(livedata(lifeCycleOwner, "getHeartAddPayment"))
        )
    }

    fun getPaymentExecution(
        lifeCycleOwner: LifecycleOwner,
        status: String?,
        tranDate: String?,
        tranId: String?,
        valId: String?,
        amount: String?,
        storeAmount: String?,
        bankTranId: String?,
        cardType: String?,
        cardNo: String?
    ) {
        dataManager.apiHelper.getPaymentExecution(
            status ?: "", tranDate ?: "", tranId ?: "", valId
                ?: "", amount ?: "", storeAmount ?: "", bankTranId ?: "", cardType ?: "", cardNo
                ?: "", ApiCallbackHelper(livedata(lifeCycleOwner, "getPaymentExecution"))
        )
    }

    fun apiGetUserInfo(lifeCycleOwner: LifecycleOwner) {
        dataManager.apiHelper.apiGetUserInfo(
            ApiCallbackHelper(livedata(lifeCycleOwner, "apiGetUserInfo"))
        )
    }

    fun getFailedTransaction(status: String, errorMsg : String, tnxId: String, lifeCycleOwner: LifecycleOwner) {
        dataManager.apiHelper.getFailedTransaction(
            status,
            errorMsg,
            tnxId,
            ApiCallbackHelper(livedata(lifeCycleOwner, "getFailedTransaction"))
        )
    }

    fun apiSetGameSound(
        gameBackgroundMusic: String,
        gameBtnSound: String,
        lifecycleOwner: LifecycleOwner
    ) {
        dataManager.apiHelper.apiSetGameSound(
            gameBackgroundMusic,
            gameBtnSound,
            ApiCallbackHelper(livedata(lifecycleOwner, "apiSetGameSound"))
        )
    }

    fun getFriendSuggestion(lifecycleOwner: LifecycleOwner) {
        dataManager.apiHelper.getFriendSuggestion(
            ApiCallbackHelper(livedata(lifecycleOwner,"getFriendSuggestion"))
        )
    }

    fun sendFriendRequest(lifecycleOwner: LifecycleOwner,userId:String) {
        dataManager.apiHelper.sendFriendRequest(userId,ApiCallbackHelper(livedata(lifecycleOwner,"sendFriendRequest")))
    }


    fun searchFriend(lifecycleOwner: LifecycleOwner , keywords: String) {
        dataManager.apiHelper.searchFriend(keywords,ApiCallbackHelper(livedata(lifecycleOwner,"searchFriend")))
    }

    fun getFriends(lifecycleOwner: LifecycleOwner) {
        dataManager.apiHelper.getFriend(ApiCallbackHelper(livedata(lifecycleOwner,"getFriends")))
    }

    fun unFriend(lifecycleOwner: LifecycleOwner, user_id: String) {
        dataManager.apiHelper.unFriend(user_id,ApiCallbackHelper(livedata(lifecycleOwner,"unFriend")))

    }

    fun FriendRequestAlreadySent(lifecycleOwner: LifecycleOwner) {
        dataManager.apiHelper.FriendRequestAlreadySent(ApiCallbackHelper(livedata(lifecycleOwner,"FriendRequestAlreadySent")))
    }

    fun getPendingReceivedRequest(lifecycleOwner: LifecycleOwner) {
        dataManager.apiHelper.getPendingReceivedRequest(ApiCallbackHelper(livedata(lifecycleOwner,"getPendingReceivedRequest")))
    }

    fun responseFriendRequest(
        requester_id: String,
        response: String,
        lifecycleOwner: LifecycleOwner
    ) {
        dataManager.apiHelper.responseFriendRequest(requester_id,response,ApiCallbackHelper(livedata(lifecycleOwner,"responseFriendRequest")))
    }

    fun cancelSentRequest(lifecycleOwner: LifecycleOwner, requester_id: String) {
        dataManager.apiHelper.cancelSentRequest(requester_id,ApiCallbackHelper(livedata(lifecycleOwner,"cancelSentRequest")))
    }


}