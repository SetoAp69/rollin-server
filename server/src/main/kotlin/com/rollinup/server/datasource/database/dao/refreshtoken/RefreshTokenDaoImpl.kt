package com.rollinup.server.datasource.database.dao.refreshtoken

import com.rollinup.server.datasource.database.model.user.UserDTO
import com.rollinup.server.datasource.database.table.RefreshTokenTable
import com.rollinup.server.datasource.database.table.RoleTable
import com.rollinup.server.datasource.database.table.UserTable
import com.rollinup.server.model.response.auth.User
import org.jetbrains.exposed.v1.core.JoinType
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.SqlExpressionBuilder.eq
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.upsert
import java.util.UUID

class RefreshTokenDaoImpl() : RefreshTokenDao {

    private fun ResultRow.toUser(): User {
        return User(
            id = this[UserTable.user_id].toString(),
            userName = this[UserTable.username],
            email = this[UserTable.email],
            firstName = this[UserTable.firstName],
            lastName = this[UserTable.lastName],
            role = this[RoleTable.name],
            gender = this[UserTable.gender].name,
        )
    }


    override fun saveToken(token: String, id: String) {
        val uuid = UUID.fromString(id)

        transaction {
            RefreshTokenTable.upsert(
                RefreshTokenTable.user_id,
                onUpdate = {
                    it[RefreshTokenTable.token] = token
                },
                where = {
                    RefreshTokenTable.user_id eq uuid
                },
                body = {
                    it[user_id] = uuid
                    it[RefreshTokenTable.token] = token
                }
            )
        }
    }

    override fun dropToken(token: String) {
        transaction {
            RefreshTokenTable.deleteWhere {
                RefreshTokenTable.token eq token
            }
        }
    }

    override fun findUserByToken(token: String): UserDTO? {
        return transaction {
            RefreshTokenTable.join(
                otherTable = UserTable,
                joinType = JoinType.LEFT,
                additionalConstraint = {
                    RefreshTokenTable.user_id eq UserTable.user_id
                }
            )
                .selectAll()
                .where(RefreshTokenTable.token eq token)
                .map { it.toUser().toDTO() }.firstOrNull()

        }
    }
}