package com.rollinup.server.util

import com.rollinup.server.service.jwt.TokenConfig

object Config {
    val smtpConfig
        get() = SmtpConfig(
            port = System.getenv("SMTP_PORT").toIntOrNull() ?: 457,
            hostName = System.getenv("SMTP_HOST"),
            userName = System.getenv("SMTP_USERNAME"),
            password = System.getenv("SMTP_PASSWORD"),
            sender = System.getenv("SMTP_SENDER")
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


data class SmtpConfig(
    val port: Int = 0,
    val hostName: String = "",
    val userName: String = "",
    val password: String = "",
    val sender: String = "",
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