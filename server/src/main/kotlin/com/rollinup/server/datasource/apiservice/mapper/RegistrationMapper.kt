package com.rollinup.server.datasource.apiservice.mapper

import com.rollinup.server.datasource.apiservice.model.response.registration.GetAdminAccessTokenResponse
import com.rollinup.server.datasource.apiservice.model.response.registration.GetRegistrationAccessTokenResponse
import com.rollinup.server.model.register.AdminAuth
import com.rollinup.server.model.register.RegistrationAccess

class RegistrationMapper {
    fun mapAdminAuth(response: GetAdminAccessTokenResponse): AdminAuth {
        return AdminAuth(
            accessToken = response.accessToken,
            expiresIn = response.expiresIn
        )
    }

    fun mapRegistrationAccess(response: GetRegistrationAccessTokenResponse): RegistrationAccess {
        return RegistrationAccess(
            id = response.id,
            clientId = response.clientId,
            name = response.name,
            registrationAccessToken = response.registrationAccessToken
        )
    }
}