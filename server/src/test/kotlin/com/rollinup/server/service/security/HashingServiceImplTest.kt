package com.rollinup.server.service.security

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class HashingServiceImplTest {

    private val hashingService = HashingServiceImpl()

    @Test
    fun `generateSaltedHash should return a valid hash and salt`() {
        // Arrange
        val password = "mySecurePassword123"
        val saltLength = 32

        // Act
        val saltedHash = hashingService.generateSaltedHash(password, saltLength)

        // Assert
        assertTrue(saltedHash.value.isNotBlank(), "Hash should not be blank")
        assertTrue(saltedHash.salt.isNotBlank(), "Salt should not be blank")
        assertEquals(saltLength * 2, saltedHash.salt.length, "Salt hex string should be 64 characters long")
    }

    @Test
    fun `verify should return true for a correct password`() {
        // Arrange
        val password = "mySecurePassword123"

        val saltedHash = hashingService.generateSaltedHash(password)

        // Act
        val isPasswordCorrect = hashingService.verify(
            value = password,
            saltedHash = saltedHash
        )

        // Assert
        assertTrue(isPasswordCorrect, "Verification should succeed for the correct password")
    }

    @Test
    fun `verify should return false for an incorrect password`() {
        // Arrange
        val correctPassword = "mySecurePassword123"
        val incorrectPassword = "wrongPassword"

        val saltedHash = hashingService.generateSaltedHash(correctPassword)

        // Act
        val isPasswordCorrect = hashingService.verify(
            value = incorrectPassword,
            saltedHash = saltedHash
        )

        // Assert
        assertTrue(!isPasswordCorrect, "Verification should fail for an incorrect password")
    }

    @Test
    fun `generateSaltedHash should produce different hashes for the same password due to random salt`() {
        // Arrange
        val password = "samePassword"

        // Act
        val saltedHash1 = hashingService.generateSaltedHash(password)
        val saltedHash2 = hashingService.generateSaltedHash(password)

        // Assert
        assertNotEquals(saltedHash1.salt, saltedHash2.salt, "Salts should be different for each generation")
        assertNotEquals(saltedHash1.value, saltedHash2.value, "Hashes should be different for each generation")
    }
}
