package com.gmpire.guruklub.data.model.heartsettings

import java.io.Serializable

data class HeartsAddResponse(
   val rewarded_hearts: String,
   val current_hearts : String,
   val user_new_ads_limit : String
):Serializable