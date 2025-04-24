package com.gmpire.guruklub.data.model.gamelevel


import com.gmpire.guruklub.data.model.question.GameResponseQuestion
import java.io.Serializable


data class GameUserState(
    val id: String?,
    val hearts: String,
    val level_id: String,
    val score: String,
    val user_id: String,
    var level: Level? = null,
    var answeredQuestions: MutableList<GameResponseQuestion> = mutableListOf()
) : Serializable