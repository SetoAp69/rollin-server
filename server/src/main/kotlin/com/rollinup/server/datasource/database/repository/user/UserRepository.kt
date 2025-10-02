package com.rollinup.server.datasource.database.repository.user

import com.rollinup.server.datasource.database.model.user.UserEntity
import com.rollinup.server.model.request.user.EditUserRequest
import com.rollinup.server.model.request.user.RegisterUserRequest
import com.rollinup.server.model.request.user.UserQueryParams

interface UserRepository {

    fun getAllUsers(queryParams: UserQueryParams): List<UserEntity>

    fun createUser(request: RegisterUserRequest)

    fun editUser(request: EditUserRequest, id: String)

    fun deleteUser(id: List<String>)

    fun getUserById(id: String): UserEntity?

    fun getUserByEmailOrUsername(emailOrUsername: String): UserEntity?

    fun resetPassword(id: String, newPassword: String, salt: String)


}