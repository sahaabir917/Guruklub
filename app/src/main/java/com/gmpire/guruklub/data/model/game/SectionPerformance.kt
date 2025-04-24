package com.gmpire.guruklub.data.model.game

data class SectionPerformance(
    val correct_ans_by_section: String,
    val question_per_section: String,
    val section_answer: String,
    val section_game_time: String,
    val section_id: String,
    val section_name: String,
    val section_percent: Double,
    val topic_performance: List<TopicPerformance>
)