package com.gmpire.guruklub.data.local_db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.gmpire.guruklub.data.local_db.dto.GameHeartDTO
import com.gmpire.guruklub.data.local_db.entity.Category

@Dao
interface GameHeartDao {
    @Query("SELECT * FROM GameHearts where user_id=:userId")
    abstract fun getAll(userId: Int): List<GameHeartDTO>

    @Query("SELECT * FROM GameHearts where localId=:localId")
    abstract fun getAllById(localId: Int): List<GameHeartDTO>

    @Insert
    abstract fun insert(gameHearts: List<GameHeartDTO>): List<Long>

    @Insert
    abstract fun insert(gameHeart: GameHeartDTO)

    @Query("DELETE FROM GameHearts where user_id=:userId")
    abstract fun delete(userId: Int): Int

    @Query("DELETE FROM GameHearts where user_id=:userId and localId=(SELECT MAX(localId) from GameHearts) ")
    abstract fun deleteLast(userId: Int): Int

    @Delete
    abstract fun delete(gameHearts: List<GameHeartDTO>): Int

    @Delete
    abstract fun delete(gameHeart: GameHeartDTO): Int
}