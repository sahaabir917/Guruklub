package com.gmpire.guruklub.data.model.question

import java.io.Serializable

data class GameResponseQuestion (
    var status: Int = -1,
    var id: String,
    val topic_id: String,
    val section_id: String,
    val subject_id: String,
    var time: Long = 0L,
    var answer: Int = -1,
    var correctAnswer: Int = -1
): Serializable