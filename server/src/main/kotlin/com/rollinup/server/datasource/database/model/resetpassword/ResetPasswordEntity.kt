package com.rollinup.server.datasource.database.model.resetpassword

data class ResetPasswordEntity(
    val id: String = "",
    val token: String = "",
    val expiredAt: String = "",
    val salt: String = ""
)
