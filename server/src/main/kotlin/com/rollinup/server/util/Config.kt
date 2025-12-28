package com.rollinup.server.util

import com.rollinup.server.service.jwt.TokenConfig


object Config {
    val emailConfig
        get() = EmailConfig(
            postUrl = System.getenv("EMAIL_POST_URL"),
            token = System.getenv("EMAIL_API_KEY"),
            sender = System.getenv("EMAIL_SENDER"),
            senderName = System.getenv("EMAIL_SENDER_NAME"),
        )

    fun getUploadDir(path: String): String {
        return "${System.getenv("UPLOAD_DIR")}/$path/"
    }

    fun getTokenConfig(): TokenConfig {
        return TokenConfig(
            issuer = System.getenv("JWT_ISSUER"),
            audience = System.getenv("JWT_AUDIENCE"),
            expiresIn = 0L,
            secret = System.getenv("JWT_SECRET"),
            realm = System.getenv("JWT_REALM")
        )
    }

    fun getDbConfig() = DBConfig(
        url = System.getenv("DB_URL"),
        username = System.getenv("DB_USERNAME"),
        password = System.getenv("DB_PASSWORD")
    )

    fun getGCSConfig() = GCSConfig(
        bucketName = System.getenv("GCS_BUCKET"),
        projectId = System.getenv("GC_PROJECT")
    )
}

data class EmailConfig(
    val postUrl :String= "",
    val token:String = "",
    val sender:String = "",
    val senderName:String = ""
)

data class DBConfig(
    val url: String = "",
    val username: String = "",
    val password: String = "",
)

data class GCSConfig(
    val bucketName: String = "",
    val projectId: String = "",
)