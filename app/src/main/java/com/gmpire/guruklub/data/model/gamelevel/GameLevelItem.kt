package com.gmpire.guruklub.data.model.gamelevel

import com.gmpire.guruklub.data.model.gameChallenge.GameChallenge
import java.io.Serializable

data class GameLevelItem(
    val challenge: List<GameChallenge>,
    val levels: List<Level>,
    val my_level_id: Int,
    val my_hearts: String,
    val resume: GameUserState
) : Serializable