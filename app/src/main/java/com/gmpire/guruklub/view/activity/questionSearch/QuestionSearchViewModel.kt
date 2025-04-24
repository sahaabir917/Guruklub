package com.gmpire.guruklub.view.activity.questionSearch

import androidx.lifecycle.LifecycleOwner
import com.gmpire.guruklub.data.DataManager
import com.gmpire.guruklub.data.local_db.dto.QuestionDTO
import com.gmpire.guruklub.data.model.categoryAndSubject.Subject
import com.gmpire.guruklub.data.model.question.Question
import com.gmpire.guruklub.util.ConstantField
import com.gmpire.guruklub.util.DatabaseHelper
import com.gmpire.guruklub.util.IDatabaseCallBack
import com.gmpire.guruklub.view.base.BaseViewModel
import sslwireless.android.easy.loyal.merchant.viewmodel.util.ApiCallbackHelper
import javax.inject.Inject

class QuestionSearchViewModel @Inject constructor(dataManager: DataManager) : BaseViewModel() {
    val dataManager = dataManager
    var iDatabaseCallBack: IDatabaseCallBack? = null

    fun getQuestionByKeyword(userId: String, keyword: String, lifecycleOwner: LifecycleOwner) {
        dataManager.apiHelper.apiGetQuestionsByKeywords(
            userId,
            keyword,
            ApiCallbackHelper(livedata(lifecycleOwner, "getQuestionByKeywords"))
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


    fun insertQuestion(question: Question, existing: Boolean) {
        val localId = if (existing) {
            question.localId
        } else {
            0
        }

        dataManager.databaseHelper.executeDBOperation(
            dataManager.roomHelper.getDatabase().questionDao()
                .insert(
                    QuestionDTO(
                        localId, question.answer,
                        question.answer_explain,
                        question.difficulty,
                        question.id,
                        question.topic_id,
                        question.section_id,
                        question.subject_id,
                        question.options,
                        question.picture,
                        question.title,
                        question.is_bookmarked,
                        question.is_math,
                        question.answered,
                        question.answered_position,
                        question.answer_type,
                        ConstantField.QUESTION_TYPE_RECENT_SEARCH,
                        dataManager.mPref.prefGetUserInfo().id,
                        question.has_image,
                        question.batches
                    )
                ),
            "insertQuestion", iDatabaseCallBack
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

    fun getFiveRecentQuestions() {
        dataManager.databaseHelper.executeDBOperation(
            dataManager.roomHelper.getDatabase().questionDao().getFiveRecent(),
            "getFiveRecentQuestions", iDatabaseCallBack
        )
    }

    fun checkIfAlreadyAdded(id: String) {
        dataManager.databaseHelper.executeDBOperation(
            dataManager.roomHelper.getDatabase().questionDao().checkIfAlreadyAdded(id),
            "checkIfAlreadyAdded", iDatabaseCallBack
        )
    }

    fun getQuestionCount() {
        dataManager.databaseHelper.executeDBOperation(
            dataManager.roomHelper.getDatabase().questionDao().getRecentQuesCount(),
            "getQuestionCount", iDatabaseCallBack
        )
    }

    fun deleteOldestQuestion() {
        dataManager.databaseHelper.executeDBOperation(
            dataManager.roomHelper.getDatabase().questionDao().deleteOldest(),
            "deleteOldestQuestion", iDatabaseCallBack
        )
    }
}