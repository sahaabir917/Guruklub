package com.gmpire.guruklub.data.model.gameusersettings

import java.io.Serializable

data class UserSettings(
    var id: Int,
    var user_share_limit: String? = null,
    var user_ads_limit: String? = null,
    var ads_free: String? = null,
    var game_sound: Int,
    var game_background_music: Int,
    var login_count: String? = null,
    var user_practice_hearts_limit : String,
    var corr_ans_consecutive_count : String
) : Serializable {
    fun hasGameSound() = game_sound == 1
    fun hasBackgroundSound() = game_background_music == 1
}

