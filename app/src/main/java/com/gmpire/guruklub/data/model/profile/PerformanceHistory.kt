package com.gmpire.guruklub.data.model.profile

data class PerformanceHistory(
    val game_id: String,
    val challenge_id: String,
    val title: String,
    val total_question: String,
    val correct_question: String,
    val performance: String,
    val wrong_question: String,
    val answred_question: Int,
    val acquired_point: String,
    val game_date: String)