package com.rollinup.server.util

object Message {
    const val LOGIN_SUCCESS = "Login success"
    const val VALIDATE_OTP_SUCCESS = "Success validate otp"
    const val FAILED_TO_SEND_EMAIL = "Failed to send email"
    const val EMAIL_SENT = "If email is registered to an account, an OTP will be sent to your email"
    const val EMAIL_ALREADY_SENT = "Email already sent"
    const val USER_NOT_FOUND = "User not found"
    const val TOKEN_NOT_FOUND = "Token not found"
    const val INVALID_TOKEN = "Invalid token"
    const val EXPIRED_TOKEN = "Token is Expired"
    const val UNAUTHORIZED_TOKEN = "Token is Unauthorized"
    const val INVALID_USERNAME_OR_PASSWORD = "Invalid username or password"
    const val UNAUTHORIZED_ACCESS = "Unauthorized access"
    const val EMAIL_USED = "Email is already used"
    const val USERNAME_USED = "Username is already used"
    const val CREATE_USER_SUCCESS = "User created successfully"
    const val EDIT_USER_SUCCESS = "User data updated successfully"
    const val INVALID_REQUEST = "Invalid request"
    const val ILLEGAL_ROLE = "User with this role don't have access to this route"
    const val INVALID_PATH_PARAMETER = "Invalid path parameter"
    const val INVALID_REQUEST_BODY = "Invalid request body"
    const val OUTSIDE_TIME_PERIOD = "Outside time period"
    const val INVALID_TIME_FORMAT = "Invalid time format"
    const val INVALID_LOCATION = "The given location is outside the range"
    const val STORAGE_CONNECTION_FAILED = "failed to connect with storage provider"
    const val INVALID_CONTENT_TYPE = "Invalid content type"
    const val INVALID_FILE_FORMAT = "Invalid file format"
    const val DEVICE_ALREADY_REGISTERED = "This account already have a registered device id"
    const val INVALID_CACHE = "There's a problem on getting cache data"
    const val INVALID_DURATIONS = "Invalid durations"
    const val INTERNAL_SERVER_ERROR = "Internal server error"

    fun getResetPasswordEmail(otp: String): String {
        return "This is your reset password verification code : <h2>$otp</h2> <br> this otp valid for 2 minutes"
    }

    fun getVerificationEmail(otp: String): String {
        return "This is your first time login on Rollin Up client, we require you to update your temporary password." +
                " Here's the OTP to update your password : <br> <h2>$otp</h2> <br> this otp valid for 2 minutes."
    }

    fun getAccountCreationEmail(email: String, password: String, username: String): String {
        return """
            <!-- Header -->
            <tr>
                <td style="background-color:#1f2937; padding:20px 24px;">
                    <h1 style="margin:0; font-size:20px; color:#ffffff;">
                        Account Successfully Created
                    </h1>
                </td>
            </tr>
        
            <!-- Content -->
            <tr>
                <td style="padding:24px; color:#111827; font-size:14px; line-height:1.6;">
                    <p>Hello <strong>$username</strong>,</p>
        
                    <p>
                        Your account has been successfully created. Below are your initial login credentials.
                        Please keep this information confidential.
                    </p>
        
                    <!-- Account Info Box -->
                    <table
                        width="100%"
                        cellpadding="0"
                        cellspacing="0"
                        style="margin:16px 0; background-color:#f9fafb; border:1px solid #e5e7eb; border-radius:4px;"
                    >
                        <tr>
                            <td style="padding:16px;">
                                <p style="margin:0 0 8px 0;">
                                    <strong>Username:</strong> $username
                                </p>
                                <p style="margin:0 0 8px 0;">
                                    <strong>Email:</strong> $email
                                </p>
                                <p style="margin:0;">
                                    <strong>Temporary Password:</strong> $password
                                </p>
                            </td>
                        </tr>
                    </table>
        
                    <p>
                        <strong>Important:</strong> For security reasons, we strongly recommend that you
                        change your password immediately after logging in for the first time.
                    </p>
        
                    <p>
                        If you did not request this account or believe this email was sent to you by mistake,
                        please contact our support team immediately.
                    </p>
        
                    <p style="margin-top:24px;">
                        Best regards,<br />
                        <strong>{{COMPANY_NAME}}</strong>
                    </p>
                </td>
            </tr>
        
            <!-- Footer -->
            <tr>
                <td
                    style="background-color:#f3f4f6; padding:16px 24px; font-size:12px; color:#6b7280;"
                >
                    <p style="margin:0;">
                        This is an automated message. Please do not reply to this email.
                    </p>
                </td>
            </tr>
        """.trimIndent()

    }


}