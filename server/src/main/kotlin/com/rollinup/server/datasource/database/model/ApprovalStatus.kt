package com.rollinup.server.datasource.database.model

enum class ApprovalStatus(val value:String) {
    APPROVAL_PENDING("approval_pending"),
    APPROVED("approved"),
    DECLINED("declined"),
    CANCELED("canceled")

    ;
    companion object{
        fun fromValue(value:String): ApprovalStatus{
            return entries.find { it.value.equals(value,true) }?:APPROVAL_PENDING
        }

        fun like(search:String):List<ApprovalStatus>{
            return entries.filter { it.value.contains(search, ignoreCase = true) }
        }
    }
}