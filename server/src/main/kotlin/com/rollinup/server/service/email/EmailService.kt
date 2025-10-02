package com.rollinup.server.service.email

interface EmailService {
    fun sendEmail(
        receiver: String,
        message: String,
        subject: String = ""
    )
}