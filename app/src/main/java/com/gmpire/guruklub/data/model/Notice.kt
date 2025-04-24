package com.gmpire.guruklub.data.model

import java.io.Serializable

data class Notice(
    val id: Int,
    val category_id: Int,
    val title: String,
    val description: String,
    val category: String,
    val picture: String?,
    val notify_description :String?,
    val notify_icon : String?,
    val button_text : String?
) : Serializable