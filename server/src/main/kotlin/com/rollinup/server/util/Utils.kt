package com.rollinup.server.util

import com.rollinup.server.Constant
import kotlinx.serialization.json.Json
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
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


    fun Long.toFormattedDateString(): String {
        val instant = Instant.ofEpochMilli(this)
        val formatter = DateTimeFormatter.ofPattern(Constant.DATABASE_DATE_FORMAT)
            .withZone(ZoneOffset.UTC)
        return formatter.format(instant)
    }

    fun getUploadDir(path: String): String {
        return "${System.getenv("UPLOAD_DIR")}/$path/"
    }

    fun <T> decodeJsonList(string: String?): List<T>? {
        if (string.isNullOrBlank()) return null
        val list: List<T> = Json.decodeFromString(string)
        return list.ifEmpty { null }
    }

    fun getOffset(): ZoneOffset {
//        return ZoneOffset.UTC
        return ZoneId.of("Asia/Jakarta").rules.getOffset(Instant.now())
    }
}