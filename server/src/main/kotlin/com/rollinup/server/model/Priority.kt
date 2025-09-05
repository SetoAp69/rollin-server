package com.rollinup.server.model

enum class Priority {
    LOW, MEDIUM, HIGH, VITAL

    ;

    companion object {
        fun fromValue(value: String): Priority {
            return entries.find { it.name.equals(value, true) } ?: LOW
        }
    }
}