package com.rollinup.server

object Constant {

    const val DATABASE_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'"

    
    const val ACCESS_TOKEN_DURATION: Long = 600_000_000
    const val REFRESH_TOKEN_DURATION: Long = 864_000_000
    const val OTP_DURATION: Long = 120_000
}