package com.gmpire.guruklub.view.fragment.notification

import androidx.lifecycle.LifecycleOwner
import com.gmpire.guruklub.data.DataManager
import com.gmpire.guruklub.view.base.BaseViewModel
import sslwireless.android.easy.loyal.merchant.viewmodel.util.ApiCallbackHelper
import javax.inject.Inject

class NotificationViewModel @Inject constructor(dataManager: DataManager) : BaseViewModel() {
    val dataManager = dataManager

    fun apiGetAllNotifications(
        userId: String,
        page: String,
        category_id: String, lifecycleOwner: LifecycleOwner
    ) {
        dataManager.apiHelper.apiGetAllNotifications(
            userId,
            page,
            category_id,
            ApiCallbackHelper(livedata(lifecycleOwner, "apiGetAllNotifications"))
        )
    }
    fun apiGetNews(
        page: String,
        category_id: String,
        search_key: String,
        dateFrom: String,
        dateTo: String,
        lifecycleOwner: LifecycleOwner
    ) {
        dataManager.apiHelper.apiGetNews(
            page, category_id, search_key, dateFrom, dateTo, "",
            ApiCallbackHelper(livedata(lifecycleOwner, "apiGetNews"))
        )
    }

    fun apiGePopularNews(
        page: String,
        category_id: String,
        search_key: String,
        dateFrom: String,
        dateTo: String,
        lifecycleOwner: LifecycleOwner
    ) {
        dataManager.apiHelper.apiGetNews(
            page, category_id, search_key, dateFrom, dateTo, "1",
            ApiCallbackHelper(livedata(lifecycleOwner, "apiGePopularNews"))
        )
    }

    fun apiGetNewsById(
        newsId: String,
        lifecycleOwner: LifecycleOwner
    ) {
        dataManager.apiHelper.apiGetNewsById(
            newsId,
            ApiCallbackHelper(livedata(lifecycleOwner, "apiGetNewsById"))
        )
    }


}