package com.rollinup.server.datasource.database.dao.auth

import com.rollinup.server.model.response.auth.User
import com.rollinup.server.datasource.database.table.RoleTable
import com.rollinup.server.datasource.database.table.UserTable
import com.rollinup.server.model.auth.UserCreateEditRequest
import com.rollinup.server.model.auth.UserQueryParams
import org.jetbrains.exposed.v1.core.JoinType
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.or
import org.jetbrains.exposed.v1.core.statements.UpsertSqlExpressionBuilder.eq
import org.jetbrains.exposed.v1.core.statements.UpsertSqlExpressionBuilder.inList
import org.jetbrains.exposed.v1.core.statements.UpsertSqlExpressionBuilder.like
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

class AuthDaoImpl() : UserDao {

    private fun ResultRow.toDTO(): User {
        return User(
            id = this[UserTable._id].toString(),
            userName = this[UserTable.username],
            email = this[UserTable.email],
            firstName = this[UserTable.firstName],
            lastName = this[UserTable.lastName],
            role = this[RoleTable.name],
            gender = this[UserTable.gender].name,
        )
    }

    override fun basicAuth(
        userName: String
    ): User? {
        return transaction {
            UserTable.join(
                otherTable = RoleTable,
                joinType = JoinType.LEFT,
                additionalConstraint = {
                    UserTable.role eq RoleTable._id
                }
            ).select(
                column =
                    ((UserTable.username eq userName) or (UserTable.email eq userName))
            ).map { it.toDTO() }.firstOrNull()
        }
    }

    override fun getAllUsers(queryParams: UserQueryParams): List<User> =
        transaction {
            UserTable.join(
                otherTable = RoleTable,
                joinType = JoinType.LEFT,
                additionalConstraint = {
                    UserTable.role eq RoleTable._id
                }
            ).select(
                columns = buildList {
                    queryParams.role?.let {
                        add(RoleTable.name inList it)
                    }
                    queryParams.search?.let {
                        if (it.isNotBlank()) {
                            listOf(
                                RoleTable.name,
                                UserTable.username,
                                UserTable.address
                            ).map { field ->
                                add(field like it)
                            }
                        }
                    }

                }
            ).map { it.toDTO() }
        }

    override fun createUser(request: UserCreateEditRequest) {

    }

    override fun editUser(request: UserCreateEditRequest) {

    }

    override fun deleteUser(id: String) {

    }

    override fun getUserById(id: String): User? {
        return null
    }

}