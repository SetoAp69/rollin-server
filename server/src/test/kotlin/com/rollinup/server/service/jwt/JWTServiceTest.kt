package com.rollinup.server.service.jwt

import com.auth0.jwt.JWT
import com.rollinup.server.util.Config
import io.mockk.every
import io.mockk.mockkObject
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.util.Date
import kotlin.test.assertEquals

class JWTServiceTest {
    private lateinit var jwtService: TokenService

    @Before
    fun setUp() {
        jwtService = JWTService()
        mockkObject(Config)
    }

    @After
    fun tearDown() {

    }

    @Test
    fun `generateToken() should generate token with correct claims`() {
        //Arrange
        val config = TokenConfig(
            realm = "realm",
            issuer = "issuer",
            audience = "audience",
            expiresIn = 6_000_000,
            secret = "supersecret"
        )

        val claim = listOf(
            TokenClaim(
                value = "idValue",
                name = "id"
            ),
            TokenClaim(
                value = "nameValue",
                name = "name"
            )
        )

        every { Config.getTokenConfig() } returns config

        //Act
        val token = jwtService.generateToken(config, *claim.toTypedArray())


        //Assert
        val decodedToken = JWT.decode(token)
        val expired = decodedToken.expiresAt
        val idClaim = decodedToken.getClaim("id").asString()
        val nameClaim = decodedToken.getClaim("name").asString()

        assertEquals<String>(expected = config.audience, actual = decodedToken.audience.first())
        assertEquals<String>(expected = config.issuer, actual = decodedToken.issuer)
        assertEquals<String>(expected = claim.first().value, actual = idClaim)
        assertEquals<String>(expected = claim.last().value, actual = nameClaim)

        assert(expired.after(Date()))
    }


    @Test
    fun `validateToken() should return true for valid token`() {
        //Arrange
        val config = TokenConfig(
            realm = "realm",
            issuer = "issuer",
            audience = "audience",
            expiresIn = 6_000_000,
            secret = "supersecret"
        )

        val claim = listOf(
            TokenClaim(
                value = "idValue",
                name = "id"
            ),
            TokenClaim(
                value = "nameValue",
                name = "name"
            )
        )

        every { Config.getTokenConfig() } returns config

        val token = jwtService.generateToken(config, *claim.toTypedArray())


        //Act
        val isValid = jwtService.validateToken(token, config)

        //Assert
        assert(isValid)
    }

    @Test
    fun `validateToken() should return false for expired token`() {
        //Arrange
        val config = TokenConfig(
            realm = "realm",
            issuer = "issuer",
            audience = "audience",
            expiresIn = -6_000_000,
            secret = "supersecret"
        )

        val claim = listOf(
            TokenClaim(
                value = "idValue",
                name = "id"
            ),
            TokenClaim(
                value = "nameValue",
                name = "name"
            )
        )

        every { Config.getTokenConfig() } returns config

        val token = jwtService.generateToken(config, *claim.toTypedArray())


        //Act
        val isValid = jwtService.validateToken(token, config)

        //Assert
        assert(!isValid)
    }

    @Test
    fun `validateToken() should return false for invalid token`() {
        //Arrange
        val config = TokenConfig(
            realm = "realm",
            issuer = "issuer",
            audience = "audience",
            expiresIn = -6_000_000,
            secret = "supersecret"
        )

        val falseConfig = TokenConfig(
            secret = "falsesecret",
            realm = "realms",
            issuer = "falseIssuer",
            audience = "falseAudience",
            expiresIn = -6_000_000,
        )

        val claim = listOf(
            TokenClaim(
                value = "idValue",
                name = "id"
            ),
            TokenClaim(
                value = "nameValue",
                name = "name"
            )
        )

        every { Config.getTokenConfig() } returns config

        val token = jwtService.generateToken(falseConfig, *claim.toTypedArray())


        //Act
        val isValid = jwtService.validateToken(token, config)

        //Assert
        assert(!isValid)
    }


}