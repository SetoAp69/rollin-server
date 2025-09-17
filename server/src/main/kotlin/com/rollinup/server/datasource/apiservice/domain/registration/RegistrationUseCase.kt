package com.rollinup.server.datasource.apiservice.domain.registration

import com.rollinup.server.datasource.apiservice.model.request.registration.GetAdminAccessTokenBody
import com.rollinup.server.datasource.apiservice.repository.RegistrationRepository

class GetAdminAccessTokenUseCase(
    private val repository: RegistrationRepository
) {
    operator fun invoke(body: GetAdminAccessTokenBody) = {
        repository.getAccessToken(body)
    }
}

class GetRegistrationAccessTokenUseCase(
    private val repository: RegistrationRepository
) {
    operator fun invoke(token: String) = {
        repository.getRegistrationToken(token)
    }
}

