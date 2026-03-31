package com.rollinup.server.datasource.database.repository.verification

import com.rollinup.server.datasource.database.model.verification.VerificationTokenEntity

interface VerificationTokenRepository {
    fun getTokenByUser(id: String): VerificationTokenEntity?

    fun createToken(id: String, token: String, salt: String)

    fun deleteToken(id: String)
}