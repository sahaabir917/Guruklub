package com.gmpire.guruklub.data.model.viewSolution

import com.gmpire.guruklub.data.model.question.Question

data class GameSolutionResponse(
    val `data`: List<Question>?,
    val show_details : Int,
    val next_page: Int
)