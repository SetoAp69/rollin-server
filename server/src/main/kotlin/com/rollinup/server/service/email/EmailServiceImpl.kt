package com.rollinup.server.service.email

import com.rollinup.server.CommonException
import com.rollinup.server.util.Config
import com.rollinup.server.util.Message
import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class EmailServiceImpl(
    private val emailClient: HttpClient,
) : EmailService {

    override fun sendEmail(
        receiver: String,
        message: String,
        subject: String,
    ) {
        val url = Config.emailConfig.postUrl
        val config = Config.emailConfig

        val body = EmailBody(
            sender = EmailBody.Sender(
                name = config.senderName,
                email = config.sender
            ),
            to = listOf(
                EmailBody.To(
                    email = receiver
                )
            ),
            subject = subject,
            htmlContent = message
        )
        try {
            CoroutineScope(Dispatchers.IO).launch {
                emailClient.post(url) {
                    headers.append("api-key", config.token)
                    headers.append("content-type", ContentType.Application.Json.toString())
                    headers.append("accept", ContentType.Application.Json.toString())
                    setBody(body)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            throw CommonException(Message.FAILED_TO_SEND_EMAIL)
        }
    }
}