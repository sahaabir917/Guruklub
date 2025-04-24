package com.gmpire.guruklub.view.activity.question

import androidx.lifecycle.LifecycleOwner
import com.gmpire.guruklub.data.DataManager
import com.gmpire.guruklub.data.model.QuestionRequest
import com.gmpire.guruklub.view.base.BaseViewModel
import sslwireless.android.easy.loyal.merchant.viewmodel.util.ApiCallbackHelper
import javax.inject.Inject

class QuestionViewModel @Inject constructor(dataManager: DataManager) : BaseViewModel() {

    val dataManager = dataManager

    fun apiGetTopicBySection(section_ids: String, lifecycleOwner: LifecycleOwner) {
        dataManager.apiHelper.apiGetTopicBySection(
            section_ids,
            ApiCallbackHelper(livedata(lifecycleOwner, "apiGetTopicBySection"))
        )
    }

    fun apiGetSectionBySubject(subjectId: String, lifecycleOwner: LifecycleOwner) {
        dataManager.apiHelper.apiGetSectionsBySubject(
            subjectId,
            ApiCallbackHelper(livedata(lifecycleOwner, "apiGetSectionBySubject"))
        )
    }

    fun apiGetSubjectByCategory(categoryId: String, lifecycleOwner: LifecycleOwner) {
        dataManager.apiHelper.apiGetSubjectByCategory(
            categoryId,
            ApiCallbackHelper(livedata(lifecycleOwner, "apiGetSubjectByCategory"))
        )
    }

    fun fetchAllCategory(lifecycleOwner: LifecycleOwner) {
        dataManager.apiHelper.apiGetSAllCategory(
            ApiCallbackHelper(livedata(lifecycleOwner, "fetchAllCategory"))
        )
    }

    fun fetchAddQuestion(questionRequest: QuestionRequest, lifecycleOwner: LifecycleOwner) {
        dataManager.apiHelper.apiAddQuestion(
            questionRequest,
            ApiCallbackHelper(livedata(lifecycleOwner, "addQuestion"))
        )
    }

    fun getQuestionByKeyword(userId: String, keyword: String, lifecycleOwner: LifecycleOwner) {
        dataManager.apiHelper.apiGetQuestionsByKeyword(
            userId,
            keyword,
            ApiCallbackHelper(livedata(lifecycleOwner, "getQuestionByKeyword"))
        )
    }

    fun apiAddVideos(
        categoryId: String,
        topicId: String?,
        videoTitle: String?,
        videoUrl: String?,
        lifecycleOwner: LifecycleOwner
    ) {
        dataManager.apiHelper.apiAddVideos(
            categoryId,
            topicId?:"1",
            videoTitle?:"",
            videoUrl?:"",
            ApiCallbackHelper(livedata(lifecycleOwner, "apiAddVideos"))
        )
    }
}