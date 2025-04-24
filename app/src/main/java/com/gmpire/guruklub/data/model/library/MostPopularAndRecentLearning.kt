package com.gmpire.guruklub.data.model.library

data class MostPopularAndRecentLearning(
    val topic_id : Int,
    val subject_id : Int,
    val section_id : Int,
    val category_name: String,
    val subject_name: String,
    val section_name: String,
    val topic_name: String,
    val total_views: Int
)