package com.gmpire.guruklub.data.model.question

import com.gmpire.guruklub.MyApp
import com.gmpire.guruklub.data.local_db.dto.QuestionDTO
import com.gmpire.guruklub.data.prefence.PreferencesHelper
import java.io.Serializable

data class Question(
    var localId : Int =-1,
    var answer: Int = -1,
    val answer_explain: String = "",
    val difficulty: String = "",
    val id: String = "",
    val topic_id: String = "",
    val section_id: String = "",
    val subject_id: String = "",
    val options: ArrayList<String> = arrayListOf(),
    val picture: String? = "",
    val title: String = "",
    var is_bookmarked: Int = 0,
    var is_math: Int = 0,
    var answered: Boolean = false,
    var answered_position: Int = -1,
    var answer_type: String? = "",
    var question_category: Int = -1,
    var has_image : Int = 0,
    var batches : ArrayList<String> = arrayListOf()

) : Serializable {
    companion object {
        fun toQuestionDTOs(questions: List<Question>): List<QuestionDTO> {
            var questionDTOs = arrayListOf<QuestionDTO>()
            questions.forEach { questionDTOs.add(it.toQuestionDTO()) }
            return questionDTOs
        }
    }

    fun toQuestionDTO(): QuestionDTO {
        return QuestionDTO(
            0,
            answer,
            answer_explain,
            difficulty,
            id,
            topic_id,
            section_id,
            subject_id,
            options,
            picture,
            title,
            is_bookmarked,
            is_math,
            answered,
            answered_position,
            answer_type,
            question_category,
            PreferencesHelper(MyApp.getInstance()).prefGetUserInfo().id,
            has_image,
            batches?: arrayListOf()
        )
    }
}