package com.gmpire.guruklub.data.model.profile.performance

class SubjectWisePerformance(
    var subject_id: String,
    var name: String?,
    var total_question: String?,
    var correct_answer: String?,
    val performance_percent: Double ?
) {
}