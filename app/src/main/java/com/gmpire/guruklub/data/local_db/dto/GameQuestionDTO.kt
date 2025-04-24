package com.gmpire.guruklub.data.local_db.dto

import androidx.room.*
import com.gmpire.guruklub.data.local_db.typeConverter.ConvertListToString
import com.gmpire.guruklub.data.model.gamequestions.GameQuestionSet
import com.gmpire.guruklub.data.model.question.Question
import com.gmpire.guruklub.util.ConstantField


/**
 * Created by Tahsin Rahman on 6/8/20.
 */

@Entity(tableName = "GameQuestion")
class GameQuestionDTO(
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
    var is_math: Int?,
    var has_image: Int?,
    val game_question_level: String,
    val only_game: String,
    val tag: String?
) {

    companion object {
        fun toQuestions(questionDTOs: List<GameQuestionDTO>): List<GameQuestionSet> {
            val questions = arrayListOf<GameQuestionSet>()
            questionDTOs.forEach { questions.add(it.toQuestion()) }
            return questions
        }
    }

    fun toQuestion(): GameQuestionSet {
        return GameQuestionSet(
            answer ?: -1,
            answer_explain ?: "",
            difficulty.toString(),
            game_question_level,
            has_image.toString(),
            id.toString(),
            is_math.toString(),
            only_game,
            options?.toList() ?: listOf(),
            picture ?: "",
            section_id.toString(),
            subject_id.toString(),
            tag ?: "",
            title.toString(),
            topic_id.toString()
        )
    }

}