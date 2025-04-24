package com.gmpire.guruklub.view.activity.library

import androidx.lifecycle.LifecycleOwner
import com.gmpire.guruklub.data.DataManager
import com.gmpire.guruklub.data.model.library.YoutubeMain
import com.gmpire.guruklub.view.base.BaseViewModel
import sslwireless.android.easy.loyal.merchant.viewmodel.util.ApiCallbackHelper


/**
 * Created by Tahsin Rahman on 9/11/20.
 */


class SingleVideoViewModel @javax.inject.Inject constructor(dataManager: DataManager) : BaseViewModel(){

    val dataManager = dataManager

    fun apiGetYoutubeSuggestedList (videoId : String, apiKey : String) : YoutubeMain? {
        val body = dataManager.apiHelper.apiService.getYoutubeSuggestions(videoId, apiKey)?.execute()?.body()
        return body
    }

    fun apiSendVideoReport(videoID: String, details: String, lifecycleOwner: LifecycleOwner) {
        dataManager.apiHelper.apiSendVideoReport(
            videoID,
            details,
            ApiCallbackHelper(livedata(lifecycleOwner, "apiSendVideoReport"))
        )
    }
}