package com.gmpire.guruklub.data.local_db.dto

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.gmpire.guruklub.data.local_db.typeConverter.ConvertListToString
import com.gmpire.guruklub.data.model.question.Question


/**
 * Created by Tahsin Rahman on 6/8/20.
 */

@Entity(tableName = "Question")
class QuestionDTO(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "localId")
    var localId: Int = 0,
    val answer: Int?,
    val answer_explain: String?,
    val difficulty: String?,
    val id: String?,
    val topic_id: String?,
    val section_id: String?,
    val subject_id: String?,
    @TypeConverters(ConvertListToString::class)
    val options: ArrayList<String>?,
    val picture: String?,
    val title: String?,
    var is_bookmarked: Int?,
    var is_math: Int?,
    var answered: Boolean? = false,
    var answered_position: Int? = -1,
    val answer_type: String?,
    val question_category: Int?,
    val user_id: String?,
    var has_image: Int?,
    @TypeConverters(ConvertListToString::class)
    var batches: ArrayList<String>?
) {

    companion object {
        fun toQuestions(questionDTOs: List<QuestionDTO>): List<Question> {
            var questions = arrayListOf<Question>()
            questionDTOs.forEach { questions.add(it.toQuestion()) }
            return questions
        }
    }

    fun toQuestion(): Question {
        return Question(
            localId,
            answer ?: -1,
            answer_explain.toString(),
            difficulty.toString(),
            id.toString(),
            topic_id.toString(),
            section_id.toString(),
            subject_id.toString(),
            options ?: arrayListOf(),
            picture,
            title.toString(),
            is_bookmarked ?: 0,
            is_math ?: 0,
            answered ?: false,
            answered_position ?: -1,
            answer_type,
            question_category ?: -1,
            has_image ?: 0,
            batches ?: arrayListOf()
        )
    }

}