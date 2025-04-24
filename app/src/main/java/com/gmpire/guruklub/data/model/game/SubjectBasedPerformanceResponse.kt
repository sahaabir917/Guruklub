package com.gmpire.guruklub.data.model.game

data class SubjectBasedPerformanceResponse(
    val correct_ans_by_subject: String,
    val question_per_subject: String,
    val section_performance: List<SectionPerformance>,
    val subject_answer: String,
    val subject_game_time: String,
    val subject_id: String,
    val subject_name: String,
    val subject_percent: Double
)