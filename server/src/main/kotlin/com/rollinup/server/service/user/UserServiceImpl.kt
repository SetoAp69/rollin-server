package com.rollinup.server.service.user

import com.auth0.jwt.JWT
import com.rollinup.server.CommonException
import com.rollinup.server.Constant
import com.rollinup.server.InvalidTokenExceptions
import com.rollinup.server.datasource.database.repository.resetpassword.ResetPasswordRepository
import com.rollinup.server.datasource.database.repository.user.UserRepository
import com.rollinup.server.mapper.UserMapper
import com.rollinup.server.model.request.user.EditUserRequest
import com.rollinup.server.model.request.user.RegisterUserRequest
import com.rollinup.server.model.request.user.UserQueryParams
import com.rollinup.server.model.response.Response
import com.rollinup.server.model.response.user.GetAllUserResponse
import com.rollinup.server.model.response.user.ResetPasswordRequestResponse
import com.rollinup.server.model.response.user.ValidateResetOtpResponse
import com.rollinup.server.service.email.EmailService
import com.rollinup.server.service.jwt.TokenClaim
import com.rollinup.server.service.jwt.TokenService
import com.rollinup.server.service.security.HashingService
import com.rollinup.server.service.security.SaltedHash
import com.rollinup.server.util.Config
import com.rollinup.server.util.Message
import com.rollinup.server.util.Utils
import com.rollinup.server.util.Utils.toLocalDateTime
import com.rollinup.server.util.manager.TransactionManager
import com.rollinup.server.util.successGettingResponse
import com.rollinup.server.util.toCensoredEmail
import java.time.ZoneOffset

class UserServiceImpl(
    private val userRepository: UserRepository,
    private val resetPasswordRepository: ResetPasswordRepository,
    private val hashingService: HashingService,
    private val tokenService: TokenService,
    private val emailService: EmailService,
    private val mapper: UserMapper,
    private val transactionManager: TransactionManager
) : UserService {
    override suspend fun registerUser(requestBody: RegisterUserRequest): Response<Unit> =
        transactionManager.suspendTransaction {
            val isEmailUsed = userRepository.getUserByEmailOrUsername(
                emailOrUsername = requestBody.email
            )

            val isUsernameUsed = userRepository.getUserByEmailOrUsername(
                emailOrUsername = requestBody.userName
            )

            when {
                isEmailUsed != null -> throw CommonException(Message.EMAIL_USED)
                isUsernameUsed != null -> throw CommonException(Message.USERNAME_USED)
            }

            userRepository.createUser(
                request = requestBody
            )

            return@suspendTransaction Response(
                status = 201,
                message = Message.CREATE_USER_SUCCESS,
                data = Unit
            )
        }

    override suspend fun editUser(
        requestBody: EditUserRequest,
        id: String
    ): Response<Unit> = transactionManager.suspendTransaction {
        val userData = userRepository.getUserById(id)

        if (userData == null) {
            throw CommonException(Message.USER_NOT_FOUND)
        }

        userRepository.editUser(
            request = requestBody,
            id = id
        )

        return@suspendTransaction Response(
            status = 200,
            message = Message.EDIT_USER_SUCCESS,
            data = Unit
        )
    }

    override suspend fun getAllUser(queryParams: UserQueryParams): Response<GetAllUserResponse> =
        transactionManager.suspendTransaction {
            val data = userRepository.getAllUsers(
                queryParams = queryParams
            )

            val response = mapper.mapGetAllUserResponse(
                data = data,
                queryParams = queryParams
            )

            return@suspendTransaction Response(
                status = 200,
                message = "user".successGettingResponse(),
                data = response
            )
        }

    override suspend fun validateResetOtp(
        userNameOrEmail: String,
        otp: String
    ): Response<ValidateResetOtpResponse> = transactionManager.suspendTransaction {
        val user = userRepository.getUserByEmailOrUsername(userNameOrEmail)
            ?: throw CommonException(Message.USER_NOT_FOUND)

        val resetPasswordEntity = resetPasswordRepository.getToken(id = user.id)
            ?: throw CommonException(Message.INVALID_TOKEN)

        val currentTime = System.currentTimeMillis() / 1000
        val tokenExpiredAt = resetPasswordEntity.expiredAt
            .toLocalDateTime()
            .toEpochSecond(ZoneOffset.UTC)

        if (tokenExpiredAt < currentTime) {
            throw CommonException(Message.EXPIRED_TOKEN)
        }

        val saltedToken = SaltedHash(
            value = resetPasswordEntity.token,
            salt = resetPasswordEntity.salt
        )

        val isValid = hashingService.verify(
            value = otp,
            saltedHash = saltedToken
        )

        when (isValid) {
            false -> throw CommonException(Message.INVALID_TOKEN)
            true -> {
                val resetToken = tokenService.generateToken(
                    config = Config.getTokenConfig().copy(expiresIn = Constant.OTP_DURATION),
                    TokenClaim(
                        value = user.id,
                        name = "id"
                    )
                )

                Response(
                    status = 200,
                    message = Message.VALIDATE_OTP_SUCCESS,
                    data = mapper.mapValidateResetOtpResponse(resetToken)
                )
            }
        }


    }

    override suspend fun resetPasswordRequest(usernameOrEmail: String): Response<ResetPasswordRequestResponse> =
        transactionManager.suspendTransaction {
            val response = mapper.mapResetPasswordRequestResponse(
                email = usernameOrEmail.toCensoredEmail()
            )

            val user = userRepository.getUserByEmailOrUsername(usernameOrEmail)
                ?: return@suspendTransaction Response(
                    status = 200,
                    message = Message.EMAIL_SENT,
                    data = response
                )

            val existedToken = resetPasswordRepository.getToken(id = user.id)

            val isStillValid = existedToken != null &&
                    existedToken.expiredAt.toLocalDateTime()
                        .toEpochSecond(ZoneOffset.UTC) > System.currentTimeMillis() / 1000

            if (isStillValid) {
                throw CommonException(Message.EMAIL_ALREADY_SENT)
            }

            val otp = Utils.generateRandom(5)

            emailService.sendEmail(
                receiver = usernameOrEmail,
                message = "This is your reset password verification code : $otp , it's valid for 5 minutes",
                subject = "Reset Password"
            )

            val saltedToken = hashingService.generateSaltedHash(otp)

            resetPasswordRepository.saveToken(
                id = user.id,
                token = saltedToken.value,
                salt = saltedToken.salt
            )

            return@suspendTransaction Response(
                status = 200,
                message = Message.EMAIL_SENT,
                data = response
            )
        }

    override suspend fun resetPassword(
        token: String,
        newPassword: String
    ): Response<Unit> =
        transactionManager.suspendTransaction {
            val tokenClaim = tokenService.validateToken(
                token = token,
                config = Config.getTokenConfig()
            )

            if (!tokenClaim)
                throw InvalidTokenExceptions

            val id = JWT.decode(token).getClaim("id").asString()
            val user = userRepository.getUserById(id)
                ?: throw CommonException(Message.USER_NOT_FOUND)

            val saltedPassword = hashingService.generateSaltedHash(newPassword)

            userRepository.resetPassword(
                id = user.id,
                newPassword = saltedPassword.value,
                salt = saltedPassword.salt
            )

            return@suspendTransaction Response(
                status = 200,
                message = Message.EDIT_USER_SUCCESS,
                data = Unit
            )
        }
}