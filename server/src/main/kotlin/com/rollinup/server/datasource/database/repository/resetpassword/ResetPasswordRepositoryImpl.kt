package com.rollinup.server.datasource.database.repository.resetpassword

import com.rollinup.server.datasource.database.model.resetpassword.ResetPasswordEntity
import com.rollinup.server.datasource.database.table.ResetPasswordTokenTable
import com.rollinup.server.datasource.database.table.ResetPasswordTokenTable.userId
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.statements.UpsertSqlExpressionBuilder.eq
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.upsert
import java.util.UUID

class ResetPasswordRepositoryImpl() : ResetPasswordRepository {

    private fun ResultRow.toEntity(): ResetPasswordEntity {
        return ResetPasswordEntity(
            id = this[userId].toString(),
            token = this[ResetPasswordTokenTable.token],
            expiredAt = this[ResetPasswordTokenTable.expiredAt].toString(),
            salt = this[ResetPasswordTokenTable.salt]
        )
    }

    override fun getToken(id: String): ResetPasswordEntity? {
        val query = ResetPasswordTokenTable
            .selectAll()
            .where(
                userId eq UUID.fromString(id)
            )
            .firstOrNull()
            ?.toEntity()

        return query
    }

    override fun saveToken(id: String, token: String, salt: String) {

        ResetPasswordTokenTable.upsert(
            ResetPasswordTokenTable.userId,
            body = { statement ->
                statement[userId] = UUID.fromString(id)
                statement[ResetPasswordTokenTable.token] = token
                statement[ResetPasswordTokenTable.salt] = salt
            },
            where = {
                userId eq UUID.fromString(id)
            },
            onUpdate = { statement ->
                statement[ResetPasswordTokenTable.token] = token
                statement[ResetPasswordTokenTable.salt] = salt
            }
        )
    }

    override fun deleteToken(id: String) {
        ResetPasswordTokenTable.deleteWhere {
            userId eq UUID.fromString(id)
        }
    }
}
