package com.gmpire.guruklub.data.model.gamequestions

import com.gmpire.guruklub.data.model.question.GameResponseQuestion

class GameChallengeFinishRequestItem(
    var challenge_id: String,
    var category_id: String,
    var questions: List<GameResponseQuestion>?
)