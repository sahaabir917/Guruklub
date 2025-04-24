package com.gmpire.guruklub.data.model.game

import com.gmpire.guruklub.data.model.question.Question

data class GameResponse(
    val game_time: String,
    val question: List<Question>,
    val question_number: String,
    val current_hearts : String?=null
)