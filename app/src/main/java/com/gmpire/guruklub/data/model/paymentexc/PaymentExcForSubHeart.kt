package com.gmpire.guruklub.data.model.paymentexc

import java.io.Serializable

data class PaymentExcForSubHeart(
        var purchase_date :String,
        var expiry_date : String,
        var status :String ,
        var id : String ,
        var price : String,
        var hearts :String,
        var days :String ,
        var active : String,
        var type :String
):Serializable