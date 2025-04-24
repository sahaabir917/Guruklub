package com.gmpire.guruklub.data.model.heartsettings


import java.io.Serializable


data class HeartSettings(
    val created_at: String,
    val hearts: String,
    val id: String,
    val slug: String,
    val type: String
):Serializable