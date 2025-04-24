package com.gmpire.guruklub.data.model.profile.performance

data class PerformanceSummery(
    var total_total_question:String,
    var total_correct_question:String?,
    var total_wrong_question:String?,
    var total_unanswer_question:String?,
    val best_performance:BestPerformance?,
    val total_get_point: String?,
    val total_game_played: String?,
    val worst_performance:WrostPerformance?,
    val recent_game_id:String?
)