package com.gmpire.guruklub.data.model.game

data class TopicPerformance(
    val correct_ans_by_topic: String,
    val question_per_topic: String,
    val topic_answer: String,
    val topic_game_time: String,
    val topic_id: String,
    val topic_name: String,
    val topic_percent: Double
)