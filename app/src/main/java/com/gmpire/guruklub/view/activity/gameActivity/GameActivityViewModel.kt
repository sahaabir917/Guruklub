package com.gmpire.guruklub.view.activity.gameActivity

import androidx.lifecycle.LifecycleOwner
import com.gmpire.guruklub.data.DataManager
import com.gmpire.guruklub.data.model.library.FilterValues
import com.gmpire.guruklub.view.base.BaseViewModel
import sslwireless.android.easy.loyal.merchant.viewmodel.util.ApiCallbackHelper
import javax.inject.Inject

class GameActivityViewModel @Inject constructor(dataManager: DataManager) : BaseViewModel() {

    val dataManager = dataManager

    fun apiGetGameQuestions(
        category_id: String,
        game_type_id: String,
        lifecycleOwner: LifecycleOwner
    ) {
        dataManager.apiHelper.apiGetGameQuestions(
            category_id,
            game_type_id,
            ApiCallbackHelper(livedata(lifecycleOwner, "apiGetGameQuestions"))
        )
    }

    fun apiGetCustomGameQuestions(
        category_id: String,
        game_type_id: String,
        ques_num: Int,
        sub_id: String,
        lifecycleOwner: LifecycleOwner
    ) {
        dataManager.apiHelper.apiGetCustomGameQuestions(
            category_id,
            game_type_id,
            ques_num,
            sub_id,
            ApiCallbackHelper(livedata(lifecycleOwner, "apiGetGameQuestions"))
        )
    }

    fun apiGetQuestionLibrary(user_id : String, filterValues: FilterValues, lifecycleOwner: LifecycleOwner) {
        dataManager.apiHelper.apiGetQuestionLibrary(
            user_id,
            filterValues,
            ApiCallbackHelper(livedata(lifecycleOwner, "apiGetQuestionLibrary"))
        )
    }

}