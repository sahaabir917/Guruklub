package com.gmpire.guruklub.view.activity.library

import androidx.lifecycle.LifecycleOwner
import androidx.sqlite.db.SimpleSQLiteQuery
import com.gmpire.guruklub.data.DataManager
import com.gmpire.guruklub.data.model.library.FilterValues
import com.gmpire.guruklub.data.model.question.Question
import com.gmpire.guruklub.util.ConstantField
import com.gmpire.guruklub.util.IDatabaseCallBack
import com.gmpire.guruklub.view.base.BaseViewModel
import sslwireless.android.easy.loyal.merchant.viewmodel.util.ApiCallbackHelper
import javax.inject.Inject

class LibraryViewmodel @Inject constructor(dataManager: DataManager) : BaseViewModel() {
    val dataManager = dataManager
    var iDatabaseCallBack: IDatabaseCallBack? = null

    fun apiGetTopicBySection(section_ids: String, lifecycleOwner: LifecycleOwner) {
        dataManager.apiHelper.apiGetTopicBySection(
            section_ids,
            ApiCallbackHelper(livedata(lifecycleOwner, "apiGetTopicBySection"))
        )
    }

    fun apiGetTopicBySectionLibraryForShow(section_ids: String, lifecycleOwner: LifecycleOwner) {
        dataManager.apiHelper.apiGetTopicBySection(
            section_ids,
            ApiCallbackHelper(livedata(lifecycleOwner, "apiGetTopicBySectionLibraryForShow"))
        )
    }

    fun apiGetSectionBySubject(subjectId: String, lifecycleOwner: LifecycleOwner) {
        dataManager.apiHelper.apiGetSectionsBySubject(
            subjectId,
            ApiCallbackHelper(livedata(lifecycleOwner, "apiGetSectionBySubject"))
        )
    }

    fun apiGetSectionBySubjectLibraryForShow(subjectId: String, lifecycleOwner: LifecycleOwner) {
        dataManager.apiHelper.apiGetSectionsBySubject(
                subjectId,
                ApiCallbackHelper(livedata(lifecycleOwner, "apiGetSectionBySubjectLibraryForShow"))
        )
    }

    fun apiGetSubjectByCategoryLibrary(categoryId: String, lifecycleOwner: LifecycleOwner) {
        dataManager.apiHelper.apiGetSubjectByCategory(
            categoryId,
            ApiCallbackHelper(livedata(lifecycleOwner, "apiGetSubjectByCategoryLibrary"))
        )
    }

    fun apiGetBatchByCategory(categoryId: String, lifecycleOwner: LifecycleOwner) {
        dataManager.apiHelper.apiGetBatchByCategory(
            categoryId,
            ApiCallbackHelper(livedata(lifecycleOwner, "apiGetBatchByCategory"))
        )
    }

    fun fetchAllCategory(lifecycleOwner: LifecycleOwner) {
        dataManager.apiHelper.apiGetSAllCategory(
            ApiCallbackHelper(livedata(lifecycleOwner, "fetchAllCategory"))
        )
    }

    fun fetchMostPopular(categoryId: String, lifecycleOwner: LifecycleOwner) {
        dataManager.apiHelper.apiMostPopular(
            categoryId,
            ApiCallbackHelper(livedata(lifecycleOwner, "fetchMostPopular"))
        )
    }

    fun fetchRecentLearn(
        category_id: String,
        lifecycleOwner: LifecycleOwner
    ) {
        dataManager.apiHelper.apiRecentlyLearn(
            category_id,
            ApiCallbackHelper(livedata(lifecycleOwner, "fetchRecentLearn"))
        )
    }

    fun apiGetLibrary(userID: String, filterValues: FilterValues, lifecycleOwner: LifecycleOwner) {
        dataManager.apiHelper.apiGetLibrary(
            userID,
            filterValues,
            ApiCallbackHelper(livedata(lifecycleOwner, "apiGetLibrary"))
        )
    }

    fun apiGetQuestionLibrary(
        user_id: String,
        filterValues: FilterValues,
        lifecycleOwner: LifecycleOwner
    ) {
        dataManager.apiHelper.apiGetQuestionLibrary(
            user_id,
            filterValues,
            ApiCallbackHelper(livedata(lifecycleOwner, "apiGetQuestionLibrary"))
        )
    }

