package com.rollinup.server.datasource.apiservice.repository

import com.rollinup.server.datasource.apiservice.model.request.registration.GetAdminAccessTokenBody
import com.rollinup.server.datasource.apiservice.model.request.registration.GetRegistrationTokenParams
import com.rollinup.server.model.Result
import com.rollinup.server.model.register.AdminAuth
import com.rollinup.server.model.register.RegistrationAccess
import kotlinx.coroutines.flow.Flow

interface RegistrationRepository {
    fun getAccessToken(body: GetAdminAccessTokenBody): Flow<Result<AdminAuth>>
    fun getRegistrationToken(token: String): Flow<Result<RegistrationAccess>>
}