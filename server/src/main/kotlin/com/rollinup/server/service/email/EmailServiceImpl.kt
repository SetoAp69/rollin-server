package com.rollinup.server.service.email

import com.rollinup.server.CommonException
import com.rollinup.server.util.Config
import com.rollinup.server.util.Message
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.apache.commons.mail2.core.EmailException
import org.apache.commons.mail2.javax.SimpleEmail

class EmailServiceImpl() : EmailService {
    override fun sendEmail(
        receiver: String,
        message: String,
        subject: String
    ) {
        try {
            CoroutineScope(Dispatchers.IO).launch {
                val port = Config.smtpConfig.port
                val hostName = Config.smtpConfig.hostName
                val username = Config.smtpConfig.userName
                val password = Config.smtpConfig.password
                val sender = Config.smtpConfig.sender

                val email = SimpleEmail()

                with(email) {
                    this.hostName = hostName
                    setSmtpPort(port)
                    setAuthentication(username, password)
                    isStartTLSEnabled = true
                    setFrom(sender)
                    addTo(receiver)
                    setMsg(message)
                    this.subject = subject
                    send()
                }
            }

        } catch (e: EmailException) {
            throw CommonException(Message.FAILED_TO_SEND_EMAIL)
        }

    }
}