package com.gmpire.guruklub.data.local_db

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

class RoomHelper(context: Context) {

    private var context = context

    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL(
                "CREATE TABLE `Question` (`localId` INTEGER NOT NULL DEFAULT 0 , " +
                        "`answer` INTEGER, " +
                        "`answer_explain` TEXT, " +
                        "`difficulty` TEXT, " +
                        "`id` TEXT, " +
                        "`topic_id` TEXT, " +
                        "`section_id` TEXT, " +
                        "`subject_id` TEXT, " +
                        "`options` TEXT, " +
                        "`picture` TEXT, " +
                        "`title` TEXT, " +
                        "`is_bookmarked` INTEGER, " +
                        "`is_math` INTEGER, " +
                        "`answered` INTEGER DEFAULT 0 , " +
                        "`answered_position` INTEGER DEFAULT -1 , " +
                        "`answer_type` TEXT, " +
                        "PRIMARY KEY(`localId`))"
            )
        }
    }

    val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL(
                "ALTER TABLE Question ADD COLUMN question_category INTEGER"
            )
        }
    }

    val MIGRATION_3_4 = object : Migration(3, 4) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL(
                "ALTER TABLE Question ADD COLUMN user_id TEXT"
            )
        }
    }

    val MIGRATION_4_5 = object : Migration(4, 5) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL(
                "ALTER TABLE Question ADD COLUMN has_image INTEGER"
            )
        }
    }

    val MIGRATION_5_6 = object : Migration(5, 6) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL(
                "CREATE TABLE `GameHearts` (`localId` INTEGER NOT NULL DEFAULT 0 , " +
                        "`user_id` INTEGER NOT NULL  DEFAULT 0, " +
                        "`heart_type` INTEGER NOT NULL  DEFAULT 0, " +
                        "PRIMARY KEY(`localId`))"
            )
        }
    }

    val MIGRATION_6_7 = object : Migration(6, 7) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL(
                "ALTER TABLE GameHearts ADD COLUMN practice TEXT"
            )
        }
    }

    val MIGRATION_7_8 = object : Migration(7, 8) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL(
                "ALTER TABLE Question ADD COLUMN batches TEXT"
            )
        }
    }

    private val db =
        Room.databaseBuilder(context, RoomDB::class.java, "BD_NAME").allowMainThreadQueries()
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7,MIGRATION_7_8)
            .build()


    fun getDatabase(): RoomDB {
        return db
    }


}