package com.rollinup.server.model.request.permit

import com.rollinup.server.datasource.database.model.ApprovalStatus
import com.rollinup.server.datasource.database.model.PermitType
import com.rollinup.server.util.Utils

data class EditPermitBody(
    val duration: List<Long>? = null,
    val reason: String? = null,
    val type: PermitType? = null,
    val attachment: String? = null,
    val note: String? = null,
    val approvedBy: String? = null,
    val approvalNote: String? = null,
    val approvalStatus: ApprovalStatus? = null,
    val approvedAt:Long? = null
) {
    companion object {
        fun fromHashMap(
            hash: HashMap<String, String>,
        ) = EditPermitBody(
            duration = hash.get("duration")?.let { Utils.decodeJsonList(it) },
            reason = hash.get("reason"),
            attachment = hash.get("attachment"),
            note = hash.get("note"),
            type = hash.get("type")?.let { PermitType.valueOf(it) }
        )
    }
}
