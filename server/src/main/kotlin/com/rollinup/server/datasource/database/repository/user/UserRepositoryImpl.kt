package com.rollinup.server.datasource.database.repository.user

import com.rollinup.server.datasource.database.model.user.Gender
import com.rollinup.server.datasource.database.model.user.UserEntity
import com.rollinup.server.datasource.database.table.RoleTable
import com.rollinup.server.datasource.database.table.UserTable
import com.rollinup.server.datasource.database.table.UserTable.gender
import com.rollinup.server.datasource.database.table.UserTable.role
import com.rollinup.server.model.Role
import com.rollinup.server.model.request.user.EditUserRequest
import com.rollinup.server.model.request.user.RegisterUserRequest
import com.rollinup.server.model.request.user.UserQueryParams
import org.jetbrains.exposed.v1.core.JoinType
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.compoundOr
import org.jetbrains.exposed.v1.core.or
import org.jetbrains.exposed.v1.core.statements.UpsertSqlExpressionBuilder.inList
import org.jetbrains.exposed.v1.jdbc.andWhere
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.update
import java.util.UUID

class UserRepositoryImpl : UserRepository {
    override fun getAllUsers(queryParams: UserQueryParams): List<UserEntity> {
        val query = UserTable.join(
            otherTable = RoleTable,
            joinType = JoinType.LEFT,
            additionalConstraint = {
                role eq RoleTable._id
            }
        ).selectAll()

        val searchField = UserTable.searchField + RoleTable.searchField
        val sortField = UserTable.sortField + RoleTable.sortField
        val useOffset = listOf(queryParams.page, queryParams.limit).all { it != null && it != 0 }
        val offset = if (useOffset) queryParams.page!! * queryParams.limit!!.toLong() else null

        val stringFilterField = mapOf(
            RoleTable.name to queryParams.role
        ).filterValues { it != null }

        if (queryParams.gender != null) {
            query.andWhere {
                gender inList queryParams.gender.map { Gender.fromValue(it) }
            }
        }

        stringFilterField.forEach {
            query.andWhere {
                it.key inList it.value.orEmpty()
            }
        }


        if (!queryParams.search.isNullOrBlank()) {
            query.andWhere {
                searchField.map {
                    (it like "%${queryParams.search}%")
                }.compoundOr()
            }
        }

        if (!queryParams.sortOrder.isNullOrBlank()) {
            sortField[queryParams.sortBy]?.let {
                query.orderBy(it to SortOrder.valueOf(queryParams.sortOrder))
            }
        }

        if (useOffset) {
            query
                .limit(queryParams.limit!!)
                .offset(offset!!)
        }

        return query.map { it.toUser() }
    }


    override fun createUser(request: RegisterUserRequest) {

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

    override fun editUser(request: EditUserRequest, id: String) {
        val uuid = UUID.fromString(id)

        UserTable.update(
            where = {
                UserTable.user_id eq uuid
            },
            body = { statement ->
                with(request) {
                    firstName?.let { statement[UserTable.firstName] = it }
                    lastName?.let { statement[UserTable.lastName] = it }
                    email?.let { statement[UserTable.email] = it }
                    role?.let { statement[UserTable.role] = UUID.fromString(it) }
                    gender?.let { statement[UserTable.gender] = Gender.fromValue(it) }
                    deviceId?.let { statement[UserTable.device] = it }
                }
            }
        )
    }

    override fun deleteUser(id: List<String>) {
        val uuid = id.map { UUID.fromString(it) }
        UserTable.deleteWhere {
            UserTable.user_id inList uuid
        }
    }


    override fun getUserById(id: String): UserEntity? {
        val query = UserTable.join(
            otherTable = RoleTable,
            joinType = JoinType.LEFT,
            additionalConstraint = {
                role eq RoleTable._id
            }
        ).selectAll()
            .where {
                (UserTable.user_id eq UUID.fromString(id))
            }

        return query.firstOrNull()?.toUser()
    }

    override fun getUserByEmailOrUsername(emailOrUsername: String): UserEntity? {
        val query = UserTable.join(
            otherTable = RoleTable,
            joinType = JoinType.LEFT,
            additionalConstraint = {
                role eq RoleTable._id
            }
        )
            .selectAll()
            .where {
                UserTable.username eq emailOrUsername or (UserTable.email eq emailOrUsername)
            }


        return query.firstOrNull()?.toUser()
    }


    override fun resetPassword(id: String, newPassword: String, salt: String) {
        val uuid = UUID.fromString(id)

        UserTable.update(
            where = {
                UserTable.user_id eq uuid
            },

            ) { statement ->
            statement[UserTable.password] = newPassword
            statement[UserTable.salt] = salt

        }

    }

    private fun ResultRow.toUser(): UserEntity {
        return UserEntity(
            id = this[UserTable.user_id].toString(),
            userName = this[UserTable.username],
            email = this[UserTable.email],
            firstName = this[UserTable.firstName],
            lastName = this[UserTable.lastName],
            role = Role.fromValue(this[RoleTable.name]) ?: Role.STUDENT,
            gender = this[gender].name,
            password = this[UserTable.password],
            salt = this[UserTable.salt],
            device = this[UserTable.device],
        )
    }
}