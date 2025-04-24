package com.gmpire.guruklub.view.fragment.home

import androidx.lifecycle.LifecycleOwner
import androidx.sqlite.db.SimpleSQLiteQuery
import com.gmpire.guruklub.data.DataManager
import com.gmpire.guruklub.data.local_db.dto.GameHeartDTO
import com.gmpire.guruklub.data.model.categoryAndSubject.Section
import com.gmpire.guruklub.data.model.categoryAndSubject.Subject
import com.gmpire.guruklub.data.model.categoryAndSubject.Topic
import com.gmpire.guruklub.data.model.library.FilterValues
import com.gmpire.guruklub.data.model.question.Question
import com.gmpire.guruklub.util.IDatabaseCallBack
import com.gmpire.guruklub.view.base.BaseViewModel
import sslwireless.android.easy.loyal.merchant.viewmodel.util.ApiCallbackHelper
import javax.inject.Inject


class HomeFragmentViewModel @Inject constructor(dataManager: DataManager) : BaseViewModel() {
    val dataManager = dataManager
    var iDatabaseCallBack: IDatabaseCallBack? = null

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

    fun apiGetSubjectListByCategory(categoryId: String, lifecycleOwner: LifecycleOwner) {
        dataManager.apiHelper.apiGetSubjectListByCategory(
            categoryId, "",
            ApiCallbackHelper(livedata(lifecycleOwner, "apiGetSubjectListByCategory"))
        )
    }

    fun apiGetSectionsBySubject(subject_ids: String, lifecycleOwner: LifecycleOwner) {
        dataManager.apiHelper.apiGetSectionsBySubject(
            subject_ids,
            ApiCallbackHelper(livedata(lifecycleOwner, "apiGetSectionsBySubject"))
        )
    }

    fun apiGetTopicBySection(section_ids: String, lifecycleOwner: LifecycleOwner) {
        dataManager.apiHelper.apiGetTopicBySection(
            section_ids,
            ApiCallbackHelper(livedata(lifecycleOwner, "apiGetTopicBySection"))
        )
    }

    fun apiGetQuestionListForHome(
        user_id: String?,
        category_id: String,
        subject_ids: String?,
        section_ids: String?,
        topic_ids: String?,
        lifecycleOwner: LifecycleOwner
    ) {
        var subjects: String = ""
        var sections: String = ""
        var topics: String = ""

        subject_ids?.let {
            subjects = it
        }

        section_ids?.let {
            sections = it
        }

        topic_ids?.let {
            topics = it
        }

        dataManager.apiHelper.apiGetQuestionListForHome(
            user_id,
            category_id,
            subjects,
            sections,
            topics,
            "",
            ApiCallbackHelper(livedata(lifecycleOwner, "apiGetQuestionListForHome"))
        )
    }

    fun apiGetMoreQuestionListForHome(
        user_id: String?,
        category_id: String,
        subject_ids: String?,
        section_ids: String?,
        topic_ids: String?, lifecycleOwner: LifecycleOwner
    ) {
        var subjects: String = ""
        var sections: String = ""
        var topics: String = ""

        subject_ids?.let {
            subjects = it
        }

        section_ids?.let {
            sections = it
        }

        topic_ids?.let {
            topics = it
        }


        dataManager.apiHelper.apiGetQuestionListForHome(
            user_id.toString(),
            category_id,
            subjects,
            sections,
            topics, "",
            ApiCallbackHelper(livedata(lifecycleOwner, "apiGetMoreQuestionListForHome"))
        )
    }

    fun apiGetOfflineQuestionListForHome(
        category_id: String,
        subject_ids: String?,
        section_ids: String?,
        topic_ids: String?,
        lifecycleOwner: LifecycleOwner
    ) {
        var subjects: String = ""
        var sections: String = ""
        var topics: String = ""

        subject_ids?.let {
            subjects = it
        }

        section_ids?.let {
            sections = it
        }

        topic_ids?.let {
            topics = it
        }

        dataManager.apiHelper.apiGetOfflineQuestionListForHome(
            category_id,
            subjects,
            sections,
            topics,
            "",
            ApiCallbackHelper(livedata(lifecycleOwner, "apiGetOfflineQuestionListForHome"))
        )
    }

