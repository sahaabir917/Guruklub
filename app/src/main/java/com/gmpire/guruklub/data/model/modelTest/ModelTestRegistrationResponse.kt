package com.gmpire.guruklub.data.model.modelTest

data class ModelTestRegistrationResponse(
    val duration: String,
    val model_type_id: String,
    val model_test_id: String,
    val question: List<ModelTestQuestion>,
    val question_number: String,
    var current_position:Int,
    val current_hearts : Int?=null
)