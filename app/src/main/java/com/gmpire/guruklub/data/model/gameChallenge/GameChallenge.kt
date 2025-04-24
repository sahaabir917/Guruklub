package com.gmpire.guruklub.data.model.gameChallenge

import com.gmpire.guruklub.data.model.gameheartpackages.GuruKlubGameHeartChallengeCriteria
import java.io.Serializable

data class GameChallenge
    (
    val id: Int,
    val title: String,
    val description: String,
    val target_points: Float,
    val challenge_time: Long,
    val wrong_points: Float,
    val correct_points: Float,
    val reward_hearts: Int,
    val question_time: Long?,
    val math_question_time: Long?,
    val question_number: Int,
    val criteria_type: GuruKlubGameHeartChallengeCriteria,
    val milestone_type: Int,
    val milestone_score: String? = null,
    val level_id: Int,
    val rule_type: String
) : Serializable {

    fun getId(score: Float): String? {
        if (score >= milestone_score?.toFloat() ?: 0f) {
            return id.toString()
        }
        return null
    }

    fun getMilestoneScore(): Float {
        return milestone_score?.toFloat() ?: 0f
    }

}
