package com.gmpire.guruklub.data.model.adsubscribepaymentresponse

import java.io.Serializable


data class AdSubscribePaymentResponse(
    val payment_info: PaymentInfo,
    val success_url: String
) : Serializable


data class LiveExamPaymentResponse(
    val payment_info: PaymentInfo,
    val success_url: String
) : Serializable

data class LiveExamPaymentCompleteResponse(
    val id: Int,
    val user_id: Int,
    val model_test_id: Int,
    val order_id: Int,
    val purchase_date: String,
    val status: Int
)
