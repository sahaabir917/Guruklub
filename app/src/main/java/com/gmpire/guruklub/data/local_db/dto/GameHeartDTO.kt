package com.gmpire.guruklub.data.local_db.dto

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "GameHearts")
class GameHeartDTO(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "localId")
    var localId: Int = 0,
    var user_id: Int,
    var heart_type: Int,
    var practice: String?
)