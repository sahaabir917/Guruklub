package com.gmpire.guruklub.data.model.login

import com.gmpire.guruklub.data.model.GlobalSettings
import com.gmpire.guruklub.data.model.gamelevel.HeartGift
import com.gmpire.guruklub.data.model.gamelevel.HeartSubscription
import com.gmpire.guruklub.data.model.gameusersettings.UserSettings
import toElipsisTxt

data class UserInfo(
    var category_id: String? = "",
    var cover_picture: Any = "",
    var display_name: String? = "",
    var dob: String? = "",
    var join_date: String? = "",
    var email: String = "",
    var email_status: String = "",
    var gender: String? = "",
    var id: String = "",
    var name: String? = "",
    var password: String = "",
    var phone: String? = "",
    var phone_status: String = "",
    var picture: String? = "",
    var status: String = "",
    var subject_id: String? = "",
    var section_id: String? = "",
    var topic_id: String? = "",
    var toc: String = "",
    var subject_names: String? = "",
    var section_names: String? = "",
    var topic_names: String? = "",
    var notification: Int = -1,
    var reg_type: String? = "",
    var user_game_level: String? = "",
    var user_hearts: String? = "",
    var user_score: String? = "",
    var user_settings: UserSettings? = null,
    var global_settings: GlobalSettings? = null,
    var hearts_subscription: HeartSubscription? = null,
    var hearts_gift: HeartGift? = null
) {
    fun userElipsName(): String {
        return name?.toElipsisTxt() ?: ""
    }
}