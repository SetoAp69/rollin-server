package com.rollinup.server.util

import com.rollinup.server.CommonException
import com.rollinup.server.Constant
import com.rollinup.server.IllegalFileFormat
import io.ktor.http.ContentType
import io.ktor.http.content.PartData
import io.ktor.util.cio.writeChannel
import io.ktor.utils.io.copyAndClose
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.File
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.UUID
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

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
        val formatter = DateTimeFormatter.ofPattern(pattern).withZone(getOffset())
        val formattedDate = LocalDateTime.parse(this, formatter)

        return formattedDate
    }


    fun Long.toFormattedDateString(): String {
        val instant = Instant.ofEpochMilli(this)
        val formatter = DateTimeFormatter.ofPattern(Constant.DATABASE_DATE_FORMAT)
            .withZone(ZoneOffset.UTC)
        return formatter.format(instant)
    }

    fun getOffsetDateTime(epochMilli: Long, offset: ZoneOffset = getOffset()): OffsetDateTime {
        return OffsetDateTime.ofInstant(
            Instant.ofEpochMilli(epochMilli),
            offset
        )
    }

    fun getLocalTime(epochMilli: Long): LocalTime {
        return LocalTime.ofSecondOfDay(epochMilli / 1000)

    }

    fun generateDateRange(start: OffsetDateTime, end: OffsetDateTime): List<LocalDate> {
        val dates = mutableListOf<LocalDate>()
        var currentDate = start.toLocalDate()
        val endDate = end.toLocalDate()

        while (currentDate <= endDate) {
            dates.add(currentDate)
            currentDate = currentDate.plusDays(1)
        }
        return dates
    }

    fun getUploadDir(path: String, fileName: String): String {
        val date = LocalDate
            .ofInstant(Instant.now(), getOffset())
            .toString()
            .replace("-", "")
        return "${System.getenv("UPLOAD_DIR")}/$path/$date/$fileName"
    }

    fun getCacheDir(path: String, fileName: String): String {
        return "${System.getenv("CACHE_DIR")}/$path/$fileName"
    }

    inline fun <reified T> decodeJsonList(string: String?): List<T>? {
        if (string.isNullOrBlank()) return null
        val list: List<T> = Json.decodeFromString(string)
        return list.ifEmpty { null }
    }

    fun getOffset(): ZoneOffset {
//        return ZoneOffset.UTC
        return ZoneId.of("Asia/Jakarta").rules.getOffset(Instant.now())
    }

    private fun String?.formatFileName(customName: String): String {
        val format = this?.substringAfterLast(".") ?: ""
        return "$customName-${UUID.randomUUID()}.$format".trimEnd('.')
    }

    fun fetchFormData(
        partData: PartData.FormItem,
        hashMap: HashMap<String, String>,
    ) {
        partData.name?.let {
            hashMap[it] = partData.value
        }
    }

    suspend fun fetchFileData(
        partData: PartData.FileItem,
        hashMap: HashMap<String, File>,
        customName: String = "",
    ) {
        withContext(Dispatchers.IO) {
            partData.name?.let {
                val fileName = partData.originalFileName.formatFileName(customName)
                val allowedType = listOf(
                    ContentType.Application.Pdf,
                    ContentType.Image.JPEG,
                    ContentType.Image.PNG
                )
                if (partData.contentType !in allowedType) {
                    throw CommonException(Message.INVALID_FILE_FORMAT)
                }
                val cacheDir =
                    getCacheDir(path = Constant.UPDATE_FILE_PATH, fileName = fileName)
                val cache = File(cacheDir).apply { parentFile?.mkdirs() }
                partData.provider().copyAndClose(cache.writeChannel())

                hashMap[it] = cache
            }
        }
    }

    fun getHaversine(
        locationA: Pair<Double, Double>,
        locationB: Pair<Double, Double>,
    ): Double {
        val earthRad = 6371
        val deltaLat = Math.toRadians(locationA.first - locationB.first)
        val deltaLong = Math.toRadians(locationA.second - locationB.second)

        val latA = Math.toRadians(locationA.first)
        val latB = Math.toRadians(locationB.first)

        val a = sin(deltaLat / 2).pow(2.0) +
                sin(deltaLong / 2).pow(2.0) *
                cos(latA) * cos(latB)

        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        val finalDistance = c * earthRad

        return finalDistance
    }

    fun validateLocations(
        locationA: Pair<Double, Double>,
        locationB: Pair<Double, Double>,
        rad: Double,
    ): Boolean {

        val finalDistance = getHaversine(locationA, locationB)
        return finalDistance <= rad
    }

    fun String.toLocalTime(): LocalTime {
        return Instant.parse(this).let {
            LocalTime.ofInstant(it, getOffset())
        }
    }

    fun getContentType(extension: String): ContentType {
        return when (extension.lowercase()) {
            "png" -> ContentType.Image.PNG
            "jpg", "jpeg" -> ContentType.Image.JPEG
            "pdf" -> ContentType.Application.Pdf
            else -> throw IllegalFileFormat()
        }
    }

}