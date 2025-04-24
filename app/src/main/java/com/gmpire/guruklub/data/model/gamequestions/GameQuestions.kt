package com.gmpire.guruklub.data.model.gamequestions


import java.io.Serializable


data class GameQuestions(
    val question_time: String,
    val questions: List<GameQuestionSet>,
    val total_questions: Int
) : Serializable