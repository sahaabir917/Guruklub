package com.gmpire.guruklub.data.local_db.dao

import androidx.room.*
import com.gmpire.guruklub.MyApp
import com.gmpire.guruklub.data.local_db.dto.GameQuestionDTO
import com.gmpire.guruklub.data.local_db.dto.QuestionDTO
import com.gmpire.guruklub.data.prefence.PreferencesHelper
import com.gmpire.guruklub.util.ConstantField


/**
 * Created by Tahsin Rahman on 6/8/20.
 */

@Dao
interface GameQuestionDao {
    @Query("SELECT * FROM GameQuestion")
    fun getAll(): List<GameQuestionDTO>

    @Query("SELECT * FROM GameQuestion WHERE localId=:localId")
    fun getAllById(localId: Int): List<GameQuestionDTO>

    @Insert
    fun insertAll(questions: List<GameQuestionDTO>): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(question: GameQuestionDTO)

    @Query("DELETE FROM Question")
    fun delete(): Int

    @Query("SELECT COUNT(*) FROM Question")
    fun getRecentQuesCount(): Int

    @Delete
    fun delete(questions: List<GameQuestionDTO>): Int

    @Delete
    fun delete(question: GameQuestionDTO): Int
}