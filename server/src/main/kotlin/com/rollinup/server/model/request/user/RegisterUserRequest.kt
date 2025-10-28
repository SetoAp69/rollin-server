package com.rollinup.server.model.request.user

import com.rollinup.server.util.Utils.isEmail
import com.rollinup.server.util.Utils.validatePassword
import io.ktor.server.plugins.requestvalidation.ValidationResult
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RegisterUserRequest(
    @SerialName("userName")
    val userName: String = "",
    @SerialName("firstName")
    val firstName: String = "",
    @SerialName("lastName")
    val lastName: String = "",
    @SerialName("email")
    val email: String = "",
    @SerialName("password")
    val password: String = "",
    @SerialName("role")
    val role: String = "",
    @SerialName("address")
    val address: String = "",
    @SerialName("assignedClass")
    val assignedClass: String = "",
    @SerialName("phoneNumber")
    val phoneNumber: String = "",
    @SerialName("gender")
    val gender: String = "",
    val salt: String = ""

) {
    private object ValidationMessages {
        // Updated to be more granular based on the user's new checks
        const val USERNAME_BLANK = "Username cannot be empty."
        const val FIRST_NAME_BLANK = "First name cannot be empty."
        const val LAST_NAME_BLANK = "Last name cannot be empty."
        const val PASSWORD_INVALID = "Password format is invalid."
        const val INVALID_EMAIL_FORMAT = "Email address is invalid."
        const val ROLE_BLANK = "Role cannot be empty."
    }

    fun validation(): ValidationResult {
        return when {
            userName.isBlank() -> {
                ValidationResult.Invalid(ValidationMessages.USERNAME_BLANK)
            }

            firstName.isBlank() -> {
                ValidationResult.Invalid(ValidationMessages.FIRST_NAME_BLANK)
            }

            lastName.isBlank() -> {
                ValidationResult.Invalid(ValidationMessages.LAST_NAME_BLANK)
            }

            !email.isEmail() -> {
                ValidationResult.Invalid(ValidationMessages.INVALID_EMAIL_FORMAT)
            }

            password.validatePassword() -> {
                ValidationResult.Invalid(ValidationMessages.PASSWORD_INVALID)
            }

            role.isBlank() -> {
                ValidationResult.Invalid(ValidationMessages.ROLE_BLANK)
            }

            else -> {
                ValidationResult.Valid
            }
        }
    }

}
