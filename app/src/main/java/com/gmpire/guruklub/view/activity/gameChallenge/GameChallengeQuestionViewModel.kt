package com.gmpire.guruklub.view.activity.gameChallenge

import androidx.lifecycle.LifecycleOwner
import com.gmpire.guruklub.data.DataManager
import com.gmpire.guruklub.data.local_db.dto.GameHeartDTO
import com.gmpire.guruklub.data.model.question.GameResponseQuestion
import com.gmpire.guruklub.util.IDatabaseCallBack
import com.gmpire.guruklub.data.model.gamequestions.GameChallengeFinishRequestItem
import com.gmpire.guruklub.view.base.BaseViewModel
import sslwireless.android.easy.loyal.merchant.viewmodel.util.ApiCallbackHelper
import javax.inject.Inject

class GameChallengeQuestionViewModel @Inject constructor(dataManager: DataManager) :
    BaseViewModel() {

    val dataManager = dataManager
    var iDatabaseCallBack: IDatabaseCallBack? = null

    fun apiMilestoneChallenge(
        categoryId: String,
        challengeId: String,
        lifecycleOwner: LifecycleOwner
    ) {
        dataManager.apiHelper.apiMilestoneChallenge(
            categoryId, challengeId,
            ApiCallbackHelper(livedata(lifecycleOwner, "apiMilestoneChallenge"))
        )
    }

    fun insertGameHeart(gameHeartDTO: GameHeartDTO) {
        dataManager.databaseHelper.executeDBOperation(
            dataManager.roomHelper.getDatabase().gameHeartDao().insert(gameHeartDTO),
            "insertGameHeart",
            iDatabaseCallBack
        )
    }

    fun dataGetUserHearts() {
        val userId: Int = dataManager.mPref.prefGetUserInfo().id.toInt()

        dataManager.databaseHelper.executeDBOperation(
            dataManager.roomHelper.getDatabase().gameHeartDao().getAll(userId),
            "getUserHearts",
            iDatabaseCallBack
        )
    }

    fun apiHeartsUpdate(
        type: String,
        lifecycleOwner: LifecycleOwner
    ) {
        dataManager.apiHelper.apiHeartsUpdate(
            type,
            ApiCallbackHelper(livedata(lifecycleOwner, "apiHeartsUpdate"))
        )
    }

    fun apiMilestoneChallengeDone(
        categoryId: String,
        challengeId: String,
        questions: List<GameResponseQuestion>?,
        lifecycleOwner: LifecycleOwner
    ) {
        dataManager.apiHelper.apiMilestoneChallengeDone(
            GameChallengeFinishRequestItem(
                challengeId,
                categoryId,
                questions
            ),
            ApiCallbackHelper(livedata(lifecycleOwner, "apiMilestoneChallengeDone"))
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

    fun deleteLastGameHeartData() {
        val userId = dataManager.mPref.prefGetUserInfo().id.toInt()
        dataManager.databaseHelper.executeDBOperation(
            dataManager.roomHelper.getDatabase().gameHeartDao().deleteLast(userId),
            "dbLastGameUserHeartDelete",
            iDatabaseCallBack
        )
    }

    fun getAllGameHeartData() {
        val userId = dataManager.mPref.prefGetUserInfo().id.toInt()
        dataManager.databaseHelper.executeDBOperation(
            dataManager.roomHelper.getDatabase().gameHeartDao().getAll(userId),
            "getAllGameHeartData",
            iDatabaseCallBack
        )
    }

}