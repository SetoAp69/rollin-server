package com.rollinup.server.model.request.permit

import com.rollinup.server.CommonException
import com.rollinup.server.datasource.database.model.ApprovalStatus
import com.rollinup.server.datasource.database.model.PermitType
import com.rollinup.server.util.Utils

data class CreatePermitBody(
    val studentId: String = "",
    val reason: String? = null,
    val duration: List<Long> = emptyList(),
    val type: PermitType = PermitType.DISPENSATION,
    val note: String? = null,
    val attachment: String = "",
    val approvalStatus: ApprovalStatus? = null,
    val approvedBy: String? = null,
    val approvedAt: Long? = null,
    val approvalNote: String? = null,
) {
    private object ValidationMessages {
        // Updated to be more granular based on the user's new checks
        const val ID_BLANK_OR_NULL = "Id cannot be empty"
        const val DURATION_INVALID = "Duration is invalid"
        const val TYPE_INVALID = "Permit type is Invalid"
    }

    companion object {
        fun fromHashMap(hashMap: HashMap<String, String>) =
            CreatePermitBody(
                studentId = hashMap.get("studentId").let {
                    if (it.isNullOrBlank())
                        throw CommonException(ValidationMessages.ID_BLANK_OR_NULL)
                    else it
                },
                reason = hashMap.get("reason"),
                duration = Utils.decodeJsonList<Long>(hashMap.get("duration"))
                    ?: throw CommonException(ValidationMessages.DURATION_INVALID),
                type = hashMap.get("type")?.let { PermitType.fromValue(it) }
                    ?: throw CommonException(ValidationMessages.TYPE_INVALID),
                note = hashMap.get("note"),
                approvalStatus = hashMap.get("approvalStatus")?.let { ApprovalStatus.fromValue(it) },
                approvedBy = hashMap.get("approvedBy"),
                approvedAt = hashMap.get("approvedAt")?.toLong(),
                approvalNote = hashMap.get("approvalNote")
            )
    }
}
