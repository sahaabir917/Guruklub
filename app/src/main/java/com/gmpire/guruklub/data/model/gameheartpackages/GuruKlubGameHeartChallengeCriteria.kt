package com.gmpire.guruklub.data.model.gameheartpackages

import com.google.gson.annotations.SerializedName

enum class GuruKlubGameHeartChallengeCriteria(value: Int) {
    @SerializedName("1")
    MARATHON(1),

    @SerializedName("2")
    CLOCK_SHOOT(2),

    @SerializedName("3")
    COUNT_DOWN(3)
}