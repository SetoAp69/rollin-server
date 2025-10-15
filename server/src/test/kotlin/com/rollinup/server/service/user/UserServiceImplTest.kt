package com.rollinup.server.service.user

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.rollinup.server.CommonException
import com.rollinup.server.Constant
import com.rollinup.server.InvalidTokenExceptions
import com.rollinup.server.MockkEnvironment
import com.rollinup.server.datasource.database.model.resetpassword.ResetPasswordEntity
import com.rollinup.server.datasource.database.model.user.UserEntity
import com.rollinup.server.datasource.database.repository.resetpassword.ResetPasswordRepository
import com.rollinup.server.datasource.database.repository.user.UserRepository
import com.rollinup.server.mapper.UserMapper
import com.rollinup.server.model.request.user.EditUserRequest
import com.rollinup.server.model.request.user.RegisterUserRequest
import com.rollinup.server.model.request.user.UserQueryParams
import com.rollinup.server.model.response.Response
import com.rollinup.server.model.response.user.ValidateResetOtpResponse
import com.rollinup.server.service.email.EmailService
import com.rollinup.server.service.jwt.TokenService
import com.rollinup.server.service.security.HashingService
import com.rollinup.server.service.security.SaltedHash
import com.rollinup.server.util.Message
import com.rollinup.server.util.Utils.toFormattedDateString
import com.rollinup.server.util.manager.TransactionManager
import com.rollinup.server.util.successGettingResponse
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.jetbrains.exposed.v1.core.Transaction
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class UserServiceImplTest {
    private lateinit var userService: UserService

    @MockK
    private lateinit var userRepository: UserRepository

    @MockK
    private lateinit var tokenService: TokenService

    @MockK
    private lateinit var resetPasswordRepository: ResetPasswordRepository

    @MockK
    private lateinit var emailService: EmailService

    @MockK
    private lateinit var hashingService: HashingService

    @MockK
    private lateinit var transactionManager: TransactionManager

    private var mapper = UserMapper()

    private val envMockk = MockkEnvironment()

    //region Arrange Helpers

    private fun arrangeSuspendTransaction() {
        coEvery {
            transactionManager.suspendTransaction<Any>(any())
        } answers {
            val block = args.first() as Function1<Transaction, Any>
            val mockedTransaction = mockk<Transaction>(relaxed = true)

            block.invoke(mockedTransaction)
        }
    }

    private fun arrangeUserGetByEmailOrUsername(emailOrUsername: String, result: UserEntity?) {
        coEvery { userRepository.getUserByEmailOrUsername(emailOrUsername) } returns result
    }

    private fun arrangeUserGetById(id: String, result: UserEntity?) {
        coEvery { userRepository.getUserById(id) } returns result
    }

    private fun arrangeEditUser(id: String, reqBody: EditUserRequest) {
        coEvery { userRepository.editUser(reqBody, id) } just runs
    }

    private fun arrangeUserCreateUser(requestBody: RegisterUserRequest) {
        coEvery { userRepository.createUser(requestBody) } just runs
    }

    private fun arrangeUserGetAll(queryParams: UserQueryParams, result: List<UserEntity>) {
        coEvery { userRepository.getAllUsers(queryParams) } returns result
    }

    private fun arrangeResetPasswordGetToken(id: String, result: ResetPasswordEntity?) {
        coEvery { resetPasswordRepository.getToken(id) } returns result
    }

    private fun arrangeHashingVerify(value: String, saltedHash: SaltedHash, result: Boolean) {
        coEvery { hashingService.verify(value, saltedHash) } returns result
    }

    private fun arrangeHashingGenerate(value: String, saltedHash: SaltedHash) {
        coEvery { hashingService.generateSaltedHash(value) } returns saltedHash
    }

    private fun arrangeEmailSend() {
        coEvery { emailService.sendEmail(any(), any(), any()) } just runs
    }

    private fun arrangeResetPasswordSaveToken() {
        coEvery { resetPasswordRepository.saveToken(any(), any(), any()) } just runs
    }

    private fun arrangeResetPassword() {
        coEvery { userRepository.resetPassword(any(), any(), any()) } just runs
    }

    private fun arrangeTokenValidate(token: String, result: Boolean) {
        coEvery { tokenService.validateToken(token, any()) } returns result
    }

    private fun arrangeTokenGenerate(result: String) {
        coEvery { tokenService.generateToken(any(), any()) } returns result
    }
    //endregion

    @Before
    fun setUp() {
        envMockk.setup()
        MockKAnnotations.init(this)
        // Mock the TransactionManager to simply execute the block passed to it
        arrangeSuspendTransaction()

        userService = UserServiceImpl(
            userRepository = userRepository,
            resetPasswordRepository = resetPasswordRepository,
            hashingService = hashingService,
            tokenService = tokenService,
            emailService = emailService,
            mapper = mapper,
            transactionManager = transactionManager
        )
    }

    @After
    fun tearDown() {
        unmockkAll()
        envMockk.teardown()
    }

    //region registerUser Tests
    @Test
    fun `registerUser() should return the correct response when success`() = runTest {
        //Arrange
        val reqBody = RegisterUserRequest(
            userName = "username",
            email = "email@email.com",
            password = "password",
            role = "admin"
        )

        arrangeUserGetByEmailOrUsername(emailOrUsername = reqBody.email, result = null)
        arrangeUserGetByEmailOrUsername(emailOrUsername = reqBody.userName, result = null)
        arrangeUserCreateUser(requestBody = reqBody)

        val expectedResponse =
            Response(status = 201, message = Message.CREATE_USER_SUCCESS, data = Unit)

        //Act
        val result = userService.registerUser(reqBody)

        //Assert
        coVerify { userRepository.createUser(reqBody) }
        assertEquals(expected = expectedResponse, actual = result)
    }

    @Test
    fun `registerUser() should throw CommonException when email is used`() = runTest {
        //Arrange
        val reqBody = RegisterUserRequest(
            userName = "username",
            email = "email@email.com",
            password = "password",
            role = "admin"
        )

        arrangeUserGetByEmailOrUsername(
            emailOrUsername = reqBody.email,
            result = UserEntity(email = "email@email.com")
        )
        arrangeUserGetByEmailOrUsername(emailOrUsername = reqBody.userName, result = null)

        val expectedMessage = Message.EMAIL_USED

        //Act & Assert
        val result = assertFailsWith<CommonException> {
            userService.registerUser(reqBody)
        }
        assertEquals(expectedMessage, result.message)
    }

    @Test
    fun `registerUser() should throw CommonException when username is used`() = runTest {
        //Arrange
        val reqBody = RegisterUserRequest(
            userName = "username",
            email = "email@email.com",
            password = "password",
            role = "admin"
        )

        arrangeUserGetByEmailOrUsername(emailOrUsername = reqBody.email, result = null)
        arrangeUserGetByEmailOrUsername(
            emailOrUsername = reqBody.userName,
            result = UserEntity(userName = "username")
        )

        val expectedMessage = Message.USERNAME_USED

        //Act & Assert
        val result = assertFailsWith<CommonException> {
            userService.registerUser(reqBody)
        }
        assertEquals(expectedMessage, result.message)
    }
    //endregion

    //region editUser Tests
    @Test
    fun `editUser() should return correct response when success`() = runTest {
        //Arrange
        val id = "userId"
        val reqBody = EditUserRequest(
            userName = "username",
            firstName = "firstname",
            email = "email",
            role = "role"
        )
        val expectedResponse =
            Response(status = 200, message = Message.EDIT_USER_SUCCESS, data = Unit)

        arrangeUserGetById(id = id, result = UserEntity(id = id))
        arrangeEditUser(id = id, reqBody = reqBody)

        //Act
        val result = userService.editUser(reqBody, id)

        //Assert
        coVerify { userRepository.editUser(reqBody, id) }
        assertEquals(expected = expectedResponse, actual = result)
    }

    @Test
    fun `editUser() should throw CommonException when user not found`() = runTest {
        //Arrange
        val id = "userId"
        val reqBody = EditUserRequest(
            userName = "username",
            firstName = "firstname",
            email = "email",
            role = "role"
        )
        val expectedMessage = Message.USER_NOT_FOUND

        arrangeUserGetById(id = id, result = null)

        //Act & Assert
        val result = assertFailsWith<CommonException> {
            userService.editUser(reqBody, id)
        }
        coVerify(exactly = 0) { userRepository.editUser(any(), any()) }
        assertEquals(expected = expectedMessage, actual = result.message)
    }
    //endregion

    //region getAllUser Tests
    @Test
    fun `getAllUser() should return correct response when users exist`() = runTest {
        //Arrange
        val queryParams = UserQueryParams()
        val mockUsers = listOf(UserEntity(id = "id1"), UserEntity(id = "id2"))
        val expectedData = mapper.mapGetAllUserResponse(data = mockUsers, queryParams = queryParams)
        val expectedResponse =
            Response(status = 200, message = "user".successGettingResponse(), data = expectedData)

        arrangeUserGetAll(queryParams = queryParams, result = mockUsers)

        //Act
        val result = userService.getAllUser(queryParams)

        //Assert
        assertEquals(expected = expectedResponse, actual = result)
    }

    @Test
    fun `getAllUser() should return correct empty response when no users exist`() = runTest {
        //Arrange
        val queryParams = UserQueryParams()
        val mockUsers = emptyList<UserEntity>()
        val expectedData = mapper.mapGetAllUserResponse(data = mockUsers, queryParams = queryParams)
        val expectedResponse =
            Response(status = 200, message = "user".successGettingResponse(), data = expectedData)

        arrangeUserGetAll(queryParams = queryParams, result = mockUsers)

        //Act
        val result = userService.getAllUser(queryParams)

        //Assert
        assertEquals(expected = expectedResponse, actual = result)
        assertEquals(expected = 0, actual = result.data?.record)
    }
    //endregion

    //region validateResetOtp Tests
    @Test
    fun `validateResetOtp() should return success and token when otp is valid`() = runTest {
        //Arrange
        val usernameOrEmail = "test@test.com"
        val otp = "12345"
        val userId = "userId"
        val storedToken = "hashedOtp"
        val storedSalt = "salt"
        val resetToken = "newResetToken"
        val expiredAt = (System.currentTimeMillis() + Constant.OTP_DURATION)
            .toFormattedDateString()

        val userEntity = UserEntity(id = userId)
        val resetPasswordEntity =
            ResetPasswordEntity(token = storedToken, salt = storedSalt, expiredAt = expiredAt)
        val saltedHash = SaltedHash(value = storedToken, salt = storedSalt)

        arrangeUserGetByEmailOrUsername(usernameOrEmail, userEntity)
        arrangeResetPasswordGetToken(userId, resetPasswordEntity)
        arrangeHashingVerify(otp, saltedHash, true)
        arrangeTokenGenerate(resetToken)

        val expectedResponse = Response(
            status = 200,
            message = Message.VALIDATE_OTP_SUCCESS,
            data = ValidateResetOtpResponse(resetToken)
        )

        //Act
        val result = userService.validateResetOtp(usernameOrEmail, otp)

        //Assert
        assertEquals(expectedResponse, result)
    }

    @Test
    fun `validateResetOtp() should throw Exception when user not found`() = runTest {
        //Arrange
        val usernameOrEmail = "notfound@test.com"
        arrangeUserGetByEmailOrUsername(usernameOrEmail, null)

        //Act & Assert
        val exception = assertFailsWith<CommonException> {
            userService.validateResetOtp(usernameOrEmail, "12345")
        }
        assertEquals(Message.USER_NOT_FOUND, exception.message)
    }

    @Test
    fun `validateResetOtp() should throw Exception when otp not found in repo`() = runTest {
        //Arrange
        val usernameOrEmail = "test@test.com"
        val userId = "userId"
        val userEntity = UserEntity(id = userId)

        arrangeUserGetByEmailOrUsername(usernameOrEmail, userEntity)
        arrangeResetPasswordGetToken(userId, null)

        //Act & Assert
        val exception = assertFailsWith<CommonException> {
            userService.validateResetOtp(usernameOrEmail, "12345")
        }
        assertEquals(Message.INVALID_TOKEN, exception.message)
    }

    @Test
    fun `validateResetOtp() should throw Exception when otp is expired`() = runTest {
        //Arrange
        val usernameOrEmail = "test@test.com"
        val userId = "userId"
        val userEntity = UserEntity(id = userId)
        val resetPasswordEntity = ResetPasswordEntity(
            token = "token",
            salt = "salt",
            expiredAt = "2025-10-04T04:16:25.123456Z"
        )

        arrangeUserGetByEmailOrUsername(usernameOrEmail, userEntity)
        arrangeResetPasswordGetToken(userId, resetPasswordEntity)

        //Act & Assert
        val exception = assertFailsWith<CommonException> {
            userService.validateResetOtp(usernameOrEmail, "12345")
        }
        assertEquals(Message.EXPIRED_TOKEN, exception.message)
    }

    @Test
    fun `validateResetOtp() should throw Exception when otp is invalid`() = runTest {
        //Arrange
        val usernameOrEmail = "test@test.com"
        val otp = "wrong-otp"
        val userId = "userId"
        val storedToken = "hashedOtp"
        val storedSalt = "salt"
        val expiredAt = (System.currentTimeMillis() + Constant.OTP_DURATION)
            .toFormattedDateString()

        val userEntity = UserEntity(id = userId)
        val resetPasswordEntity =
            ResetPasswordEntity(token = storedToken, salt = storedSalt, expiredAt = expiredAt)
        val saltedHash = SaltedHash(value = storedToken, salt = storedSalt)

        arrangeUserGetByEmailOrUsername(usernameOrEmail, userEntity)
        arrangeResetPasswordGetToken(userId, resetPasswordEntity)
        arrangeHashingVerify(otp, saltedHash, false)

        //Act & Assert
        val exception = assertFailsWith<CommonException> {
            userService.validateResetOtp(usernameOrEmail, otp)
        }
        assertEquals(Message.INVALID_TOKEN, exception.message)
    }
    //endregion

    //region resetPasswordRequest Tests
    @Test
    fun `resetPasswordRequest() should return success when user exists and has no valid token`() =
        runTest {
            //Arrange
            val usernameOrEmail = "test@test.com"
            val userId = "userId"
            val userEntity = UserEntity(id = userId)
            val saltedHash = SaltedHash("hashedOtp", "salt")


            arrangeUserGetByEmailOrUsername(usernameOrEmail, userEntity)
            arrangeResetPasswordGetToken(userId, null) // No existing token
            arrangeEmailSend()
            arrangeResetPasswordSaveToken()

            coEvery {
                hashingService.generateSaltedHash(any())
            } returns saltedHash


            //Act
            val result = userService.resetPasswordRequest(usernameOrEmail)

            //Assert
            coVerify { emailService.sendEmail(any(), any(), any()) }
            coVerify {
                resetPasswordRepository.saveToken(
                    userId,
                    saltedHash.value,
                    saltedHash.salt
                )
            }
            assertEquals(Message.EMAIL_SENT, result.message)
        }

    @Test
    fun `resetPasswordRequest() should still return success when user does not exist for security`() =
        runTest {
            //Arrange
            val usernameOrEmail = "notfound@test.com"
            arrangeUserGetByEmailOrUsername(usernameOrEmail, null)

            //Act
            val result = userService.resetPasswordRequest(usernameOrEmail)

            //Assert
            coVerify(exactly = 0) { emailService.sendEmail(any(), any(), any()) }
            coVerify(exactly = 0) { resetPasswordRepository.saveToken(any(), any(), any()) }
            assertEquals(Message.EMAIL_SENT, result.message)
        }

    @Test
    fun `resetPasswordRequest() should throw Exception when a valid token already exists`() =
        runTest {
            //Arrange
            val usernameOrEmail = "test@test.com"
            val userId = "userId"
            val userEntity = UserEntity(id = userId)
            val expiresAt =
                (System.currentTimeMillis() + Constant.OTP_DURATION).toFormattedDateString()
            val existingToken = ResetPasswordEntity(
                token = "token",
                salt = "salt",
                expiredAt = expiresAt
            )

            arrangeUserGetByEmailOrUsername(usernameOrEmail, userEntity)
            arrangeResetPasswordGetToken(userId, existingToken)

            //Act & Assert
            val exception = assertFailsWith<CommonException> {
                userService.resetPasswordRequest(usernameOrEmail)
            }
            assertEquals(Message.EMAIL_ALREADY_SENT, exception.message)
        }
    //endregion

    //region resetPassword Tests
    @Test
    fun `resetPassword() should return success when token is valid`() = runTest {
        //Arrange
        val userId = "userId"
        // Create a valid-looking JWT token for decoding
        val token = JWT.create().withClaim("id", userId).sign(Algorithm.HMAC256("secret"))
        val newPassword = "newPassword123"
        val saltedHash = SaltedHash("hashedPassword", "salt")

        arrangeTokenValidate(token, true)
        arrangeUserGetById(userId, UserEntity(id = userId))
        arrangeHashingGenerate(newPassword, saltedHash)
        arrangeResetPassword()

        val expectedResponse =
            Response(status = 200, message = Message.EDIT_USER_SUCCESS, data = Unit)

        //Act
        val result = userService.resetPassword(token, newPassword)

        //Assert
        coVerify { userRepository.resetPassword(userId, saltedHash.value, saltedHash.salt) }
        assertEquals(expectedResponse, result)
    }

    @Test
    fun `resetPassword() should throw InvalidTokenExceptions when token validation fails`() =
        runTest {
            //Arrange
            val token = "invalidToken"
            val newPassword = "newPassword123"

            arrangeTokenValidate(token, false)

            //Act & Assert
            assertFailsWith<InvalidTokenExceptions> {
                userService.resetPassword(token, newPassword)
            }
        }

    @Test
    fun `resetPassword() should throw CommonException when user from token not found`() = runTest {
        //Arrange
        val userId = "nonExistentUserId"
        val token = JWT.create().withClaim("id", userId).sign(Algorithm.HMAC256("secret"))
        val newPassword = "newPassword123"

        arrangeTokenValidate(token, true)
        arrangeUserGetById(userId, null)

        //Act & Assert
        val exception = assertFailsWith<CommonException> {
            userService.resetPassword(token, newPassword)
        }
        assertEquals(Message.USER_NOT_FOUND, exception.message)
    }
    //endregion
}
