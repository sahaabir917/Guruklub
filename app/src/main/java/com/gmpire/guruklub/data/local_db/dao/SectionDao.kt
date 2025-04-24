package com.gmpire.guruklub.data.local_db.dao

import androidx.room.*
import androidx.sqlite.db.SimpleSQLiteQuery
import com.gmpire.guruklub.data.local_db.dto.SectionDTO

@Dao
interface SectionDao {

    @Query("SELECT * FROM Section")
    fun getAll(): List<SectionDTO>

    @RawQuery
    fun getAllBySubjectIds(query: SimpleSQLiteQuery): List<SectionDTO>

    @Query("SELECT * FROM Section where localId=:localId")
    fun getAllById(localId: Int): List<SectionDTO>

    @Insert
    fun insertAll(sections: List<SectionDTO>): List<Long>

    @Insert
    fun insert(section: SectionDTO)

    @Query("DELETE FROM Section")
    fun deleteAll(): Int

    @Delete
    fun delete(sections: List<SectionDTO>): Int

    @Delete
    fun delete(section: SectionDTO): Int
}