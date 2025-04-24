package com.gmpire.guruklub.data.model.modelTest

data class ModelTestQuestion(
    val answer: Int,
    var is_math: String,
    val options: List<String>,
    val picture: String?,
    val question_id: String,
    val section_id: String,
    val subject_id: String,
    val title: String,
    var time: Long = 0L,
    var answered : Boolean = false,
    var answered_position : String?,
    val topic_id: String,
    val has_image : String

)