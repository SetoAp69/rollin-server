package com.rollinup.server.service.refreshtoken

import com.rollinup.server.ExpiredTokenExceptions
import com.rollinup.server.datasource.database.repository.refreshtoken.RefreshTokenRepository
import com.rollinup.server.service.jwt.JWTService
import com.rollinup.server.service.jwt.TokenClaim
import com.rollinup.server.service.jwt.TokenService
import com.rollinup.server.util.Utils

class RefreshTokenServiceImpl(
    private val tokenService: JWTService,
    private val refreshTokenRepository: RefreshTokenRepository
) : RefreshTokenService {
    val tokenConfig = Utils.getTokenConfig()

    override fun refreshToken(token: String): String {

        val isTokenValid = tokenService.validateToken(token)
        val user = refreshTokenRepository.findUserId(token)
        val expiresIn = System.currentTimeMillis()

        if (!isTokenValid || user == null) {
            if ((!isTokenValid)) {
                refreshTokenRepository.dropToken(token)
            }
            throw ExpiredTokenExceptions
        }

        val accessToken = tokenService.generateToken(
            config = Utils.getTokenConfig().copy(
                expiresIn = expiresIn + 600_000
            ),
            TokenClaim(
                name = "id",
                value = user.id
            ),
            TokenClaim(
                name = "username",
                value = user.userName
            ),
            TokenClaim(
                name = "email",
                value = user.email
            ),
            TokenClaim(
                name = "role",
                value = user.roles
            )
        )

        return accessToken
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