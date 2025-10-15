package com.rollinup.server.service.auth

import com.rollinup.server.CommonException
import com.rollinup.server.Constant
import com.rollinup.server.MockkEnvironment
import com.rollinup.server.datasource.database.model.user.UserEntity
import com.rollinup.server.datasource.database.repository.refreshtoken.RefreshTokenRepository
import com.rollinup.server.datasource.database.repository.user.UserRepository
import com.rollinup.server.mapper.AuthMapper
import com.rollinup.server.mockkEnvironment
import com.rollinup.server.model.Role
import com.rollinup.server.model.request.auth.LoginRequest
import com.rollinup.server.model.response.Response
import com.rollinup.server.model.response.auth.LoginResponse
import com.rollinup.server.model.response.auth.RefreshTokenResponse
import com.rollinup.server.model.response.user.UserDTO
import com.rollinup.server.service.jwt.TokenClaim
import com.rollinup.server.service.jwt.TokenConfig
import com.rollinup.server.service.jwt.TokenService
import com.rollinup.server.service.security.HashingService
import com.rollinup.server.service.security.SaltedHash
import com.rollinup.server.util.Config
import com.rollinup.server.util.Message
import com.rollinup.server.util.manager.TransactionManager
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.jetbrains.exposed.v1.core.Transaction
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class AuthServiceImplTest {

    private lateinit var authService: AuthService

    @MockK
    private var hashingService: HashingService = mockk()

    @MockK
    private var jwtService: TokenService = mockk()

    @MockK
    private var userRepository: UserRepository = mockk()

    @MockK
    private var refreshTokenRepository: RefreshTokenRepository = mockk()

    @MockK
    private var transactionManager: TransactionManager = mockk()

    private var authMapper: AuthMapper = AuthMapper()

    private val envMock = MockkEnvironment()

    private fun arrangeHashVerification(
        value: String,
        saltedHash: SaltedHash,
        result: Boolean
    ) {
        coEvery {
            hashingService.verify(
                value = value,
                saltedHash = saltedHash
            )
        } returns result
    }

    //User Repo
    private fun arrangeUserRepositoryGetUserByEmailOrUserName(
        username: String,
        result: UserEntity?
    ) {
        coEvery {
            userRepository.getUserByEmailOrUsername(username)
        } returns result
    }

    //Refresh Token Repo
    private fun arrangeRefreshTokenRepositorySave(
        token: String,
        userId: String
    ) {
        coEvery {
            refreshTokenRepository.save(token, userId)
        } just Runs
    }

    private fun arrangeRefreshTokenRepositoryDelete(
        token: String
    ) {
        coEvery {
            refreshTokenRepository.dropToken(token)
        } just Runs
    }

    private fun arrangeRefreshTokenRepositoryGetId(
        token: String,
        result: UserEntity?
    ) {
        coEvery {
            refreshTokenRepository.findUserId(token)
        } returns result
    }

    //JWTService
    private fun arrangeJWTServiceGenerateToken(
        config: TokenConfig,
        claims: List<TokenClaim>,
        result: String
    ) {

        coEvery {
            jwtService.generateToken(
                config = config,
                *claims.toTypedArray()
            )
        } returns result
    }

    private fun arrangeJWTServiceValidateToken(
        token: String,
        config: TokenConfig,
        result: Boolean
    ) {
        coEvery {
            jwtService.validateToken(
                token = token,
                config = config
            )
        } returns result
    }


    @Before
    fun setUp() {
        envMock.setup()
        MockKAnnotations.init(this)
        authService = AuthServiceImpl(
            hashingService = hashingService,
            jwtService = jwtService,
            userRepository = userRepository,
            refreshTokenRepository = refreshTokenRepository,
            authMapper = authMapper,
            transactionManager = transactionManager
        )

        coEvery {
            transactionManager.suspendTransaction<Any>(any())
        } answers {
            val block = args.first() as Function1<Transaction, Any>

            val mockedTransaction = mockk<Transaction>(relaxed = true)

            block.invoke(mockedTransaction)
        }
    }

    @After
    fun tearDown() {
        unmockkAll()
        envMock.teardown()
    }


    @Test
    fun `login() should return correct response data`() = runTest {
        //Arrange
        val loginRequest = LoginRequest(
            username = "username",
            password = "password"
        )

        val mockkUserEntity = UserEntity(
            id = "id",
            userName = "username",
            email = "email",
            firstName = "firstName",
            lastName = "lastName",
            role = Role.STUDENT,
            gender = "F",
            password = "hashedPassword",
            salt = "salt"
        )

        val expectedUserDTO = UserDTO(
            id = "id",
            userName = "username",
            email = "email",
            firstName = "firstName",
            lastName = "lastName",
            role = "student",
            gender = "F"
        )
        val expectedAccessToken = "AccessToken"
        val expectedRefreshToken = "RefreshToken"

        val expectedLoginResponse = LoginResponse(
            data = expectedUserDTO,
            accessToken = expectedAccessToken,
            refreshToken = expectedRefreshToken
        )

        val expectedResponse = Response(
            status = 200,
            message = Message.LOGIN_SUCCESS,
            data = expectedLoginResponse
        )
        arrangeUserRepositoryGetUserByEmailOrUserName(
            username = loginRequest.username,
            result = mockkUserEntity
        )

        arrangeHashVerification(
            value = loginRequest.password,
            saltedHash = SaltedHash(
                value = "hashedPassword",
                salt = "salt"
            ),
            result = true
        )

        arrangeJWTServiceGenerateToken(
            config = Config.getTokenConfig().copy(expiresIn = Constant.ACCESS_TOKEN_DURATION),
            claims = listOf(
                TokenClaim(
                    name = "id",
                    value = "id"
                ),
                TokenClaim(
                    value = "username",
                    name = "username"
                ),
                TokenClaim(
                    value = "email",
                    name = "email"
                ),
                TokenClaim(
                    value = "student",
                    name = "role"
                )
            ),
            result = expectedAccessToken
        )

        arrangeJWTServiceGenerateToken(
            config = Config.getTokenConfig().copy(expiresIn = Constant.REFRESH_TOKEN_DURATION),
            claims = listOf(TokenClaim(name = "id", value = "id")),
            result = expectedRefreshToken,
        )

        arrangeRefreshTokenRepositorySave(
            token = expectedRefreshToken,
            userId = mockkUserEntity.id
        )

//        coEvery { suspendTransaction { any() } } returns expectedResponse

        //Act
        val result = authService.login(loginRequest)


        //Assert
        coVerify {
            userRepository.getUserByEmailOrUsername(loginRequest.username)
            hashingService.verify(
                value = loginRequest.password,
                saltedHash = SaltedHash(
                    value = "hashedPassword",
                    salt = "salt"
                )
            )
            refreshTokenRepository.save(
                token = expectedRefreshToken,
                id = mockkUserEntity.id
            )
        }

        assertEquals(
            expected = expectedResponse,
            actual = result
        )
    }

    @Test
    fun `login() should throw the correct exceptions when user not found`() = runTest {
        //Arrange
        val loginRequest = LoginRequest(
            username = "username",
            password = "password"
        )

        arrangeUserRepositoryGetUserByEmailOrUserName(
            username = loginRequest.username,
            result = null
        )

        //Act
        val result = assertFailsWith<CommonException> {
            authService.login(loginRequest)
        }

        assert(result.message == "can't find User data")
    }

    @Test
    fun `login() should throw correct exceptions when password not match`() = runTest {
        //Arrange
        val loginRequest = LoginRequest(
            username = "username",
            password = "password"
        )
        val mockkUserEntity = UserEntity(
            id = "id",
            userName = "username",
            email = "email",
            firstName = "firstName",
            lastName = "lastName",
            role = Role.STUDENT,
            gender = "F",
            password = "hashedPassword",
            salt = "salt"
        )

        arrangeUserRepositoryGetUserByEmailOrUserName(
            username = loginRequest.username,
            result = mockkUserEntity
        )

        arrangeHashVerification(
            value = loginRequest.password,
            saltedHash = SaltedHash(
                value = mockkUserEntity.password,
                salt = mockkUserEntity.salt
            ),
            result = false
        )

        //Act Assert
        val result = assertFailsWith<CommonException> {
            authService.login(loginRequest)
        }

        coVerify {
            userRepository.getUserByEmailOrUsername(loginRequest.username)
            hashingService.verify(
                value = loginRequest.password,
                saltedHash = SaltedHash(
                    value = mockkUserEntity.password,
                    salt = mockkUserEntity.salt
                )
            )
        }

        assertEquals(Message.INVALID_USERNAME_OR_PASSWORD, result.message)
    }

    @Test
    fun `refreshToken() should return correct response data`() = runTest {
        //Arrange
        val refreshToken = "This is MY Supper Cool Refresh token"
        val mockkUser = UserEntity(
            id = "id",
            userName = "username",
            email = "email",
            firstName = "firstName",
            lastName = "lastName",
            role = Role.ADMIN,
            gender = "M",
            password = "hashedPassword",
            salt = "salt"
        )

        val expectedAccessToken = "This is MY Supper Cool Access token"

        val expectedResponse = Response(
            status = 201,
            message = "Access Token data successfully created",
            data = RefreshTokenResponse(
                accessToken = expectedAccessToken
            )
        )

        arrangeJWTServiceValidateToken(
            token = refreshToken,
            config = Config.getTokenConfig(),
            result = true
        )

        arrangeRefreshTokenRepositoryGetId(
            token = refreshToken,
            result = mockkUser
        )

        arrangeJWTServiceGenerateToken(
            config = Config.getTokenConfig().copy(expiresIn = Constant.ACCESS_TOKEN_DURATION),
            claims = listOf(
                TokenClaim(
                    name = "id",
                    value = "id"
                ),
                TokenClaim(
                    value = "username",
                    name = "username"
                ),
                TokenClaim(
                    value = "email",
                    name = "email"
                ),
                TokenClaim(
                    name = "role",
                    value = "admin"
                )
            ),
            result = expectedAccessToken
        )

        //Act
        val result = authService.refreshToken(refreshToken)

        //Assert
        coVerify {
            jwtService.validateToken(
                token = refreshToken,
                config = Config.getTokenConfig()
            )
            refreshTokenRepository.findUserId(refreshToken)
            jwtService.generateToken(
                config = Config.getTokenConfig().copy(expiresIn = Constant.ACCESS_TOKEN_DURATION),
                TokenClaim(
                    name = "id",
                    value = "id"
                ),
                TokenClaim(
                    value = "username",
                    name = "username"
                ),
                TokenClaim(
                    value = "email",
                    name = "email"
                ),
                TokenClaim(
                    name = "role",
                    value = "admin"
                )
            )
        }

        assertEquals(expectedResponse, result)
    }

    @Test
    fun `refreshToken() should throw the correct exceptions when user data not found`() = runTest {
        //Arrange
        val refreshToken = "This is MY Supper Cool Refresh token"

        arrangeJWTServiceValidateToken(
            token = refreshToken,
            config = Config.getTokenConfig(),
            result = true
        )

        arrangeRefreshTokenRepositoryGetId(
            token = refreshToken,
            result = null
        )

        //Act Assert
        val result = assertFailsWith<CommonException> {
            jwtService.validateToken(token = refreshToken, config = Config.getTokenConfig())
            authService.refreshToken(refreshToken)
        }

        assertEquals("can't find User data", result.message)
    }

    @Test
    fun `refreshToken() should drop current token and throw the correct exceptions when token is invalid`() =
        runTest {
            //Arrange
            val refreshToken = "My supper refresh token"
            val mockkedUser = UserEntity(
                id = "id",
                userName = "userName",
                email = "email",
                firstName = "firstName",
                lastName = "lastName",
                role = Role.ADMIN,
                gender = "M",
                password = "hashedPassword",
                salt = "salt"
            )

            arrangeJWTServiceValidateToken(
                token = refreshToken,
                config = Config.getTokenConfig(),
                result = false
            )

            arrangeRefreshTokenRepositoryGetId(
                token = refreshToken,
                result = mockkedUser
            )

            arrangeRefreshTokenRepositoryDelete(
                token = refreshToken
            )

            //Act Assert
            val result = assertFailsWith<CommonException> {
                authService.refreshToken(token = refreshToken)
            }

            coVerify {
                refreshTokenRepository.findUserId(token = refreshToken)
                refreshTokenRepository.dropToken(token = refreshToken)
            }

            assertEquals(Message.INVALID_TOKEN, result.message)

        }

}