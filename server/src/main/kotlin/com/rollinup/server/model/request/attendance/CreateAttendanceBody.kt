package com.rollinup.server.model.request.attendance

import com.rollinup.server.CommonException
import com.rollinup.server.datasource.database.model.AttendanceStatus

data class CreateAttendanceBody(
    val studentUserId: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val attachment: String = "",
    val status: AttendanceStatus = AttendanceStatus.CHECKED_IN,
    val checkedInAt: Long = 0L,
) {


    private object ValidationMessages {
        const val ID_BLANK_OR_NULL = "Id cannot be empty"
        const val LOCATION_INVALID = "Location is invalid"
        const val CHECKED_IN_AT_INVALID = "Check in time is invalid"
    }

    companion object {
        fun fromHashMap(hashMap: HashMap<String, String>): CreateAttendanceBody {
            return CreateAttendanceBody(
                studentUserId = hashMap.get("studentUserId").let {
                    if (it.isNullOrBlank()) throw CommonException(ValidationMessages.ID_BLANK_OR_NULL)
                    else it
                },
                latitude = hashMap["latitude"]?.toDoubleOrNull()
                    ?: throw CommonException(ValidationMessages.LOCATION_INVALID),
                longitude = hashMap["longitude"]?.toDoubleOrNull()
                    ?: throw CommonException(ValidationMessages.LOCATION_INVALID),
                status = hashMap["status"].let {
                    if (it.isNullOrBlank()) AttendanceStatus.CHECKED_IN
                    else AttendanceStatus.fromValue(it)
                },
                checkedInAt = hashMap["checkedInAt"]?.toLongOrNull()
                    ?: throw CommonException(ValidationMessages.CHECKED_IN_AT_INVALID)
            )
        }
    }
}
