package com.gmpire.guruklub.data.model

import java.io.Serializable

class BaseModel<z>() : Serializable {
    var status = false
    var status_code : Int? = null
    var token : String? = null
    var message: ArrayList<String> = arrayListOf()
    var new_notification : Int = 0
    var data: z? = null
}