package com.rollinup.server.service.jwt

data class TokenConfig(
    val realm: String,
    val issuer: String,
    val audience: String,
    val expiresIn: Long,
    val secret: String
)
