package com.rollinup.server.datasource.database.dao.auth

import com.rollinup.server.datasource.database.model.user.Gender
import com.rollinup.server.datasource.database.table.RoleTable
import com.rollinup.server.datasource.database.table.UserTable
import com.rollinup.server.model.auth.UserQueryParams
import com.rollinup.server.model.request.user.RegisterEditUserRequest
import com.rollinup.server.model.response.auth.User
import org.jetbrains.exposed.v1.core.JoinType
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.or
import org.jetbrains.exposed.v1.core.statements.UpsertSqlExpressionBuilder.eq
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.update
import java.util.UUID

class UserDaoImpl() : UserDao {

    private fun ResultRow.toDTO(): User {
        return User(
            id = this[UserTable.user_id].toString(),
            userName = this[UserTable.username],
            email = this[UserTable.email],
            firstName = this[UserTable.firstName],
            lastName = this[UserTable.lastName],
            role = this[RoleTable.name],
            gender = this[UserTable.gender].name,
            password = this[UserTable.password],
            salt = this[UserTable.salt]
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
            ).selectAll()
                .where { (UserTable.username eq userName) or (UserTable.email eq userName) }
                .map { it.toDTO() }.firstOrNull()
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
            )
                .selectAll()
                /*TODO : FIX THE SEARCH AND FILTERING QUERY*/
//                .where {
//                    queryParams.role?.let{
//                        (RoleTable.name inList it )
//                    }
////                    queryParams.search?.let {
////                        if (it.isNotBlank()) {
////                            listOf(
////                                RoleTable.name,
////                                UserTable.username,
////                                UserTable.address
////                            )
////                                .forEach { field ->
////                                    (UserTable.username like "%$it%")
////                                }
////                        }
////                    }
//                }
                .map { it.toDTO() }
        }

    override fun createUser(request: RegisterEditUserRequest) {
        transaction {
            UserTable.insert { statement ->
                statement[username] = request.userName
                statement[firstName] = request.firstName
                statement[lastName] = request.lastName
                statement[email] = request.email
                statement[password] = request.password
                statement[role] = UUID.fromString(request.role)
                statement[gender] = Gender.fromValue(request.gender)
                statement[address] = request.address
                statement[phoneNumber] = request.phoneNumber
                statement[salt] = request.salt
            }
        }
    }

    override fun editUser(request: RegisterEditUserRequest, id: String) {
        val uuid = UUID.fromString(id)
        transaction {
            UserTable.update(
                where = {
                    UserTable.user_id eq uuid
                },
                limit = 1,
                body = { statement ->
                    with(request) {
                        if (firstName.isNotBlank()) statement[UserTable.firstName] = firstName
                        if (lastName.isNotBlank()) statement[UserTable.lastName] = lastName
                        if (email.isNotBlank()) statement[UserTable.email] = email
                        if (password.isNotBlank()) statement[UserTable.password] = password
                        if (role.isNotBlank()) {
                            val uuid = UUID.fromString(request.role)
                            statement[UserTable.role] = uuid
                        }
                        if (gender.isNotBlank()) {
                            val gender = Gender.fromValue(request.gender)
                            statement[UserTable.gender] = gender
                        }
                    }
                }
            )
        }
    }

    override fun deleteUser(id: String) {
        transaction {
            val uuid = UUID.fromString(id)
            UserTable.deleteWhere {
                UserTable.user_id eq uuid
            }
        }
    }


    override fun getUserById(id: String): User? =
        transaction {
            UserTable.join(
                otherTable = RoleTable,
                joinType = JoinType.LEFT,
                additionalConstraint = {
                    UserTable.role eq RoleTable._id
                }
            ).select(
                UserTable.user_id eq UUID.fromString(id)
            ).firstOrNull()?.toDTO()
        }

    override fun getUserByEmailOrUsername(emailOrUsername: String): User? =
        transaction {
            UserTable.join(
                otherTable = RoleTable,
                joinType = JoinType.LEFT,
                additionalConstraint = {
                    UserTable.role eq RoleTable._id
                }
            ).select(
                UserTable.username eq emailOrUsername or (UserTable.email eq emailOrUsername)
            ).map { it.toDTO() }.firstOrNull()
        }

    override fun resetPassword(emailOrUsername: String, newPassword: String) {

    }
}