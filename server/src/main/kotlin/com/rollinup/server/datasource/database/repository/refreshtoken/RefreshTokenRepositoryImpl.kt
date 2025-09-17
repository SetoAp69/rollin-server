package com.rollinup.server.datasource.database.repository.refreshtoken

import com.rollinup.server.datasource.database.dao.refreshtoken.RefreshTokenDao
import com.rollinup.server.datasource.database.table.RefreshTokenTable
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import java.util.UUID


class RefreshTokenRepositoryImpl() : RefreshTokenRepository {

    override fun save(token: String, id: String) {
        transaction {
            RefreshTokenDao
                .new {
                    user_id = UUID.fromString(id)
                    this.token = token
                }
        }
    }

    override fun dropToken(token: String) {
        transaction {
            RefreshTokenDao
                .find { RefreshTokenTable.token eq token }.firstOrNull()?.delete()

        }
    }

    override fun findUserId(token: String): String? {
        return transaction {
            RefreshTokenDao
                .find {
                    RefreshTokenTable.token eq token
                }.firstOrNull()?.toString()
        }
    }
}