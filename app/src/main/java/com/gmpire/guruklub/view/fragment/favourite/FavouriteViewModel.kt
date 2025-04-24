package com.gmpire.guruklub.view.fragment.favourite

import androidx.lifecycle.LifecycleOwner
import com.gmpire.guruklub.data.DataManager
import com.gmpire.guruklub.data.model.library.FilterValues
import com.gmpire.guruklub.data.model.question.Question
import com.gmpire.guruklub.util.ConstantField
import com.gmpire.guruklub.util.IDatabaseCallBack
import com.gmpire.guruklub.view.base.BaseViewModel
import sslwireless.android.easy.loyal.merchant.viewmodel.util.ApiCallbackHelper
import javax.inject.Inject

class FavouriteViewModel @Inject constructor(dataManager: DataManager) : BaseViewModel() {
    val dataManager = dataManager
    var iDatabaseCallBack: IDatabaseCallBack? = null

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

    fun apiGetBatchByCategory(categoryId: String, lifecycleOwner: LifecycleOwner) {
        dataManager.apiHelper.apiGetBatchByCategory(
            categoryId,
            ApiCallbackHelper(livedata(lifecycleOwner, "apiGetBatchByCategory"))
        )
    }

    fun apiGetFavouriteQuestion(
        page: String,
        filterValues: FilterValues,
        userID: String,
        lifecycleOwner: LifecycleOwner
    ) {
        dataManager.apiHelper.apiGetFavouriteQuestion(
            page,
            filterValues,
            userID,
            ApiCallbackHelper(livedata(lifecycleOwner, "apiGetFavouriteQuestion"))
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

    fun apiGetAllVideos(
        categoryId: String,
        subjectId: String,
        sectionId: String,
        topicId: String,
        lifecycleOwner: LifecycleOwner
    ) {
        dataManager.apiHelper.apiGetAllVideos(
            categoryId,
            subjectId,
            sectionId,
            topicId,
            ApiCallbackHelper(livedata(lifecycleOwner, "apiGetAllVideos"))
        )
    }

    fun insertAllTemp(questionList: ArrayList<Question>?) {
        questionList?.forEach { it.question_category = ConstantField.QUESTION_TYPE_TEMP_LIST }
        val questionDTOList = Question.toQuestionDTOs(questionList ?: arrayListOf())
        dataManager.databaseHelper.executeDBOperation(
            dataManager.roomHelper.getDatabase().questionDao().insertAll(questionDTOList),
            "insertAllTemp", iDatabaseCallBack
        )
    }

    fun dataDeleteAllTempQuestions() {
        dataManager.databaseHelper.executeDBOperation(
            dataManager.roomHelper.getDatabase().questionDao().deleteAllTempQuestions(),
            "dataDeleteAllTempQuestions", iDatabaseCallBack
        )
    }


}