package com.rollinup.server

object Constant {

    const val DATABASE_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSSSSXXX"

    //TODO:Replace the access token for prod
    const val ACCESS_TOKEN_DURATION: Long = 600_000_000_000
    const val REFRESH_TOKEN_DURATION: Long = 864_000_000
    const val OTP_DURATION: Long = 120_000

    const val ATTENDANCE_FILE_PATH:String = "attachment/attendance"
    const val PERMIT_FILE_PATH:String = "attachment/permit"
    const val UPDATE_FILE_PATH:String = "update"
}
