package com.rollinup.server

import uk.org.webcompere.systemstubs.environment.EnvironmentVariables

fun mockkEnvironment() {
    val listVariables = listOf(
        "DB_URL",
        "DB_USERNAME",
        "DB_PASSWORD",
        "SMTP_PORT",
        "SMTP_HOST",
        "SMTP_USERNAME",
        "SMTP_PASSWORD",
        "SMTP_SENDER",
        "UPLOAD_DIR",
        "JWT_REALM",
        "JWT_ISSUER",
        "JWT_AUDIENCE",
        "JWT_SECRET"
    )

    listVariables.forEach { envVariable ->
        EnvironmentVariables(envVariable, envVariable).setup()
    }
}

class MockkEnvironment() {
    private val listVariables = listOf(
        "DB_URL",
        "DB_USERNAME",
        "DB_PASSWORD",
        "SMTP_PORT",
        "SMTP_HOST",
        "SMTP_USERNAME",
        "SMTP_PASSWORD",
        "SMTP_SENDER",
        "UPLOAD_DIR",
        "JWT_REALM",
        "JWT_ISSUER",
        "JWT_AUDIENCE",
        "JWT_SECRET"
    )

    val envVariables = listVariables.map {
        EnvironmentVariables(it, it)
    }


    fun setup() {
        envVariables.forEach {
            it.setup()
        }
    }

    fun teardown() {
        envVariables.forEach {
            it.teardown()
        }
    }
}