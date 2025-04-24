package com.gmpire.guruklub.data.model.adsubscribepaymentresponse



import java.io.Serializable


data class PaymentInfo(
    val amount: String,
    val id: String,
    val order_id: String,
    val payment_date: String,
    val status: String,
    val tnx_id: String
):Serializable