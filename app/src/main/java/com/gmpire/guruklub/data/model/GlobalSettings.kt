package com.gmpire.guruklub.data.model

import com.gmpire.guruklub.data.model.heartsettings.HeartSettingsPractice

data class GlobalSettings(
    var ads_limit : String,
    var share_limit : String,
    var practice_hearts_limit : String,
    var practice_hearts_range : String = "0",
    var hearts_settings : HeartSettingsPractice
)