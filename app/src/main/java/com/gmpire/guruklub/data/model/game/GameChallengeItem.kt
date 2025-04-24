package com.gmpire.guruklub.data.model.game

import java.io.Serializable

data class GameChallengeItem(
    val id: String,
    val name: String,
    val position: String,
    val type: String,
    val question_number: String = "0",
    val game_time: String = "0",
    val correct: String = "0",
    val wrong: String = "0",
    val total_point: String = "0",
    val tag: String = "0",
    val game_type_id: String = "0",
    val setting_status: String = "0",
    val status: String = "0",
    val hearts_cost :String = "0"
) : Serializable