package com.gmpire.guruklub.data.model.gamelevel


import java.io.Serializable

data class Level(
    val correct_points: String,
    val level: String,
    val id: String,
    val question_time: String,
    val math_question_time: String,
    var reward_hearts: String,
    val target_points: String,
    val wrong_points: String,
    val position: Int
) : Serializable
