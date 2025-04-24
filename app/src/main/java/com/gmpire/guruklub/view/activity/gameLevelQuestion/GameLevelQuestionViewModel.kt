package com.gmpire.guruklub.view.activity.gameLevelQuestion

import androidx.lifecycle.LifecycleOwner
import com.gmpire.guruklub.data.DataManager
import com.gmpire.guruklub.data.local_db.dto.GameHeartDTO
import com.gmpire.guruklub.data.local_db.dto.GameQuestionDTO
import com.gmpire.guruklub.data.model.gameheartpackages.GameLevelOverRequestItem
import com.gmpire.guruklub.data.model.gameheartpackages.GameLevelUpRequestItem
import com.gmpire.guruklub.data.model.gameheartpackages.GamePauseUserStateRequestItem
import com.gmpire.guruklub.data.model.question.GameResponseQuestion
import com.gmpire.guruklub.util.IDatabaseCallBack
import com.gmpire.guruklub.view.base.BaseViewModel
import sslwireless.android.easy.loyal.merchant.viewmodel.util.ApiCallbackHelper
import javax.inject.Inject

class GameLevelQuestionViewModel @Inject constructor(dataManager: DataManager) : BaseViewModel() {

    val dataManager = dataManager
    var iDatabaseCallBack: IDatabaseCallBack? = null

    fun apiGetQuestionListForGame(
        category_id: String,
        gameLevelId: String,
        lifecycleOwner: LifecycleOwner
    ) {
        dataManager.apiHelper.apiGetQuestionListForGame(
            category_id,
            gameLevelId,
            ApiCallbackHelper(livedata(lifecycleOwner, "apiGetQuestionListForGame"))
        )
    }

    fun apiGameLevelOver(
        challengeId: String?,
        questions: ArrayList<GameResponseQuestion>,
        lifecycleOwner: LifecycleOwner
    ) {
        val data = GameLevelOverRequestItem()
        data.category_id = "1"
        data.challenge_id = challengeId
        data.questions = questions

        dataManager.apiHelper.apiGameLevelOver(
            data,
            ApiCallbackHelper(livedata(lifecycleOwner, "apiGameLevelOver"))
        )
    }

    fun apiGameLevelUp(
        challengeId: String?,
        questions: ArrayList<GameResponseQuestion>,
        lifecycleOwner: LifecycleOwner
    ) {
        val data = GameLevelUpRequestItem()
        data.category_id = "1"
        data.challenge_id = challengeId
        data.questions = questions
        dataManager.apiHelper.apiGameLevelUp(
            data,
            ApiCallbackHelper(livedata(lifecycleOwner, "apiGameLevelUp"))
        )
    }

    fun apiGamePauseUserState(
        levelId: String,
        score: String,
        hearts: String,
        challengeId: String?,
        questions: ArrayList<GameResponseQuestion>,
        lifecycleOwner: LifecycleOwner
    ) {
        val data = GamePauseUserStateRequestItem()
        data.category_id = "1"
        data.level_id = levelId
        data.challenge_id = challengeId
        data.score = score
        data.hearts = hearts
        data.questions = questions
        dataManager.apiHelper.apiGamePauseGameState(
            data,
            ApiCallbackHelper(livedata(lifecycleOwner, "apiGameState"))
        )
    }

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

    fun apiHeartsUpdate(
        type: String,
        lifecycleOwner: LifecycleOwner
    ) {
        dataManager.apiHelper.apiHeartsUpdate(
            type,
            ApiCallbackHelper(livedata(lifecycleOwner, "apiHeartsUpdate"))
        )
    }

    fun insertGameHeart(gameHeartDTO: GameHeartDTO) {
        dataManager.databaseHelper.executeDBOperation(
            dataManager.roomHelper.getDatabase().gameHeartDao().insert(gameHeartDTO),
            "insertGameHeart",
            iDatabaseCallBack
        )
    }

    fun deleteAllGameHeartData() {
        val userId = dataManager.mPref.prefGetUserInfo().id.toInt()
        dataManager.databaseHelper.executeDBOperation(
            dataManager.roomHelper.getDatabase().gameHeartDao().delete(userId),
            "deleteAllGameHeartData",
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

    fun getAllGameHeartData() {
        val userId = dataManager.mPref.prefGetUserInfo().id.toInt()
        dataManager.databaseHelper.executeDBOperation(
            dataManager.roomHelper.getDatabase().gameHeartDao().getAll(userId),
            "getAllGameHeartData",
            iDatabaseCallBack
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

}