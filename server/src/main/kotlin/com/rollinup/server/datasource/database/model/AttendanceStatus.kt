package com.rollinup.server.datasource.database.model

enum class AttendanceStatus(val value: String) {
    CHECKED_IN("checked_in"),
    APPROVAL_PENDING("approval_pending"),
    ABSENT("absent"),
    EXCUSED("excused"),
    ALPHA("alpha"),
    LATE("late")
    ;

    companion object {
        fun fromValue(value: String): AttendanceStatus {
            return entries.find { it.value.equals(value, true) } ?: ALPHA
        }

        fun like(search: String): List<AttendanceStatus> {
            if (search.isBlank()) return emptyList()

            return entries.filter { it.value.contains(search, ignoreCase = true) }
        }
    }
}