package com.rollinup.server.service.jwt

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
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

    override fun validateToken(token: String): Boolean {
        val jwt = JWT.decode(token)
        val isExpired = jwt.expiresAt.before(Date())
        return !isExpired
    }
}