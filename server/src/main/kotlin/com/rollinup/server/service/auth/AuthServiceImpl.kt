package com.rollinup.server.service.auth

import com.rollinup.server.datasource.database.model.user.UserDTO
import com.rollinup.server.datasource.database.repository.user.UserRepository
import com.rollinup.server.model.auth.LoginRequest
import com.rollinup.server.model.auth.UserQueryParams
import com.rollinup.server.service.jwt.JWTService
import com.rollinup.server.service.jwt.TokenClaim
import com.rollinup.server.service.refreshtoken.RefreshTokenService
import com.rollinup.server.service.security.HashingService
import com.rollinup.server.service.security.SaltedHash
import com.rollinup.server.util.Utils

class AuthServiceImpl(
    private val hashingService: HashingService,
    private val refreshTokenService: RefreshTokenService,
    private val jwtService: JWTService,
    private val userRepository: UserRepository
) : AuthService {
    override suspend fun basicAuth(authRequest: LoginRequest): UserDTO {
        val user = userRepository.basicAuth(authRequest.username)

        if (authRequest.password.isBlank() || authRequest.username.isBlank()) throw (Exception("empty username or password"))

        if (user == null) throw (Exception("User Not Found"))

        val isValidPassword = hashingService.verify(
            value = authRequest.password,
            saltedHash = SaltedHash(
                value = user.password,
                salt = user.salt
            )
        )

        if (!isValidPassword) throw (Exception("Invalid Password"))

        val accessToken = jwtService.generateToken(
            config = Utils.getTokenConfig().copy(
                expiresIn = System.currentTimeMillis() + 600_000
            ),
            TokenClaim(
                name = "id",
                value = user.id
            ),
            TokenClaim(
                name = "username",
                value = user.userName
            ),
            TokenClaim(
                name = "email",
                value = user.email
            ),
            TokenClaim(
                name = "role",
                value = user.role
            )
        )

        val refreshToken = refreshTokenService.generateToken(user.id)

        val userDto = user.toDTO().copy(
            accessToken = accessToken,
            refreshToken = refreshToken
        )

        return userDto

    }

    override suspend fun getAllUsers(queryParams: UserQueryParams): List<UserDTO> {
        val userData = userRepository.getAllUsers(queryParams).map {
            it.toDTO()
        }
        return userData
    }

    override suspend fun getUserById(id: String): UserDTO {
        if (id.isBlank()) throw (Exception("Empty ID"))

        val user = userRepository.getUserById(id)

        if (user == null) throw (Exception("User Not Found"))
        else return user.toDTO()
    }
}