package com.rollinup.server.service.user

import com.auth0.jwt.JWT
import com.rollinup.server.CommonException
import com.rollinup.server.Constant
import com.rollinup.server.InvalidTokenExceptions
import com.rollinup.server.datasource.database.model.verification.VerificationTokenEntity
import com.rollinup.server.datasource.database.repository.resetpassword.ResetPasswordRepository
import com.rollinup.server.datasource.database.repository.user.UserRepository
import com.rollinup.server.datasource.database.repository.verification.VerificationTokenRepository
import com.rollinup.server.mapper.UserMapper
import com.rollinup.server.model.Role
import com.rollinup.server.model.request.ListIdBody
import com.rollinup.server.model.request.user.EditUserRequest
import com.rollinup.server.model.request.user.RegisterUserRequest
import com.rollinup.server.model.request.user.UpdatePasswordAndDeviceRequest
import com.rollinup.server.model.request.user.UserQueryParams
import com.rollinup.server.model.response.Response
import com.rollinup.server.model.response.user.GetAllUserResponse
import com.rollinup.server.model.response.user.GetUserByIdResponse
import com.rollinup.server.model.response.user.GetUserOptionsResponse
import com.rollinup.server.model.response.user.ResetPasswordRequestResponse
import com.rollinup.server.model.response.user.ValidateResetOtpResponse
import com.rollinup.server.model.response.user.ValidateVerificationOtpResponse
import com.rollinup.server.service.email.EmailService
import com.rollinup.server.service.jwt.TokenClaim
import com.rollinup.server.service.jwt.TokenService
import com.rollinup.server.service.security.HashingService
import com.rollinup.server.service.security.SaltedHash
import com.rollinup.server.util.Config
import com.rollinup.server.util.Message
import com.rollinup.server.util.Utils
import com.rollinup.server.util.isExistException
import com.rollinup.server.util.manager.TransactionManager
import com.rollinup.server.util.missingArgumentException
import com.rollinup.server.util.notFoundException
import com.rollinup.server.util.successEditResponse
import com.rollinup.server.util.successGettingResponse
import com.rollinup.server.util.toCensoredEmail
import java.time.Instant

