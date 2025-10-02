//package com.rollinup.server.datasource.database.repository.user
//
//import com.rollinup.server.datasource.database.dao.user.UserDao
//import com.rollinup.server.datasource.database.model.user.UserEntity
//import com.rollinup.server.datasource.database.table.RoleTable
//import com.rollinup.server.datasource.database.table.UserTable
//import com.rollinup.server.model.request.user.RegisterEditUserRequest
//import com.rollinup.server.model.request.user.UserQueryParams
//import com.rollinup.server.util.Utils
//import org.jetbrains.exposed.v1.core.ResultRow
//
//class UserRepositoryASDASDImpl(
//    private val dao: UserDao,
//) : UserRepositoryASDASD {
//
//    override suspend fun getAllUsers(queryParams: UserQueryParams): List<UserEntity> {
//        return
//            dao.getAllUsers(queryParams).map { it.toUser() }
//
//    }
//
//    override suspend fun getUserById(id: String): UserEntity? {
//        return Utils.dbQuery {
//            dao.getUserById(id).map {
//                it.toUser()
//            }.firstOrNull()
//        }
//    }
//
//    override suspend fun getUserByEmailOrUsername(emailOrUsername: String): UserEntity? {
//        return Utils.dbQuery {
//            dao.getUserByEmailOrUsername(
//                emailOrUsername = emailOrUsername
//            ).map {
//                it.toUser()
//            }.firstOrNull()
//        }
//    }
//
//    override suspend fun resetPassword(
//        id: String,
//        newPassword: String,
//        salt: String
//    ) {
//        Utils.dbQuery {
//            dao.resetPassword(
//                id = id,
//                newPassword = newPassword,
//                salt = salt,
//            )
//        }
//    }
//
//    override suspend fun registerUser(createRequest: RegisterEditUserRequest) {
//        dao.createUser(createRequest)
//    }
//
//    override suspend fun editUser(editRequest: RegisterEditUserRequest, id: String) {
//        Utils.dbQuery { dao.editUser(editRequest, id) }
//    }
//
//    private fun ResultRow.toUser(): UserEntity {
//        return UserEntity(
//            id = this[UserTable.user_id].toString(),
//            userName = this[UserTable.username],
//            email = this[UserTable.email],
//            firstName = this[UserTable.firstName],
//            lastName = this[UserTable.lastName],
//            role = this[RoleTable.name],
//            gender = this[UserTable.gender].name,
//            password = this[UserTable.password],
//            salt = this[UserTable.salt]
//        )
//    }
//}