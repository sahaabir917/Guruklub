package com.gmpire.guruklub.data.local_db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.gmpire.guruklub.data.local_db.dto.SubjectDTO
import com.gmpire.guruklub.data.model.library.CountCheck

@Dao
interface SubjectDao {

    @Query("SELECT * FROM Subject")
    fun getAll(): List<SubjectDTO>

    @Query("SELECT * FROM Subject where localId=:localId")
    fun getAllById(localId: Int): List<SubjectDTO>

    @Insert
    fun insertAll(subjects: List<SubjectDTO>): List<Long>

    @Query("SELECT COUNT(*) FROM Subject")
    fun checkIfDataAvailable(): Int

    @Insert
    fun insert(subject: SubjectDTO)

    @Query("DELETE FROM Subject")
    fun deleteAll(): Int

    @Query(
        """SELECT (
                       SELECT COUNT(*)
                      FROM   Subject
                     ) AS subC,
                     ( 
                    SELECT COUNT(*)
                      FROM   Section
                    ) AS secC,
                     ( 
                    SELECT COUNT(*)
                      FROM   Topic
                    ) AS topC
                """
    )
    fun checkTest(): CountCheck

    @Delete
    fun delete(subjects: List<SubjectDTO>): Int

    @Delete
    fun delete(subject: SubjectDTO): Int
}