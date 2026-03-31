package com.rollinup.server.datasource.database.model.resetpassword

import java.time.OffsetDateTime

data class ResetPasswordEntity(
    val id: String = "",
    val token: String = "",
    val expiredAt: OffsetDateTime = OffsetDateTime.now(),
    val salt: String = "",
)
