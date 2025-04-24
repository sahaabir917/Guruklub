package com.gmpire.guruklub.data.local_db.dao

import androidx.room.*
import androidx.sqlite.db.SimpleSQLiteQuery
import com.gmpire.guruklub.data.local_db.dto.TopicDTO

@Dao
interface TopicDao {

    @Query("SELECT * FROM Topic")
    abstract fun getAll(): List<TopicDTO>

    @RawQuery
    fun getAllBySectionIds(query: SimpleSQLiteQuery): List<TopicDTO>

    @Query("SELECT * FROM Topic where localId=:localId")
    abstract fun getAllById(localId: Int): List<TopicDTO>

    @Insert
    abstract fun insertAll(topics: List<TopicDTO>): List<Long>

    @Insert
    abstract fun insert(topic: TopicDTO)

    @Query("DELETE FROM Topic")
    abstract fun deleteAll(): Int

    @Delete
    abstract fun delete(topics: List<TopicDTO>): Int

    @Delete
    abstract fun delete(topic: TopicDTO): Int
}