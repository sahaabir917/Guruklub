package com.gmpire.guruklub.data.model


/**
 * Created by Tahsin Rahman on 22/9/20.
 */


class Registration() {
    var status = false
    var status_code: Int? = null
    var token: String? = null
    lateinit var message: ArrayList<String>
    var email_status: Int = 0

}