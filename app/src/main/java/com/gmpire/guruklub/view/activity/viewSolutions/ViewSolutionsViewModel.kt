package com.gmpire.guruklub.view.activity.viewSolutions

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

class ViewSolutionsViewModel @Inject constructor(dataManager: DataManager) : BaseViewModel() {

    val dataManager = dataManager
    var iDatabaseCallBack : IDatabaseCallBack ?= null

    fun apiGetGameSubjectSectionTopic(game_id: String, lifecycleOwner: LifecycleOwner) {
        dataManager.apiHelper.apiGetGameSubjectSectionTopic(
            game_id,
            ApiCallbackHelper(livedata(lifecycleOwner, "apiGetGameSubjectSectionTopic"))
        )
    }

    fun apiGetBatchByCategory(categoryId: String, lifecycleOwner: LifecycleOwner) {
        dataManager.apiHelper.apiGetBatchByCategory(
            categoryId,
            ApiCallbackHelper(livedata(lifecycleOwner, "apiGetBatchByCategory"))
        )
    }

    fun apiGetGameSolution(
        page: String,
        user_id: String,
        game_id: String,
        slug: String,
        filterValues: FilterValues,
        lifecycleOwner: LifecycleOwner
    ) {
        dataManager.apiHelper.apiGetGameSolution(
            page, user_id, game_id, slug,
            filterValues,
            ApiCallbackHelper(livedata(lifecycleOwner, "apiGetGameSolution"))
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

    fun dataGetSubjectListByCategory() {
        dataManager.databaseHelper.executeDBOperation(
            dataManager.roomHelper.getDatabase().subjectDao().getAll(),
            "dataGetSubjectListByCategory", iDatabaseCallBack
        )
    }

    fun checkFilterValuesAvailable() {
        dataManager.databaseHelper.executeDBOperation(
            dataManager.roomHelper.getDatabase().subjectDao().checkIfDataAvailable(),
            "checkFilterValuesAvailable", iDatabaseCallBack
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


}