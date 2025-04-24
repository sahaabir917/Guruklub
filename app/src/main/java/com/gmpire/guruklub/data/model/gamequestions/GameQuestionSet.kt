package com.gmpire.guruklub.data.model.gamequestions


import com.gmpire.guruklub.data.local_db.dto.GameQuestionDTO
import com.gmpire.guruklub.data.local_db.dto.QuestionDTO
import com.gmpire.guruklub.data.model.question.Question
import java.io.Serializable


data class GameQuestionSet(
    val answer: Int,
    val answer_explain: String,
    val difficulty: String,
    val game_question_level: String,
    val has_image: String,
    val id: String,
    val is_math: String,
    val only_game: String,
    val options: List<String>,
    val picture: String,
    val section_id: String,
    val subject_id: String,
    val tag: String,
    val title: String,
    val topic_id: String
) : Serializable {

    companion object {
        fun toGameQuestionDTOs(questions: List<GameQuestionSet>): List<GameQuestionDTO> {
            val questionDTOs = arrayListOf<GameQuestionDTO>()
            questions.forEach { questionDTOs.add(it.toGameQuestionDTO()) }
            return questionDTOs
        }
    }

    fun toGameQuestionDTO(): GameQuestionDTO {
        return GameQuestionDTO(
            0,
            answer,
            answer_explain,
            difficulty,
            id,
            topic_id,
            section_id,
            subject_id,
            ArrayList(options),
            picture,
            title,
            is_math.toInt(),
            has_image.toInt(),
            game_question_level,
            only_game,
            tag
        )
    }
}