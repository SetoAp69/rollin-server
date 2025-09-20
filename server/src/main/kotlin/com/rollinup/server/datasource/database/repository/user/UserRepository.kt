package com.rollinup.server.datasource.database.repository.user

import com.rollinup.server.model.auth.UserQueryParams
import com.rollinup.server.model.request.user.RegisterEditUserRequest
import com.rollinup.server.model.response.auth.User

interface UserRepository {
     fun basicAuth(userName: String): User?

     fun getAllUsers(queryParams: UserQueryParams): List<User>

     fun getUserById(id: String): User?

     fun getUserByEmailOrUsername(emailOrUsername: String): User?

    fun resetPassword(emailOrUsername: String, newPassword: String)

    fun registerUser(createRequest: RegisterEditUserRequest)

    fun editUser(editRequest: RegisterEditUserRequest, id: String)

}