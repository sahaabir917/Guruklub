package com.gmpire.guruklub.data.model.gamelevel

import java.io.Serializable

data class GameLevelChallenge(
   val id: String,
   val title: String,
   val target_points :String,
   val challenge_time: String,
   val wrong_points: String,
   val correct_points: String,
   val reward_hearts: String,
   val question_number:String,
   val criteria_type: String,
   val created_at: String,
   val level_id: String,
   val score: String
):Serializable