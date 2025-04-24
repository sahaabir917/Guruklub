package com.gmpire.guruklub.data.model.library

import java.io.Serializable

data class Common(
    val id: String,
    val name: String,
    val exam_year: String = "0",
    val total_views_percentage : String = "0"
) : Serializable