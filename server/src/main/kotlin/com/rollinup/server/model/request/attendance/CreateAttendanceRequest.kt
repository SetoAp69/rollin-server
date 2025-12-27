package com.rollinup.server.model.request.attendance

import com.rollinup.server.model.request.attendance.CreateAttendanceRequest.ValidationMessages.DATE_BLANK
import com.rollinup.server.model.request.attendance.CreateAttendanceRequest.ValidationMessages.ID_BLANK
import io.ktor.server.plugins.requestvalidation.ValidationResult
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.LocalDate

@Serializable
data class CreateAttendanceRequest(
    @SerialName("id")
    val id: String = "",
    @SerialName("checkInAt")
    val checkInAt: Long = 0L,
    @SerialName("date")
    val sDate: String = "",
) {
    val date: LocalDate
        get() = LocalDate.parse(sDate)

    private object ValidationMessages {
        const val ID_BLANK = "Id cannot be empty."
        const val DATE_BLANK = "Date cannot be empty"
    }

    fun validation(): ValidationResult {
        return when {
            id.isBlank() -> {
                ValidationResult.Invalid(ID_BLANK)
            }

            sDate.isBlank() -> {
                ValidationResult.Invalid(DATE_BLANK)
            }

            else -> {
                ValidationResult.Valid
            }
        }
    }
}

