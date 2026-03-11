package com.rollinup.server.datasource.database.repository.user

import com.rollinup.server.datasource.database.model.user.Gender
import com.rollinup.server.datasource.database.model.user.UserEntity
import com.rollinup.server.datasource.database.table.ClassTable
import com.rollinup.server.datasource.database.table.RoleTable
import com.rollinup.server.datasource.database.table.UserTable
import com.rollinup.server.datasource.database.table.UserTable.gender
import com.rollinup.server.datasource.database.table.UserTable.role
import com.rollinup.server.model.request.user.EditUserRequest
import com.rollinup.server.model.request.user.RegisterUserRequest
import com.rollinup.server.model.request.user.UserQueryParams
import com.rollinup.server.model.response.OptionData
import com.rollinup.server.model.response.user.GetUserOptionsResponse
import com.rollinup.server.util.addFilter
import com.rollinup.server.util.addOffset
import org.jetbrains.exposed.v1.core.JoinType
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.compoundOr
import org.jetbrains.exposed.v1.core.lowerCase
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
        val query = UserTable
            .join(
                otherTable = RoleTable,
                joinType = JoinType.LEFT,
                additionalConstraint = {
                    role eq RoleTable._id
                }
            )
            .join(
                otherTable = ClassTable,
                joinType = JoinType.LEFT,
                onColumn = ClassTable._id,
                otherColumn = UserTable.classX
            )
            .selectAll()

        val searchField = UserTable.searchField + RoleTable.searchField
        val sortField = UserTable.sortField + RoleTable.sortField

        val stringFilterField = mapOf(
            RoleTable.key to queryParams.role,
            ClassTable.key to queryParams.classX
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

        query.addOffset(queryParams.limit, queryParams.page)

        return query.map { it.toUser() }
    }


    override fun createUser(request: RegisterUserRequest) {
        val classId = request.classX?.let {
            UUID.fromString(request.classX)
        }

        UserTable.insert { statement ->
            statement[username] = request.userName
            statement[studentId] = request.studentId
            statement[lastName] = request.fullname
            statement[email] = request.email
            statement[classX] = classId
            statement[password] = request.password
            statement[role] = UUID.fromString(request.role)
            statement[gender] = Gender.fromValue(request.gender)
            statement[address] = request.address
            statement[phoneNumber] = request.phoneNumber
            statement[birthDay] = request.birthdayDate
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
                    userName?.let { statement[UserTable.username] = it }
                    firstName?.let { statement[UserTable.firstName] = it }
                    lastName?.let { statement[UserTable.lastName] = it }
                    email?.let { statement[UserTable.email] = it }
                    role?.let { statement[UserTable.role] = UUID.fromString(it) }
                    gender?.let { statement[UserTable.gender] = Gender.fromValue(it) }
                    deviceId?.let { statement[UserTable.device] = it }
                    birthdayDate?.let { statement[UserTable.birthDay] = it }
                    studentId?.let { statement[UserTable.studentId] = it }
                    classX?.let { statement[UserTable.classX] = UUID.fromString(it) }
                    phoneNumber?.let { statement[UserTable.phoneNumber] = it }
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
        val query = UserTable
            .join(
                otherTable = RoleTable,
                joinType = JoinType.LEFT,
                additionalConstraint = {
                    role eq RoleTable._id
                }
            )
            .join(
                otherTable = ClassTable,
                onColumn = UserTable.classX,
                joinType = JoinType.LEFT,
                otherColumn = ClassTable._id
            )
            .selectAll()
            .where {
                (UserTable.user_id eq UUID.fromString(id))
            }

        return query.firstOrNull()?.toUser()
    }

    override fun checkDevice(deviceId: String): Boolean {
        val query = UserTable
            .selectAll()
            .where { UserTable.device eq deviceId }

        return query.firstOrNull() != null
    }

    override fun getUserByEmailOrUsername(emailOrUsername: String): UserEntity? {
        val query = UserTable.join(
            otherTable = RoleTable,
            joinType = JoinType.LEFT,
            additionalConstraint = {
                role eq RoleTable._id
            }
        )
            .join(
                otherTable = ClassTable,
                onColumn = UserTable.classX,
                joinType = JoinType.LEFT,
                otherColumn = ClassTable._id
            )
            .selectAll()
            .where {
                UserTable.username eq emailOrUsername or (UserTable.email.lowerCase() eq emailOrUsername.lowercase())
            }


        return query.firstOrNull()?.toUser()
    }

    override fun checkEmailOrUsername(email: String?, username: String?): Boolean {
        val query = UserTable
            .selectAll()

        query.addFilter(email) {
            andWhere {
                UserTable.email eq it
            }
        }

        query.addFilter(username) {
            andWhere {
                UserTable.username eq it
            }
        }

        return query.firstOrNull() == null
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

    override fun updatePasswordAndDevice(
        id: String,
        salt: String,
        password: String,
        deviceId: String?,
    ) {
        val uuid = UUID.fromString(id)
        UserTable.update(
            where = {
                UserTable.user_id eq uuid
            }
        ) { statement ->
            statement[UserTable.device] = deviceId
            statement[UserTable.password] = password
            statement[UserTable.salt] = salt
            statement[UserTable.isVerified] = true
        }
    }

    override fun getUserOptions(): GetUserOptionsResponse {
        val roleQuery = RoleTable
            .selectAll()
            .map { row ->
                OptionData(
                    label = row[RoleTable.name],
                    value = row[RoleTable.key],
                )
            }

        val roleIdQuery = RoleTable
            .selectAll()
            .map { row ->
                OptionData(
                    label = row[RoleTable.name],
                    value = row[RoleTable._id].toString()
                )
            }

        val classQuery = ClassTable
            .selectAll()
            .map { row ->
                OptionData(
                    label = row[ClassTable.name],
                    value = row[ClassTable.key]
                )
            }

        val classIdQuery = ClassTable
            .selectAll()
            .map { row ->
                OptionData(
                    label = row[ClassTable.name],
                    value = row[ClassTable._id].toString()
                )
            }

        return GetUserOptionsResponse(
            roles = roleQuery,
            classX = classQuery,
            rolesId = roleIdQuery,
            classId = classIdQuery,
        )
    }

    private fun ResultRow.toUser(): UserEntity {
        return UserEntity(
            id = this[UserTable.user_id].toString(),
            userName = this[UserTable.username],
            email = this[UserTable.email],
            firstName = this[UserTable.firstName],
            lastName = this[UserTable.lastName],
            role = UserEntity.Role(
                id = this[RoleTable._id].toString(),
                key = this[RoleTable.key],
                name = this[RoleTable.name]
            ),
            gender = this[gender].name,
            password = this[UserTable.password],
            salt = this[UserTable.salt],
            device = this[UserTable.device],
            studentId = this[UserTable.studentId],
            phoneNumber = this[UserTable.phoneNumber],
            classX = this.getOrNull(ClassTable._id)?.let {
                UserEntity.Class(
                    id = it.toString(),
                    key = this[ClassTable.key],
                    name = this[ClassTable.name],
                    grade = this[ClassTable.grade]
                )
            },
            isVerified = this[UserTable.isVerified],
            address = this[UserTable.address],
            birthday = this[UserTable.birthDay].toString(),
        )
    }
}