package com.rollinup.server.model.request.permit

import io.ktor.server.plugins.requestvalidation.ValidationResult
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PermitApprovalBody(
    @SerialName("id")
    val listId: List<String> = emptyList(),
    @SerialName("approvalNote")
    val approvalNote: String = "",
    @SerialName("isApproved")
    val isApproved: Boolean? = null,
) {
    private object ValidationMessages {
        const val ID_BLANK_OR_NULL = "Id cannot be empty"
        const val NOTE_MAX_LENGTH = "Note max length is 120"
        const val APPROVAL_STATUS_INVALID = "Approval status is invalid"
    }

    fun validation(): ValidationResult {
        return when {
            listId.isEmpty() -> ValidationResult.Invalid(ValidationMessages.ID_BLANK_OR_NULL)
            approvalNote.length > 120 -> ValidationResult.Invalid(ValidationMessages.NOTE_MAX_LENGTH)
            isApproved == null -> ValidationResult.Invalid(ValidationMessages.APPROVAL_STATUS_INVALID)
            else -> ValidationResult.Valid
        }
    }
}
