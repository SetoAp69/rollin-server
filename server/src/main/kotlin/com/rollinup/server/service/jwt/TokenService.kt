package com.rollinup.server.service.jwt

interface TokenService {
    fun generateToken(
        config: TokenConfig,
        vararg claim: TokenClaim
    ):String

    fun validateToken(token: String, config: TokenConfig): Boolean
}