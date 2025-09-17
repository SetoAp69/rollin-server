package com.rollinup.server.datasource.apiservice.datasource

import com.rollinup.server.datasource.apiservice.model.request.registration.GetAdminAccessTokenBody
import com.rollinup.server.datasource.apiservice.model.response.registration.GetAdminAccessTokenResponse
import com.rollinup.server.datasource.apiservice.model.response.registration.GetRegistrationAccessTokenResponse

interface RegistrationApi {
    suspend fun getAccessToken(body: GetAdminAccessTokenBody): GetAdminAccessTokenResponse
    suspend fun getRegistrationToken(token: String): GetRegistrationAccessTokenResponse
}