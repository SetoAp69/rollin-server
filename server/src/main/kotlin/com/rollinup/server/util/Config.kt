package com.rollinup.server.util

import com.rollinup.server.service.jwt.TokenConfig
import com.typesafe.config.ConfigFactory
import io.ktor.server.config.HoconApplicationConfig

object Config {
    private val config by lazy {
        HoconApplicationConfig(ConfigFactory.load())
    }

    fun fetchProperty(key: String) = config.property(key).getString()

    val smtpConfig
        get() = SmtpConfig(
            port = fetchProperty("SMTP_PORT").toIntOrNull() ?: 457,
            hostName = fetchProperty("SMTP_HOST_NAME"),
            userName = fetchProperty("SMTP_USERNAME"),
            password = fetchProperty("SMTP_PASSWORD"),
            sender = fetchProperty("SMTP_SENDER")
        )


    fun getUploadDir(path: String): String {
        return "${fetchProperty("UPLOAD_DIR")}/$path/"
    }

    fun getTokenConfig(): TokenConfig {
        return TokenConfig(
            issuer = fetchProperty("JWT_ISSUER"),
            audience = fetchProperty("JWT_AUDIENCE"),
            expiresIn = 0L,
            secret = fetchProperty("JWT_SECRET"),
            realm = fetchProperty("JWT_REALM")
        )
    }

    fun getDbConfig() = DBConfig(
        url = fetchProperty("DATABASE_URL"),
        username = fetchProperty("DATABASE_USERNAME"),
        password = fetchProperty("DATABASE_PASSWORD")
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