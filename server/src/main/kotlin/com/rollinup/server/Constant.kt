package com.rollinup.server

object Constant {

    const val EMPTY_TOKEN_ERROR = "Error Token was Empty"
    const val RESPONSE_ERROR = "Error, failed to get response"
    const val DATABASE_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'"

    
    const val ACCESS_TOKEN_DURATION: Long = 600_000
    const val REFRESH_TOKEN_DURATION: Long = 864_000_000
    const val OTP_DURATION: Long = 120_000
}