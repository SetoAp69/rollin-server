package com.rollinup.server.datasource.database.model.user

enum class Gender(val value: String, val label: String) {
    M(
        value = "M",
        label = "Male"
    ),
    F(
        value = "F",
        label = "Female"
    )
    ;

    companion object {
        fun fromValue(value: String): Gender {
            return entries.find { it.value == value } ?: M
        }
    }
}