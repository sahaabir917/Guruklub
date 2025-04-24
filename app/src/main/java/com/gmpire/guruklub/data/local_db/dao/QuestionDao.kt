package com.gmpire.guruklub.data.local_db.dao

import androidx.room.*
import com.gmpire.guruklub.MyApp
import com.gmpire.guruklub.data.local_db.dto.QuestionDTO
import com.gmpire.guruklub.data.prefence.PreferencesHelper
import com.gmpire.guruklub.util.ConstantField


/**
 * Created by Tahsin Rahman on 6/8/20.
 */

@Dao
interface QuestionDao {
    @Query("SELECT * FROM Question")
    fun getAll(): List<QuestionDTO>

    @Query("SELECT * FROM Question WHERE question_category =:category AND user_id =:userId ORDER BY localId DESC LIMIT 5")
    fun getFiveRecent(
        category: Int = ConstantField.QUESTION_TYPE_RECENT_SEARCH,
        userId: String = PreferencesHelper(MyApp.getInstance()).prefGetUserInfo().id
    ): List<QuestionDTO>

    @Query("SELECT * FROM Question WHERE question_category =:category ORDER BY localId")
    fun getAllTempQuestions(category: Int = ConstantField.QUESTION_TYPE_TEMP_LIST): List<QuestionDTO>

    @Query("SELECT * FROM Question WHERE question_category =:category ORDER BY Random()")
    fun getAllOfflineQuestions(category: Int = ConstantField.QUESTION_TYPE_OFFLINE): List<QuestionDTO>

    @Query("SELECT * FROM Question WHERE localId=:localId")
    fun getAllById(localId: Int): List<QuestionDTO>

    @Query("SELECT COUNT(*) FROM Question WHERE id=:serverId AND question_category =:category")
    fun checkIfAlreadyAdded(
        serverId: String,
        category: Int = ConstantField.QUESTION_TYPE_RECENT_SEARCH
    ): Int

    @Insert
    fun insertAll(questions: List<QuestionDTO>): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(question: QuestionDTO)

    @Query("DELETE FROM Question")
    fun delete(): Int

    @Query("SELECT COUNT(*) FROM Question")
    fun getRecentQuesCount(): Int

    @Query("DELETE FROM Question WHERE localId IN (SELECT localId FROM Question ORDER BY localId ASC LIMIT 1)")
    fun deleteOldest(): Int

    @Query("DELETE FROM Question WHERE question_category =:category")
    fun deleteAllOfflineQuestions(category: Int = ConstantField.QUESTION_TYPE_OFFLINE): Int

    @Query("DELETE FROM Question WHERE question_category =:category")
    fun deleteAllTempQuestions(category: Int = ConstantField.QUESTION_TYPE_TEMP_LIST): Int

    @Delete
    fun delete(questions: List<QuestionDTO>): Int

    @Delete
    fun delete(question: QuestionDTO): Int
}