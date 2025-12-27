package com.rollinup.server.datasource.database.repository.verification

import com.rollinup.server.datasource.database.model.verification.VerificationTokenEntity
import com.rollinup.server.datasource.database.table.VerificationTokenTable
import org.jetbrains.exposed.v1.core.statements.UpsertSqlExpressionBuilder.eq
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.upsert
import java.time.Instant
import java.util.UUID

class VerificationTokenRepositoryImpl : VerificationTokenRepository {
    override fun getTokenByUser(id: String): VerificationTokenEntity? {
        val query = VerificationTokenTable
            .selectAll()
            .where { VerificationTokenTable.user_id eq UUID.fromString(id) }
            .firstOrNull()

        val token = query?.let { row ->
            VerificationTokenEntity.fromRow(row)
        }
        return token
    }

    override fun createToken(id: String, token: String, salt: String, expiredAt: Instant) {
        VerificationTokenTable.upsert(
            VerificationTokenTable.user_id,
            where = {
                VerificationTokenTable.user_id eq UUID.fromString(id)
            },
            onUpdate = { body ->
                body[VerificationTokenTable.token] = token
                body[VerificationTokenTable.salt] = salt
                body[VerificationTokenTable.expiredAt] = expiredAt
            }
        ) { body ->
            body[VerificationTokenTable.user_id] = UUID.fromString(id)
            body[VerificationTokenTable.token] = token
            body[VerificationTokenTable.salt] = salt
            body[VerificationTokenTable.expiredAt] = expiredAt
        }
    }

    override fun deleteToken(id: String) {
        VerificationTokenTable.deleteWhere {
            VerificationTokenTable.user_id eq UUID.fromString(id)
        }
    }
}