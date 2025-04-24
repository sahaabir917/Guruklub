package com.gmpire.guruklub.data.model.gamelevel

import com.gmpire.guruklub.data.model.GlobalSettings
import com.gmpire.guruklub.data.model.gameusersettings.UserSettings
import java.io.Serializable

data class GameUserInfo(
    var user_settings: UserSettings,
    var current_hearts: String? = null,
    var current_level: Level? = null,
    var resume: GameUserState? = null,
    var hearts_subscription: HeartSubscription? = null,
    var hearts_gift: HeartGift? = null,
    var global_settings: GlobalSettings? = null
) : Serializable