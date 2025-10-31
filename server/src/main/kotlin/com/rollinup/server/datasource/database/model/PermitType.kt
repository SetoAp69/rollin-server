package com.rollinup.server.datasource.database.model

enum class PermitType(val value: String) {
    DISPENSATION("dispensation"),
    ABSENCE("absence")
    ;

    companion object {
        fun fromValue(value: String): PermitType {
            return entries.find { it.value.equals(value,true) } ?: ABSENCE
        }
        fun like(search:String):List<PermitType>{
            if (search.isBlank()) return emptyList()
            return entries.filter { it.value.contains(search, ignoreCase = true) }
        }
    }
}