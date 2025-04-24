package com.gmpire.guruklub.data.model.heartsettings

import com.gmpire.guruklub.data.model.gameusersettings.UserSettings

data class HeartVariation(
    var current_hearts : String,
    var user_settings : UserSettings
)