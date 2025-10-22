package com.rollinup.server.model

enum class Status(val value:String) {
    CHECKED_IN("checked_in"),
    APPROVAL_PENDING ("approval_pending"),
    ABSENT("absent"),
    EXCUSED("excused"),
    ALPHA("alpha"),
    NO_DATA("no data")

    ;
    companion object {
        fun fromValue(value:String): Status{
            return entries.find { it.value.equals(value, true)}?:NO_DATA
        }

        fun like(query:String):List<Status>{
            return entries.filter { it.value.contains(query) }
        }
    }
}