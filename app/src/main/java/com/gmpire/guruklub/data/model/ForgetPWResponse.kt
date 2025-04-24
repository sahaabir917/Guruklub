package com.gmpire.guruklub.data.model


/**
 * Created by Tahsin Rahman on 26/9/20.
 */


class ForgetPWResponse {
    var status = false
    var status_code: Int? = null
    var token: String? = null
    lateinit var message: ArrayList<String>
    var email_status: Int = 0
    var code: Int = 0
}