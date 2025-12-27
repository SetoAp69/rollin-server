package com.rollinup.server.datasource.database.repository.verification

import com.rollinup.server.datasource.database.model.verification.VerificationTokenEntity
import java.time.Instant

interface VerificationTokenRepository {
    fun getTokenByUser(id: String): VerificationTokenEntity?

    fun createToken(id: String, token: String, salt: String, expiredAt: Instant)

    fun deleteToken(id: String)
}