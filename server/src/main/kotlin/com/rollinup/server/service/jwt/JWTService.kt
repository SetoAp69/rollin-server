package com.rollinup.server.service.jwt

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTVerificationException
import java.util.Date

class JWTService : TokenService {
    override fun generateToken(
        config: TokenConfig,
        vararg claim: TokenClaim
    ): String {
        var token = JWT.create()
            .withAudience(config.audience)
            .withIssuer(config.issuer)
            .withExpiresAt(Date(System.currentTimeMillis() + config.expiresIn))
        claim.forEach { claim ->
            token = token.withClaim(claim.name, claim.value)
        }
        return token.sign(Algorithm.HMAC256(config.secret))
    }

    override fun validateToken(token: String, config: TokenConfig): Boolean {
        try {
            val algorithm = Algorithm.HMAC256(config.secret)
            val verificator = JWT.require(algorithm)
                .withIssuer(config.issuer)
                .withAudience(config.audience)
                .build()

            val decoded = verificator.verify(token)
            val isExpired = decoded.expiresAt.before(Date())

            return !isExpired
        } catch (e: JWTVerificationException) {
            return false
        }
    }
}