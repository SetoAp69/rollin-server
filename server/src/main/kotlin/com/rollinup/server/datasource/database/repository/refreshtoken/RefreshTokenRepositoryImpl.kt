package com.rollinup.server.datasource.database.repository.refreshtoken

import com.rollinup.server.datasource.database.model.user.UserEntity
import com.rollinup.server.datasource.database.table.RefreshTokenTable
import com.rollinup.server.datasource.database.table.RoleTable
import com.rollinup.server.datasource.database.table.UserTable
import com.rollinup.server.model.Role
import org.jetbrains.exposed.v1.core.JoinType
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.statements.UpsertSqlExpressionBuilder.eq
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.upsert
import java.util.UUID


class RefreshTokenRepositoryImpl() : RefreshTokenRepository {

    override fun save(token: String, id: String) {
        val uuid = UUID.fromString(id)

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

    override fun dropToken(token: String) {
        RefreshTokenTable.deleteWhere {
            RefreshTokenTable.token eq token
        }
    }

    override fun findUserId(token: String): UserEntity? {
        return RefreshTokenTable.join(
            otherTable = UserTable,
            joinType = JoinType.LEFT,
            additionalConstraint = {
                RefreshTokenTable.user_id eq UserTable.user_id
            }
        )
            .join(
                otherTable = RoleTable,
                joinType = JoinType.LEFT,
                additionalConstraint = {
                    UserTable.role eq RoleTable._id
                }
            )
            .selectAll()
            .where(RefreshTokenTable.token eq token)
            .firstOrNull()
            ?.toUser()


    }

    private fun ResultRow.toUser(): UserEntity {
        return UserEntity(
            id = this[UserTable.user_id].toString(),
            userName = this[UserTable.username],
            email = this[UserTable.email],
            firstName = this[UserTable.firstName],
            lastName = this[UserTable.lastName],
            role = Role.fromValue(this[RoleTable.name]) ?: Role.STUDENT,
            gender = this[UserTable.gender].name,
            password = this[UserTable.password],
            salt = this[UserTable.salt]
        )
    }
}