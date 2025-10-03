package com.rollinup.server.util

import com.rollinup.server.Constant
import com.rollinup.server.service.jwt.TokenConfig
import io.ktor.client.call.body
import io.ktor.client.statement.HttpResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

object Utils {

    fun String.isEmail(): Boolean {
        val emailRegex = "^[\\w-.]+@([\\w-]+\\.)+[\\w-]{2,4}\$".toRegex()

        return this.isNotBlank() && emailRegex.matches(this)
    }

    fun String.validatePassword(): Boolean {
        val lengthValid = this.length >= 8
        val hasUppercase = this.any { it.isUpperCase() }
        val hasLowercase = this.any { it.isLowerCase() }
        val hasDigit = this.any { it.isDigit() }
        val hasSymbol = this.any { !it.isLetterOrDigit() }

        return lengthValid && hasUppercase && hasLowercase && hasDigit && hasSymbol
    }

    suspend inline fun <reified T> HttpResponse.handleBody(): T {
        if (this.status.value in 200..299) {
            return body<T>()
        } else {
            throw Exception(this.status.description)
        }
    }

    fun generateRandom(digit: Int): String {
        val digit = if (digit < 5) 5 else digit
        val random = UUID.randomUUID()
            .toString()
            .take(digit)
            .replace("-", "")

        return random
    }


    fun String.toLocalDateTime(pattern: String = Constant.DATABASE_DATE_FORMAT): LocalDateTime {
        if (this.isBlank()) return LocalDateTime.now()


        val pattern = pattern.ifBlank { Constant.DATABASE_DATE_FORMAT }
        val formatter = DateTimeFormatter.ofPattern(pattern)
        val formattedDate = LocalDateTime.parse(this, formatter)

        return formattedDate
    }

}