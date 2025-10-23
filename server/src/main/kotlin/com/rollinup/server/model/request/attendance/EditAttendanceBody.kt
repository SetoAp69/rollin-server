package com.rollinup.server.model.request.attendance

import com.rollinup.server.datasource.database.model.AttendanceStatus


data class EditAttendanceBody(
    val location: Location = Location(),
    val status: AttendanceStatus? = null,
    val checkedInAt: Long? = null,
) {
    data class Location(
        val latitude: Double? = null,
        val longitude: Double? = null,
    )

    companion object {
        fun fromHashMap(hash: HashMap<String, String>): EditAttendanceBody {
            return EditAttendanceBody(
                location = Location(
                    latitude = hash.get("latitude")?.toDoubleOrNull(),
                    longitude = hash.get("longitude")?.toDoubleOrNull()
                ),
                status = hash.get("status")?.let { AttendanceStatus.fromValue(it) },
                checkedInAt = hash.get("checkedInAt")?.toLongOrNull()
            )

        }
    }
}
