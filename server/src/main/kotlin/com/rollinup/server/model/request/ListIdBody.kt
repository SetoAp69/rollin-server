package com.rollinup.server.model.request

import com.rollinup.server.model.request.ListIdBody.ValidationMessages.LIST_ID_BLANK
import io.ktor.server.plugins.requestvalidation.ValidationResult
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ListIdBody(
    @SerialName("listId")
    val listId: List<String> = emptyList(),
) {
    private object ValidationMessages {
        const val LIST_ID_BLANK = "List id cannot be empty."
    }

    fun validation(): ValidationResult {
        return when {
            listId.isEmpty() -> {
                ValidationResult.Invalid(LIST_ID_BLANK)
            }

            else -> ValidationResult.Valid
        }
    }
}
