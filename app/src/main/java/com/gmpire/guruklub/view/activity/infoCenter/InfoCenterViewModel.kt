package com.gmpire.guruklub.view.activity.infoCenter

import androidx.lifecycle.LifecycleOwner
import com.gmpire.guruklub.data.DataManager
import com.gmpire.guruklub.view.base.BaseViewModel
import sslwireless.android.easy.loyal.merchant.viewmodel.util.ApiCallbackHelper
import javax.inject.Inject

class InfoCenterViewModel @Inject constructor(dataManager: DataManager) : BaseViewModel() {
    val dataManager = dataManager

    fun apiGetNewsCategory(lifecycleOwner: LifecycleOwner) {
        dataManager.apiHelper.apiGetNewsCategory(
            ApiCallbackHelper(livedata(lifecycleOwner, "apiGetNewsCategory"))
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


    fun apiGetBatchByCategory(categoryId: String, lifecycleOwner: LifecycleOwner) {
        dataManager.apiHelper.apiGetBatchByCategory(
            categoryId,
            ApiCallbackHelper(livedata(lifecycleOwner, "apiGetBatchByCategory"))
        )
    }

    fun apiGetQuestionListByBatch(
        user_id: String,
        category_id: String,
        subject_ids: String?,
        section_ids: String?,
        topic_ids: String?,
        batch_id: String,
        lifecycleOwner: LifecycleOwner
    ) {
        var subjects = ""
        var sections = ""
        var topics = ""

        subject_ids?.let {
            subjects = it
        }

        section_ids?.let {
            sections = it
        }

        topic_ids?.let {
            topics = it
        }

        dataManager.apiHelper.apiGetQuestionListForBatch(
            user_id,
            category_id,
            subjects,
            sections,
            topics,
            batch_id,
            ApiCallbackHelper(livedata(lifecycleOwner, "apiGetQuestionListByBatch"))
        )

    }

    fun apiReportAboutQuestion(
        question_id: String,
        type: String,
        details: String, lifecycleOwner: LifecycleOwner
    ) {
        dataManager.apiHelper.apiReportAboutQuestion(
            question_id,
            type,
            details,
            ApiCallbackHelper(livedata(lifecycleOwner, "apiReportAboutQuestion"))
        )
    }

    fun apiBookmarkQuestion(
        question_id: String,
        category_id: String,
        lifecycleOwner: LifecycleOwner
    ) {
        dataManager.apiHelper.apiBookmarkQuestion(
            question_id,
            category_id,
            ApiCallbackHelper(livedata(lifecycleOwner, "apiBookmarkQuestion"))
        )
    }

    fun apiUnBookmarkQuestion(
        question_id: String,
        category_id: String,
        lifecycleOwner: LifecycleOwner
    ) {
        dataManager.apiHelper.apiUnBookmarkQuestion(
            question_id,
            category_id,
            ApiCallbackHelper(livedata(lifecycleOwner, "apiUnBookmarkQuestion"))
        )
    }



}