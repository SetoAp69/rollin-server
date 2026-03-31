package com.rollinup.server.service.auth

import com.rollinup.server.CommonException
import com.rollinup.server.Constant
import com.rollinup.server.datasource.database.model.user.UserEntity
import com.rollinup.server.datasource.database.repository.refreshtoken.RefreshTokenRepository
import com.rollinup.server.datasource.database.repository.user.UserRepository
import com.rollinup.server.datasource.database.repository.verification.VerificationTokenRepository
import com.rollinup.server.mapper.AuthMapper
import com.rollinup.server.model.request.auth.LoginRequest
import com.rollinup.server.model.response.Response
import com.rollinup.server.model.response.auth.LoginResponse
import com.rollinup.server.model.response.auth.RefreshTokenResponse
import com.rollinup.server.service.email.EmailService
import com.rollinup.server.service.jwt.TokenClaim
import com.rollinup.server.service.jwt.TokenService
import com.rollinup.server.service.security.HashingService
import com.rollinup.server.service.security.SaltedHash
import com.rollinup.server.util.Config
import com.rollinup.server.util.Message
import com.rollinup.server.util.Utils
import com.rollinup.server.util.manager.TransactionManager
import com.rollinup.server.util.notFoundException
import com.rollinup.server.util.successCreateResponse
import java.time.OffsetDateTime

class AuthServiceImpl(
    private val hashingService: HashingService,
    private val jwtService: TokenService,
    private val userRepository: UserRepository,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val emailService: EmailService,
    private val verificationTokenRepository: VerificationTokenRepository,
    private val authMapper: AuthMapper,
    private val transactionManager: TransactionManager,
) : AuthService {

    override suspend fun login(loginRequest: LoginRequest): Response<LoginResponse> =
        transactionManager.suspendTransaction {
            val user = userRepository.getUserByEmailOrUsername(loginRequest.username)
                ?: throw "User".notFoundException()

            val saltedHash = SaltedHash(
                value = user.password,
                salt = user.salt
            )

            val isPasswordCorrect = hashingService.verify(
                value = loginRequest.password,
                saltedHash = saltedHash
            )

            if (!isPasswordCorrect) {
                throw CommonException(Message.INVALID_USERNAME_OR_PASSWORD)
            }

            if (user.isVerified.not()) {
                sendVerificationToken(user)
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
                    value = user.role.name
                ),
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

            refreshTokenRepository.save(token = refreshToken, id = user.id)

            return@suspendTransaction Response(
                status = 200,
                message = Message.LOGIN_SUCCESS,
                data = loginResponse
            )
        }

    override suspend fun refreshToken(token: String): Response<RefreshTokenResponse> =
        transactionManager.suspendTransaction {
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
                    value = user.role.name
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

    override suspend fun loginJWT(id: String): Response<LoginResponse> =
        transactionManager.suspendTransaction {
            val user = userRepository.getUserById(id)
                ?: throw "User".notFoundException()

            val loginResponse = authMapper.mapLoginResponse(
                data = user,
                accessToken = "",
                refreshToken = ""
            )

            return@suspendTransaction Response(
                status = 200,
                message = Message.LOGIN_SUCCESS,
                data = loginResponse
            )
        }

    private fun sendVerificationToken(
        user: UserEntity,
    ) {
        val existedOTP = verificationTokenRepository.getTokenByUser(user.id)
        val otpExpired = existedOTP?.let {
            it.expiredAt < OffsetDateTime.now()
        } ?: true

        val otp = Utils.generateRandom(5)
        val saltedToken = hashingService.generateSaltedHash(otp)

        if (otpExpired) {
            emailService.sendEmail(
                receiver = user.email,
                message = "This is your first time login on Rollin Up client, we require you to update your temporary password. Here's the OTP to update your password : $otp",
                subject = "First Time Login Verification"
            )

            verificationTokenRepository.createToken(
                id = user.id,
                token = saltedToken.value,
                salt = saltedToken.salt
            )
        }

    }
}