    fun apiGetQuestionById(
        user_id: String?,
        category_id: String,
        question_id: String,
        lifecycleOwner: LifecycleOwner
    ) {
        dataManager.apiHelper.apiGetQuestionById(
            user_id,
            category_id,
            question_id,
            ApiCallbackHelper(livedata(lifecycleOwner, "apiGetQuestionById"))
        )
    }


    // database operations
    fun dataGetAllOfflineQuestions() {
        dataManager.databaseHelper.executeDBOperation(
            dataManager.roomHelper.getDatabase().questionDao()
                .getAllOfflineQuestions(),
            "dataGetAllOfflineQuestions", iDatabaseCallBack
        )
    }

    fun dataInsertOfflineQuestions(quesList: List<Question>) {
        dataManager.databaseHelper.executeDBOperation(
            dataManager.roomHelper.getDatabase().questionDao()
                .insertAll(Question.toQuestionDTOs(quesList)),
            "dataInsertOfflineQuestions", iDatabaseCallBack
        )
    }

    fun dataDeleteOfflineQuestions() {
        dataManager.databaseHelper.executeDBOperation(
            dataManager.roomHelper.getDatabase().questionDao()
                .deleteAllOfflineQuestions(),
            "dataDeleteOfflineQuestions", iDatabaseCallBack
        )
    }


    // Subject
    fun dataInsertAllSubjectList(subjectList: List<Subject>) {
        dataManager.databaseHelper.executeDBOperation(
            dataManager.roomHelper.getDatabase().subjectDao()
                .insertAll(Subject.toSubjectDTOs(subjectList)),
            "dataInsertAllSubjectList", iDatabaseCallBack
        )
    }

    fun dataDeleteAllSubjectList() {
        dataManager.databaseHelper.executeDBOperation(
            dataManager.roomHelper.getDatabase().subjectDao().deleteAll(),
            "dataDeleteAllSubjectList", iDatabaseCallBack
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

    // Sections
    fun dataInsertAllSectionList(sectionList: List<Section>) {
        dataManager.databaseHelper.executeDBOperation(
            dataManager.roomHelper.getDatabase().sectionDao()
                .insertAll(Section.toSectionDTOs(sectionList)),
            "dataInsertAllSectionList", iDatabaseCallBack
        )
    }

    fun dataDeleteAllSectionList() {
        dataManager.databaseHelper.executeDBOperation(
            dataManager.roomHelper.getDatabase().sectionDao().deleteAll(),
            "dataDeleteAllSectionList", iDatabaseCallBack
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

    // Topic
    fun dataInsertAllTopicList(topicList: List<Topic>) {
        dataManager.databaseHelper.executeDBOperation(
            dataManager.roomHelper.getDatabase().topicDao()
                .insertAll(Topic.toTopicDTOs(topicList)),
            "dataInsertAllTopicList", iDatabaseCallBack
        )
    }

    fun dataDeleteAllTopicList() {
        dataManager.databaseHelper.executeDBOperation(
            dataManager.roomHelper.getDatabase().topicDao().deleteAll(),
            "dataDeleteAllTopicList", iDatabaseCallBack
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

    fun dataGetAllPassedTempQuestions() {
        dataManager.databaseHelper.executeDBOperation(
            dataManager.roomHelper.getDatabase().questionDao().getAllTempQuestions(),
            "dataGetAllPassedTempQuestions", iDatabaseCallBack
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

    fun apiPracticeRewardHeart(
        heartVariable: String,
        practice: String,
        lifecycleOwner: LifecycleOwner
    ) {
        dataManager.apiHelper.apiPracticeRewardHeart(
            heartVariable,
            practice,
            ApiCallbackHelper(livedata(lifecycleOwner, "apiPracticeRewardHeart"))
        )
    }

    fun insertGameHeart(gameHeartDTO: GameHeartDTO) {
        dataManager.databaseHelper.executeDBOperation(
            dataManager.roomHelper.getDatabase().gameHeartDao().insert(gameHeartDTO),
            "insertGameHeart",
            iDatabaseCallBack
        )
    }

    fun deleteLastGameHeartData() {
        val userId = dataManager.mPref.prefGetUserInfo().id.toInt()
        dataManager.databaseHelper.executeDBOperation(
            dataManager.roomHelper.getDatabase().gameHeartDao().deleteLast(userId),
            "dbLastGameUserHeartDelete",
            iDatabaseCallBack
        )
    }

}