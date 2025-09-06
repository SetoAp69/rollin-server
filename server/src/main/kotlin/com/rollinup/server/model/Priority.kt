package com.rollinup.server.model

enum class Priority(
    val value:String
) {
    LOW("Low"), MEDIUM("Medium"), HIGH("High"), VITAL("Vital")

    ;

    companion object {
        fun fromValue(value: String): Priority {
            return entries.find { it.name.equals(value, true) } ?: LOW
        }
    }
}