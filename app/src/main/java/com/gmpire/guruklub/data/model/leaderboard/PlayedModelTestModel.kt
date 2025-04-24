package com.gmpire.guruklub.data.model.leaderboard

import java.io.Serializable

data class PlayedModelTestModel(
    val id: String,
    val title: String,
    var isSelected:Boolean = false,
    val total_participate: Int,
    val top_scorer : List<TopScorer>
) : Serializable
