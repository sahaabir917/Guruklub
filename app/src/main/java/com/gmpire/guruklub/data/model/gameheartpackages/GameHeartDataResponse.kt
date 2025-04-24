package com.gmpire.guruklub.data.model.gameheartpackages

import com.gmpire.guruklub.data.model.gameChallenge.GameChallenge
import com.gmpire.guruklub.data.model.gamelevel.Level

class GameLevelUpResponse {
    val challenge: ArrayList<GameChallenge> = arrayListOf()
    val rewarded_hearts: String? = null
    val milestone_bonus_hearts: Int = 0
    val new_level: Level? = null
    val current_hearts: Int = 0

    fun totalRewardHearts(): Int {
        return rewarded_hearts?.toInt()?.plus(milestone_bonus_hearts) ?: 0
    }
}