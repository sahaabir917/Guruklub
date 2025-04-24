package com.gmpire.guruklub.data.model.game

data class GameResultResponse(
    val correct_answer: Int,
    val game_date: String,
    val game_id: Int,
    val get_point: Double,
    val section_based_performance: List<SectionBasedPerformance>,
    val subject_based_performance: List<SubjectBasedPerformance>,
    val topic_based_performance: List<TopicBasedPerformance>,
    val total_point: String,
    val total_question: String,
    val total_time: Int,
    val un_answer: Int,
    val wrong_answer: Int
)