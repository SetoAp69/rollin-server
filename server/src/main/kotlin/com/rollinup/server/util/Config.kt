package com.rollinup.server.util

import com.rollinup.server.service.jwt.TokenConfig
import com.typesafe.config.ConfigFactory
import io.ktor.server.config.HoconApplicationConfig

object Config {
    val smtpConfig
        get() = SmtpConfig(
            port = System.getenv("SMTP_PORT").toIntOrNull() ?: 457,
                hostName = System.getenv("SMTP_HOST_NAME"),
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
}


data class SmtpConfig(
    val port: Int = 0,
    val hostName: String = "",
    val userName: String = "",
    val password: String = "",
    val sender: String = ""
)

data class DBConfig(
    val url: String = "",
    val username: String = "",
    val password: String = ""
)