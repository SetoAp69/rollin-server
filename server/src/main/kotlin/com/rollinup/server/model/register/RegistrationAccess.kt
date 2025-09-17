package com.rollinup.server.model.register

data class RegistrationAccess(
    val id:String = "",
    val clientId:String = "",
    val name:String="",
    val registrationAccessToken:String = ""
)
