package com.gmpire.guruklub.data.model.modelTest

import java.io.Serializable

data class ModelTest(
    val category_id: String,
    val created_at: String,
    val created_by: String,
    val date: String,
    val duration: String,
    val id: String,
    val published_by: String,
    val status: String,
    val syllabus: String,
    val title: String,
    val total_question: String,
    val update_by: String,
    var register: Int,
    var updateFromMobileRes: Int,
    var exam_fees: Float,
    var tempStartOrResumeText: String,
    var is_participated: Int,
    var is_completed: Int,
    var hearts_cost :Int
) : Serializable