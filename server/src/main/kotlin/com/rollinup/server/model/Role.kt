package com.rollinup.server.model

enum class Role(val value: String) {
    STUDENT(
        value = "student"
    ),
    TEACHER(
        value = "teacher"
    ),
    ADMIN(
        value = "admin"
    )
    ;

    companion object {
        fun fromValue(value: String?): Role? {
            return entries.find { it.value == value }
        }
    }
}