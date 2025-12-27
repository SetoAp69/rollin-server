package com.rollinup.server.datasource.database.model.resetpassword

import java.time.Instant

data class ResetPasswordEntity(
    val id: String = "",
    val token: String = "",
    val expiredAt: Instant = Instant.now(),
    val salt: String = ""
)
