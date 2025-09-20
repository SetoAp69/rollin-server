package com.rollinup.server.service.security

import io.ktor.util.logging.Logger
import org.apache.commons.codec.binary.Hex
import org.apache.commons.codec.digest.DigestUtils
import org.slf4j.LoggerFactory
import java.security.SecureRandom

class HashingServiceImpl() : HashingService {

    override fun generateSaltedHash(value: String, saltLength: Int): SaltedHash {
        val salt = SecureRandom.getInstance("SHA1PRNG").generateSeed(saltLength)
        val saltAsHex = Hex.encodeHexString(salt)
        val hash = DigestUtils.sha256Hex("$saltAsHex$value")
        return SaltedHash(
            value = hash,
            salt = saltAsHex
        )

    }

    override fun verify(
        value: String,
        saltedHash: SaltedHash
    ): Boolean {
        val inputPassword = DigestUtils.sha256Hex(saltedHash.salt + value)
        val actualPassword = saltedHash.value
        println("Actual password :$actualPassword")
        println("Input :$inputPassword")

        return DigestUtils.sha256Hex(saltedHash.salt + value) == saltedHash.value
    }


}