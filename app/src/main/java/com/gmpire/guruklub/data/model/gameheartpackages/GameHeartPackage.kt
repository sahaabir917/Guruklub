package com.gmpire.guruklub.data.model.gameheartpackages


import java.io.Serializable

data class GameHeartPackage(
    val active: String,
    val days: String,
    val hearts: String?,
    val id: String,
    val price: String,
    val type: String,
    var switchOnOff:Boolean = false
):Serializable
