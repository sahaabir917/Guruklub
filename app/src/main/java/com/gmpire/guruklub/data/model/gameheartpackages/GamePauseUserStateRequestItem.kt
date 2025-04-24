package com.gmpire.guruklub.data.model.gameheartpackages

import com.gmpire.guruklub.data.model.gameChallenge.GameChallenge
import com.gmpire.guruklub.data.model.question.GameResponseQuestion
import okhttp3.Challenge
import java.io.Serializable

class GamePauseUserStateRequestItem : Serializable {
    var score: String? = null
    var category_id: String? = null
    var challenge_id: String? = null
    var level_id: String? = null
    var hearts: String? = null
    var questions: ArrayList<GameResponseQuestion>? = null
}

class GameLevelUpRequestItem : Serializable {
    var category_id: String? = null
    var challenge_id: String? = null
    var questions: ArrayList<GameResponseQuestion>? = null
}

class GameLevelOverRequestItem : Serializable {
    var category_id: String? = null
    var challenge_id: String? = null
    var questions: ArrayList<GameResponseQuestion>? = null
}

class GameChallengeState : Serializable {
    var score: Float = 0f
    var categoryId: String? = null
    var questions: MutableList<GameResponseQuestion> = mutableListOf()
}
