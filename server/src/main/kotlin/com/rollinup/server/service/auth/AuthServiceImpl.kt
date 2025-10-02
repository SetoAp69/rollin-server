package com.rollinup.server.service.auth

import com.rollinup.server.CommonException
import com.rollinup.server.Constant
import com.rollinup.server.datasource.database.repository.refreshtoken.RefreshTokenRepository
import com.rollinup.server.datasource.database.repository.user.UserRepository
import com.rollinup.server.mapper.AuthMapper
import com.rollinup.server.model.request.auth.LoginRequest
import com.rollinup.server.model.response.Response
import com.rollinup.server.model.response.auth.LoginResponse
import com.rollinup.server.model.response.auth.RefreshTokenResponse
import com.rollinup.server.service.jwt.TokenClaim
import com.rollinup.server.service.jwt.TokenService
import com.rollinup.server.service.security.HashingService
import com.rollinup.server.service.security.SaltedHash
import com.rollinup.server.util.Config
import com.rollinup.server.util.Message
import com.rollinup.server.util.notFoundException
import com.rollinup.server.util.successCreateResponse
import com.rollinup.server.util.suspendTransaction

class AuthServiceImpl(
    private val hashingService: HashingService,
    private val jwtService: TokenService,
    private val userRepository: UserRepository,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val authMapper: AuthMapper
) : AuthService {

    override suspend fun login(loginRequest: LoginRequest): Response<LoginResponse> =
        suspendTransaction {
            val user = userRepository.getUserByEmailOrUsername(loginRequest.username)
                ?: throw "User".notFoundException()

            val saltedHash = SaltedHash(
                value = user.password,
                salt = user.salt
            )

            val isVerified = hashingService.verify(
                value = loginRequest.password,
                saltedHash = saltedHash
            )

            if (!isVerified) {
                throw CommonException(Message.INVALID_USERNAME_OR_PASSWORD)
            }

            val accessToken = jwtService.generateToken(
                config = Config.getTokenConfig().copy(expiresIn = Constant.ACCESS_TOKEN_DURATION),
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
                    value = user.role.value
                )
            )

            val refreshToken = jwtService.generateToken(
                config = Config.getTokenConfig().copy(expiresIn = Constant.REFRESH_TOKEN_DURATION),
                TokenClaim(
                    name = "id",
                    value = user.id
                )
            )

            val loginResponse = authMapper.mapLoginResponse(
                data = user,
                accessToken = accessToken,
                refreshToken = refreshToken
            )

            return@suspendTransaction Response(
                status = 200,
                message = Message.LOGIN_SUCCESS,
                data = loginResponse
            )
        }

    override suspend fun refreshToken(token: String): Response<RefreshTokenResponse> =
        suspendTransaction {
            val isTokenValid = jwtService.validateToken(token, Config.getTokenConfig())

            val user = refreshTokenRepository.findUserId(token)
                ?: throw "User".notFoundException()

            if (!isTokenValid) {
                refreshTokenRepository.dropToken(token)
                throw CommonException(Message.INVALID_TOKEN)
            }

            val accessToken = jwtService.generateToken(
                config = Config.getTokenConfig().copy(expiresIn = Constant.ACCESS_TOKEN_DURATION),
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
                    value = user.role.value
                )
            )

            return@suspendTransaction Response(
                status = 201,
                message = "Access Token".successCreateResponse(),
                data = RefreshTokenResponse(
                    accessToken = accessToken
                )
            )
        }


}