class UserServiceImpl(
    private val userRepository: UserRepository,
    private val resetPasswordRepository: ResetPasswordRepository,
    private val hashingService: HashingService,
    private val tokenService: TokenService,
    private val verificationTokenRepository: VerificationTokenRepository,
    private val emailService: EmailService,
    private val mapper: UserMapper,
    private val transactionManager: TransactionManager,
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

            val generatedPassword = Utils.generateRandomPassword()

            val saltedPassword = hashingService.generateSaltedHash(generatedPassword)

            userRepository.createUser(
                request = requestBody.copy(
                    salt = saltedPassword.salt,
                    password = saltedPassword.value
                )
            )

            emailService.sendEmail(
                receiver = requestBody.email,
                message = Message.getAccountCreationEmail(
                    email = requestBody.email,
                    password = generatedPassword,
                    username = requestBody.userName
                ),
                subject = "Account Creation",
            )

            return@suspendTransaction Response(
                status = 201,
                message = Message.CREATE_USER_SUCCESS,
                data = Unit
            )
        }

    override suspend fun editUser(
        requestBody: EditUserRequest,
        id: String,
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
            status = 202,
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

    override suspend fun getUserById(id: String): Response<GetUserByIdResponse> =
        transactionManager.suspendTransaction {
            val result =
                userRepository.getUserById(id) ?: throw CommonException(Message.USER_NOT_FOUND)

            return@suspendTransaction Response(
                status = 200,
                message = "user".successGettingResponse(),
                data = mapper.mapGetUserByIdResponse(result)
            )
        }

    override suspend fun validateResetOtp(
        userNameOrEmail: String,
        otp: String,
    ): Response<ValidateResetOtpResponse> = transactionManager.suspendTransaction {
        val user = userRepository.getUserByEmailOrUsername(userNameOrEmail)
            ?: throw CommonException(Message.USER_NOT_FOUND)

        val resetPasswordEntity = resetPasswordRepository.getToken(id = user.id)
            ?: throw CommonException(Message.INVALID_TOKEN)

        validateOtp(
            expiredAt = resetPasswordEntity.expiredAt,
            otp = otp,
            saltedOtp = resetPasswordEntity.token,
            salt = resetPasswordEntity.salt
        )

        val resetToken = tokenService.generateToken(
            config = Config.getTokenConfig().copy(expiresIn = Constant.OTP_DURATION),
            TokenClaim(
                value = user.id,
                name = "id"
            )
        )

        return@suspendTransaction Response(
            status = 200,
            message = Message.VALIDATE_OTP_SUCCESS,
            data = mapper.mapValidateResetOtpResponse(resetToken)
        )
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
                    existedToken.expiredAt > Instant.now()

            if (isStillValid) {
                return@suspendTransaction Response(
                    status = 200,
                    message = Message.EMAIL_SENT,
                    data = response
                )
            }

            val otp = Utils.generateRandom(5)

            emailService.sendEmail(
                receiver = usernameOrEmail,
                message = Message.getResetPasswordEmail(otp),
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
        newPassword: String,
    ): Response<Unit> =
        transactionManager.suspendTransaction {
            val saltedPassword = hashingService.generateSaltedHash(newPassword)
            val id = validateJWT(token)

            userRepository.resetPassword(
                id = id,
                newPassword = saltedPassword.value,
                salt = saltedPassword.salt
            )

            return@suspendTransaction Response(
                status = 200,
                message = Message.EDIT_USER_SUCCESS,
                data = Unit
            )
        }

    override suspend fun getUserOptions(): Response<GetUserOptionsResponse> =
        transactionManager.suspendTransaction {
            val response = userRepository.getUserOptions()
            return@suspendTransaction Response(
                status = 200,
                message = "",
                data = response
            )
        }


    override suspend fun deleteUsers(body: ListIdBody): Response<Unit> =
        transactionManager.suspendTransaction {
            userRepository.deleteUser(body.listId)
            return@suspendTransaction Response(
                status = 202,
                message = "",
                data = Unit
            )
        }

    override suspend fun checkEmailUserName(email: String?, username: String?): Response<Unit> =
        transactionManager.suspendTransaction {
            val isEmailOrUsernameAvailable =
                userRepository.checkEmailOrUsername(email, username)
            return@suspendTransaction if (isEmailOrUsernameAvailable) {
                Response(
                    status = 200,
                    message = "Available",
                )
            } else {
                throw "Email or Username".isExistException()
            }
        }

    override suspend fun validateVerificationOtp(
        id: String,
        otp: String,
    ): Response<ValidateVerificationOtpResponse> = transactionManager.suspendTransaction {
        val user = userRepository.getUserById(id)
            ?: throw CommonException(Message.USER_NOT_FOUND)
        val verificationOtp = verificationTokenRepository.getTokenByUser(id)
            ?: throw CommonException(Message.TOKEN_NOT_FOUND)
        val tokenExpiredAt = verificationOtp.expiredAt

        validateOtp(
            expiredAt = tokenExpiredAt,
            otp = otp,
            saltedOtp = verificationOtp.token,
            salt = verificationOtp.salt
        )

        val verificationToken = tokenService.generateToken(
            config = Config.getTokenConfig().copy(expiresIn = Constant.OTP_DURATION),
            TokenClaim(
                value = user.id,
                name = "id"
            )
        )

        return@suspendTransaction Response(
            status = 202,
            message = Message.VALIDATE_OTP_SUCCESS,
            data = mapper.mapValidateVerificationOtpResponse(verificationToken)
        )
    }

    override suspend fun resendVerificationOtp(id: String): Response<Unit> =
        transactionManager.suspendTransaction {
            val user = userRepository.getUserById(id)
                ?: throw CommonException(Message.USER_NOT_FOUND)

            val otp = verificationTokenRepository.getTokenByUser(id)
            val isOTPExpired = otp?.let { isOTPExpired(it) } ?: true

            if (isOTPExpired) {
                val newOtp = Utils.generateRandom(5)

                val saltedOtp = hashingService.generateSaltedHash(newOtp)

                val expiredAT = (System.currentTimeMillis() + Constant.OTP_DURATION)
                    .let { Instant.ofEpochMilli(it) }

                verificationTokenRepository.createToken(
                    id = id,
                    token = saltedOtp.value,
                    salt = saltedOtp.salt,
                    expiredAt = expiredAT
                )

                emailService.sendEmail(
                    receiver = user.email,
                    message = Message.getVerificationEmail(newOtp),
                    subject = "First Time Login Verification"
                )
            } else {
                throw CommonException(Message.EMAIL_ALREADY_SENT)
            }
            return@suspendTransaction Response(status = 202, message = Message.EMAIL_ALREADY_SENT)
        }

    override suspend fun updatePasswordAndVerify(
        body: UpdatePasswordAndDeviceRequest,
    ): Response<Unit> = transactionManager.suspendTransaction {
        val id = validateJWT(body.token)
        val saltedPassword =
            hashingService.generateSaltedHash(body.password)

        val user = userRepository.getUserById(id)
            ?: throw "user".notFoundException()

        if (user.device != null)
            throw CommonException(Message.DEVICE_ALREADY_REGISTERED)

        val deviceId = if (user.role.name.equals(Role.STUDENT.value, true)) {
            body.deviceId ?: throw "deviceId".missingArgumentException()
        } else {
            body.deviceId
        }

        val isDeviceRegistered = deviceId?.let {
            userRepository.checkDevice(it)
        } ?: false

        if (isDeviceRegistered)
            throw CommonException(Message.DEVICE_ALREADY_REGISTERED)

        userRepository.updatePasswordAndDevice(
            id = id,
            salt = saltedPassword.salt,
            password = saltedPassword.value,
            deviceId = deviceId,
        )

        return@suspendTransaction Response(
            status = 201,
            message = "user's password and device id".successEditResponse()
        )
    }

    private fun validateOtp(
        expiredAt: Instant,
        otp: String,
        saltedOtp: String,
        salt: String,
    ) {
        val currentTime = Instant.now()
        if (expiredAt < currentTime)
            throw CommonException(Message.EXPIRED_TOKEN)

        val saltedToken = SaltedHash(saltedOtp, salt)

        val isValid = hashingService.verify(
            value = otp,
            saltedHash = saltedToken
        )
        if (!isValid)
            throw CommonException(Message.INVALID_TOKEN)
    }

    private fun isOTPExpired(otp: VerificationTokenEntity): Boolean {
        val now = Instant.now()
        return now > otp.expiredAt
    }

    private fun validateJWT(
        token: String,
    ): String {
        val tokenClaim = tokenService.validateToken(
            token = token,
            config = Config.getTokenConfig()
        )

        if (!tokenClaim)
            throw InvalidTokenExceptions

        val id = JWT.decode(token).getClaim("id").asString()
        val user = userRepository.getUserById(id)
            ?: throw CommonException(Message.USER_NOT_FOUND)

        return user.id
    }

}