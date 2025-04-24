package com.gmpire.guruklub.data.model.login

data class LoginResponse(
    val `data`: UserInfo,
    val message: List<String>,
    val status: Boolean,
    val status_code: Int,
    val token: String
)