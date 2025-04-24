package com.gmpire.guruklub.data.model.leaderboard

data class LeaderBoardResponse(
    val leader_board: List<LeaderBoard>?,
    val my_position: MyPosition?,
    val next_page: Int
)