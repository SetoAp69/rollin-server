package com.rollinup.server.service.user

import com.auth0.jwt.JWT
import com.rollinup.server.datasource.database.repository.user.UserRepository
import com.rollinup.server.model.request.user.RegisterEditUserRequest
import com.rollinup.server.service.jwt.JWTService
import com.rollinup.server.service.security.HashingService

class UserServiceImpl(
    private val hashingService: HashingService,
    private val tokenService: JWTService,
    private val userRepository: UserRepository
) : UserService {
    override fun registerUser(registerEditUserRequest: RegisterEditUserRequest) {
        val saltedHash = hashingService.generateSaltedHash(
            value = registerEditUserRequest.password,
        )

        val hashedData = registerEditUserRequest.copy(
            password = saltedHash.value,
            salt = saltedHash.salt
        )

        if (hashedData.isInvalid) {
            throw Exception("Invalid form data")
        }
        userRepository.registerUser(hashedData)
    }

    override fun editUser(
        editRequest: RegisterEditUserRequest,
        id: String
    ) {
        val user = userRepository.getUserById(id)
        val saltedHash = hashingService.generateSaltedHash(
            value = editRequest.password
        )

        val hashedPassword: String
        val salt: String

        if (editRequest.password.isNotBlank()) {
            hashedPassword = saltedHash.value
            salt = saltedHash.salt
        } else {
            hashedPassword = ""
            salt = ""
        }

        val editUser = editRequest.copy(
            password = hashedPassword,
            salt = salt
        )


        if (user == null) {
            throw Exception("User not found")
        }

        userRepository.editUser(
            editRequest = editUser,
            id = id
        )

    }

    override fun resetPassword(token: String, newPassword: String) {
        val isTokenValid = tokenService.validateToken(token)
        if (!isTokenValid) {
            throw Exception("Token Expired")
        }

        val id = JWT.decode(token).getClaim("id").asString()

        val saltedHash = hashingService.generateSaltedHash(newPassword)

        val editRequest = RegisterEditUserRequest(
            password = saltedHash.value,
            salt = saltedHash.salt
        )

        val user = userRepository.getUserById(token)

        if (user == null) {
            throw Exception("User Not Found")
        }

        userRepository.editUser(
            editRequest = editRequest,
            id = user.id
        )
    }

    override fun resetPasswordRequest(usernameOrEmail: String): String {
        //This is for getting reset request and then send the token via email, idk how thoo
        val user = userRepository.getUserByEmailOrUsername(usernameOrEmail)
        if (user == null)
            throw Exception("User Not Found")

        val censoredEmail =
            user.email.replaceRange(3, user.email.length - 3, "*".repeat(user.email.length - 6))

        return censoredEmail
    }

}