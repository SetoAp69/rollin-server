package com.rollinup.server.model.request.user

import com.rollinup.server.util.Utils.isEmail
import com.rollinup.server.util.Utils.toLocalDate
import io.ktor.server.plugins.requestvalidation.ValidationResult
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RegisterUserRequest(
    @SerialName("username")
    val userName: String = "",
    @SerialName("fullName")
    val fullname: String = "",
    @SerialName("email")
    val email: String = "",
    @SerialName("studentId")
    val studentId: String? = null,
    @SerialName("role")
    val role: String = "",
    @SerialName("address")
    val address: String = "",
    @SerialName("class")
    val classX: String? = null,
    @SerialName("phoneNumber")
    val phoneNumber: String = "",
    @SerialName("birthday")
    val birthday: Long = 0L,
    @SerialName("gender")
    val gender: String = "",
    val salt: String = "",
    val password:String = ""
) {
    val birthdayDate
        get() = birthday.toLocalDate()


    private object ValidationMessages {
        // Updated to be more granular based on the user's new checks
        const val USERNAME_BLANK = "Username cannot be empty."
//        const val FIRST_NAME_BLANK = "First name cannot be empty."
        const val LAST_NAME_BLANK = "Full name cannot be empty."
        const val INVALID_EMAIL_FORMAT = "Email address is invalid."
        const val ROLE_BLANK = "Role cannot be empty."
    }

    fun validation(): ValidationResult {
        return when {
            userName.isBlank() -> {
                ValidationResult.Invalid(ValidationMessages.USERNAME_BLANK)
            }

            fullname.isBlank() -> {
                ValidationResult.Invalid(ValidationMessages.LAST_NAME_BLANK)
            }

            !email.isEmail() -> {
                ValidationResult.Invalid(ValidationMessages.INVALID_EMAIL_FORMAT)
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
