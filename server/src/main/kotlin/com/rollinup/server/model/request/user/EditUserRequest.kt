package com.rollinup.server.model.request.user

import com.rollinup.server.util.Utils.isEmail
import io.ktor.server.plugins.requestvalidation.ValidationResult
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class EditUserRequest(
    @SerialName("userName")
    val userName: String? = null,
    @SerialName("firstName")
    val firstName: String? = null,
    @SerialName("lastName")
    val lastName: String? = null,
    @SerialName("email")
    val email: String? = null,
    @SerialName("role")
    val role: String? = null,
    @SerialName("address")
    val address: String? = null,
    @SerialName("assignedClass")
    val assignedClass: String? = null,
    @SerialName("phoneNumber")
    val phoneNumber: String? = null,
    @SerialName("gender")
    val gender: String? = null,
    val salt: String? = null
) {
    private object ValidationMessages {
        // Updated to be more granular based on the user's new checks
        const val USERNAME_BLANK = "Username cannot be empty."
        const val FIRST_NAME_BLANK = "First name cannot be empty."
        const val LAST_NAME_BLANK = "Last name cannot be empty."
        const val INVALID_EMAIL_FORMAT = "Email address is invalid."
        const val ROLE_BLANK = "Role cannot be empty."
    }

    fun validation(): ValidationResult {
        return when {
            userName?.isBlank() ?: false -> ValidationResult.Invalid(ValidationMessages.USERNAME_BLANK)
            firstName?.isBlank() ?: false -> ValidationResult.Invalid(ValidationMessages.FIRST_NAME_BLANK)
            lastName?.isBlank() ?: false -> ValidationResult.Invalid(ValidationMessages.LAST_NAME_BLANK)
            email?.isEmail() ?: false -> ValidationResult.Invalid(ValidationMessages.INVALID_EMAIL_FORMAT)
            role?.isBlank() ?: false -> ValidationResult.Invalid(ValidationMessages.ROLE_BLANK)
            else -> ValidationResult.Valid
        }
    }


}
