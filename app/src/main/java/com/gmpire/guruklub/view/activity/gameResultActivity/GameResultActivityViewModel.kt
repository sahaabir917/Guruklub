package com.gmpire.guruklub.view.activity.gameResultActivity

import androidx.lifecycle.LifecycleOwner
import com.gmpire.guruklub.data.DataManager
import com.gmpire.guruklub.data.model.game.GameResultSubmitRequestItem
import com.gmpire.guruklub.view.base.BaseViewModel
import sslwireless.android.easy.loyal.merchant.viewmodel.util.ApiCallbackHelper
import javax.inject.Inject

class GameResultActivityViewModel @Inject constructor(dataManager: DataManager) : BaseViewModel() {

    val dataManager = dataManager

    fun apiGameResult(gameResultSubmitRequestItem: GameResultSubmitRequestItem, lifecycleOwner: LifecycleOwner) {
        dataManager.apiHelper.apiGameResult(gameResultSubmitRequestItem, ApiCallbackHelper(livedata(lifecycleOwner,"apiGameResult")))
    }

    fun apiGameResultByGameId(game_id: String, lifecycleOwner: LifecycleOwner) {
        dataManager.apiHelper.apiGameResultByGameId(game_id, ApiCallbackHelper(livedata(lifecycleOwner,"apiGameResultByGameId")))
    }


}