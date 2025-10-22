package com.rollinup.server.model.request.attendance

import kotlinx.serialization.Serializable

@Serializable
data class CreateAttendanceBody(
    val studentUserId: String = "",
    val location: Location = Location(),
) {
    @Serializable
    data class Location(
        val latitude: Double = 0.0,
        val longitude: Double = 0.0,
    )
}
