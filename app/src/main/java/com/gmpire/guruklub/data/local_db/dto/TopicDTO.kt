package com.gmpire.guruklub.data.local_db.dto

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.gmpire.guruklub.data.model.categoryAndSubject.Topic

@Entity(tableName = "Topic")
class TopicDTO(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "localId")
    var localId: Int = 0,
    var id: String? = "",
    var name: String? = "",
    var isSelected: Boolean? = false,
    var subject_id: String? = "",
    var subject_name: String? = "",
    var section_id: String? = "",
    var section_name: String? = ""
) {
    companion object {
        fun toTopics(topicDTOs: List<TopicDTO>): List<Topic> {
            var topics = arrayListOf<Topic>()
            topicDTOs.forEach { topics.add(it.toTopic()) }
            return topics
        }
    }

    fun toTopic(): Topic {
        val topic = Topic()
        topic.id = id
        topic.name = name
        topic.isSelected = isSelected ?: false
        topic.subject_id = subject_id
        topic.subject_name = subject_name
        topic.section_id = section_id
        topic.section_name = section_name
        return topic
    }
}