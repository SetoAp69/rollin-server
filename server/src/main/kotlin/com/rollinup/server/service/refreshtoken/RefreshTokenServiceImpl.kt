package com.rollinup.server.service.refreshtoken

import com.rollinup.server.ExpiredTokenExceptions
import com.rollinup.server.datasource.database.repository.refreshtoken.RefreshTokenRepository
import com.rollinup.server.service.jwt.TokenClaim
import com.rollinup.server.service.jwt.TokenService
import com.rollinup.server.util.Utils

class RefreshTokenServiceImpl(
    private val tokenService: TokenService,
    private val refreshTokenRepository: RefreshTokenRepository
) : RefreshTokenService {
    val tokenConfig = Utils.getTokenConfig()

    override fun refreshToken(token: String): String {

        val isTokenValid = tokenService.validateToken(token)
        val userId = refreshTokenRepository.findUserId(token)

        if (!isTokenValid || userId.isNullOrBlank()) throw ExpiredTokenExceptions

        return userId
    }

    override fun generateToken(id: String): String {
        val expiresIn = System.currentTimeMillis() + 86_400_000

        val token = tokenService.generateToken(
            config = tokenConfig.copy(
                expiresIn = expiresIn
            ),
            TokenClaim(
                name = "id",
                value = id
            ),
        )

        refreshTokenRepository.save(
            token = token,
            id = id
        )

        return token
    }


}