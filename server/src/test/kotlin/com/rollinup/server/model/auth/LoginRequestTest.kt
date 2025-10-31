package com.rollinup.server.model.auth

import com.rollinup.server.model.request.auth.LoginRequest
import io.ktor.server.plugins.requestvalidation.ValidationResult
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class LoginRequestTest {

    @Test
    fun `default instantiation should have empty strings`() {
        val request = LoginRequest()
        assertEquals("", request.username)
        assertEquals("", request.password)
    }

    @Test
    fun `instantiation with values should hold correct data`() {
        val request = LoginRequest(username = "testuser", password = "testpassword")
        assertEquals("testuser", request.username)
        assertEquals("testpassword", request.password)
    }

    @Test
    fun `validation should return Valid when username and password are not blank`() {
        val request = LoginRequest(username = "testuser", password = "testpassword")
        val result = request.validation()
        assertIs<ValidationResult.Valid>(result)
    }

    @Test
    fun `validation should return Invalid when username is blank`() {
        val request = LoginRequest(username = "", password = "testpassword")
        val result = request.validation()
        assertIs<ValidationResult.Invalid>(result)
        assertEquals("Username cannot be empty.", result.reasons.first())
    }

    @Test
    fun `validation should return Invalid when password is blank`() {
        val request = LoginRequest(username = "testuser", password = "")
        val result = request.validation()
        assertIs<ValidationResult.Invalid>(result)
        assertEquals("Password cannot be empty.", result.reasons.first())
    }

    @Test
    fun `validation should prioritize username blank over password blank`() {
        val request = LoginRequest(username = "", password = "")
        val result = request.validation()
        assertIs<ValidationResult.Invalid>(result)
        assertEquals("Username cannot be empty.", result.reasons.first())
    }
}
