package com.rollinup.server.model.auth

import com.rollinup.server.model.request.auth.RefreshTokenRequest
import org.junit.Test
import kotlin.test.assertEquals

class RefreshTokenRequestTest {

    @Test
    fun `instantiation should hold correct refresh token`() {
        val tokenValue = "my-refresh-token-123"
        val request = RefreshTokenRequest(refreshToken = tokenValue)
        assertEquals(tokenValue, request.refreshToken)
    }
}
