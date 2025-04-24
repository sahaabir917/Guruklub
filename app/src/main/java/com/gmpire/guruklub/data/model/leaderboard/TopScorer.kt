package com.gmpire.guruklub.data.model.leaderboard

import java.io.Serializable

data class TopScorer(
        val user_id : String,
        val name : String,
        val profile_pic : String,
        val score : String
) : Serializable


