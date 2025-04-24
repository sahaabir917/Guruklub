package com.gmpire.guruklub.data.model.categoryAndSubject

import com.gmpire.guruklub.data.local_db.dto.TopicDTO

class Topic(
    var section_id: String? = "",
    var section_name: String? = ""
) : Section() {
    companion object {
        fun toTopicDTOs(topics: List<Topic>): List<TopicDTO> {
            var topicDTOs = arrayListOf<TopicDTO>()
            topics.forEach { topicDTOs.add(it.toTopicDTO()) }
            return topicDTOs
        }
    }

    fun toTopicDTO(): TopicDTO {
        return TopicDTO(
            0, id, name, isSelected, subject_id, subject_name, section_id, section_name
        )
    }

}