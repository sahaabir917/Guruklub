package com.gmpire.guruklub.data.model.gameChallenge

import com.gmpire.guruklub.data.model.gamequestions.GameQuestionSet

data class GameMilestoneChallengeResponse(
    var total_questions: String,
    var challenge: GameChallenge?,
    var questions: List<GameQuestionSet>
)