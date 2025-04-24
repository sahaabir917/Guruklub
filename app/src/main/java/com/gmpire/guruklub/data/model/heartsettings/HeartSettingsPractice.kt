package com.gmpire.guruklub.data.model.heartsettings

import com.google.gson.annotations.SerializedName


/**
 * Created by Tahsin Rahman on 27/6/21.
 */


data class HeartSettingsPractice(
    @SerializedName("practice-random-hearts")
    var practice_random_hearts: String,
    @SerializedName("practice-hearts")
    var practice_hearts: String
)