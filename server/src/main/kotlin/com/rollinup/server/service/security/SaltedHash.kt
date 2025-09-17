package com.rollinup.server.service.security

data class SaltedHash(
    val value: String = "",
    val salt: String = ""
)