    fun apiGetQuestionLibraryPage(
        user_id: String,
        filterValues: FilterValues,
        page: String,
        lifecycleOwner: LifecycleOwner
    ) {
        dataManager.apiHelper.apiGetQuestionLibraryPage(
            user_id,
            filterValues,
            page,
            ApiCallbackHelper(livedata(lifecycleOwner, "apiGetQuestionLibrary"))
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

    fun insertAllTemp(questionList: List<Question>) {
        questionList.forEach { it.question_category = ConstantField.QUESTION_TYPE_TEMP_LIST }
        val questionDTOList = Question.toQuestionDTOs(questionList)
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

    fun dataGetSubjectListByCategoryLibrary() {
        dataManager.databaseHelper.executeDBOperation(
            dataManager.roomHelper.getDatabase().subjectDao().getAll(),
            "dataGetSubjectListByCategoryLibrary", iDatabaseCallBack
        )
    }

    fun checkFilterValuesAvailable() {
        dataManager.databaseHelper.executeDBOperation(
            dataManager.roomHelper.getDatabase().subjectDao().checkIfDataAvailable(),
            "checkFilterValuesAvailable", iDatabaseCallBack
        )
    }

    fun checkTest() {
        dataManager.databaseHelper.executeDBOperation(
            dataManager.roomHelper.getDatabase().subjectDao().checkTest(),
            "checkTest", iDatabaseCallBack
        )
    }

    fun checkSecTest() {
        dataManager.databaseHelper.executeDBOperation(
            dataManager.roomHelper.getDatabase().sectionDao().getAll(),
            "checkSecTest", iDatabaseCallBack
        )
    }

    fun dataGetAllSectionListBySubject(subjectIds: String) {
        val subjectIdList = subjectIds.split(",").toTypedArray()
        val query = ("SELECT * FROM Section"
                + " WHERE subject_id IN (" + makePlaceholders(subjectIds.length) + ")")

        dataManager.databaseHelper.executeDBOperation(
            dataManager.roomHelper.getDatabase().sectionDao()
                .getAllBySubjectIds(SimpleSQLiteQuery(query, subjectIdList)),
            "dataGetAllSectionListBySubject", iDatabaseCallBack
        )
    }

    fun dataGetAllTopicListBySection(sectionIds: String) {
        val subjectIdList = sectionIds.split(",").toTypedArray()
        val query = ("SELECT * FROM Topic"
                + " WHERE section_id IN (" + makePlaceholders(sectionIds.length) + ")")
        dataManager.databaseHelper.executeDBOperation(
            dataManager.roomHelper.getDatabase().topicDao()
                .getAllBySectionIds(SimpleSQLiteQuery(query, subjectIdList)),
            "dataGetAllTopicListBySection", iDatabaseCallBack
        )
    }

    private fun makePlaceholders(length: Int): String {
        var placeHolders = ""
        var pos = 0
        while (pos <= length) {
            if (placeHolders.isEmpty()) {
                placeHolders += "?"
            }
            placeHolders += ", ?"
            pos++
        }
        return placeHolders
    }

    fun apiGetSubjectByCategoryLibraryForShow(categoryId: String, lifecycleOwner: LifecycleOwner) {
        dataManager.apiHelper.apiGetSubjectByCategory(
            categoryId,
            ApiCallbackHelper(livedata(lifecycleOwner, "apiGetSubjectByCategoryLibraryForShow"))
        )
    }

    fun apiGetResumeable(subjectId: String?, sectionId: String?, topicId: String?, categoryId: String, lifecycleOwner: LifecycleOwner) {
        dataManager.apiHelper.apiGetResumeable(
            subjectId?:"1",
            sectionId?:"2",
            topicId?:"2",
            categoryId,
            ApiCallbackHelper(livedata(lifecycleOwner, "apiGetResumeable"))
        )

    }

    fun apiShowResumeAbleState(lifecycleOwner: LifecycleOwner) {
        dataManager.apiHelper.apiShowResumeAbleState(ApiCallbackHelper(livedata(lifecycleOwner,"apiShowResumeAbleState")))
    }

    fun apiGetQuestionLibraryPageForResumeable(user_id: String, filterValues: FilterValues, page: String, lifecycleOwner: LifecycleOwner) {
        dataManager.apiHelper.apiGetQuestionLibraryPageForResumeable(
            user_id,
            filterValues,
            page,
            ApiCallbackHelper(livedata(lifecycleOwner, "apiGetQuestionLibraryPageForResumeable"))
        )
    }


}