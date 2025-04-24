package com.gmpire.guruklub.data.local_db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.gmpire.guruklub.data.local_db.dao.*
import com.gmpire.guruklub.data.local_db.dto.*
import com.gmpire.guruklub.data.local_db.entity.Category
import com.gmpire.guruklub.data.local_db.typeConverter.ConvertListToString


@Database(
    entities = [Category::class, SubjectDTO::class, SectionDTO::class, TopicDTO::class, QuestionDTO::class, GameHeartDTO::class],
    version = 8,
    exportSchema = false
)
@TypeConverters(ConvertListToString::class)
abstract class RoomDB : RoomDatabase() {

    abstract fun categoryDao(): CategoryDao
    abstract fun subjectDao(): SubjectDao
    abstract fun sectionDao(): SectionDao
    abstract fun topicDao(): TopicDao
    abstract fun questionDao(): QuestionDao
    abstract fun gameHeartDao(): GameHeartDao
}