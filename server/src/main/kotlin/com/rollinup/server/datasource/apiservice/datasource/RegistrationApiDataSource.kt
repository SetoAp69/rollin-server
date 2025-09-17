package com.rollinup.server.datasource.apiservice.datasource

import com.rollinup.server.configurations.Configuration
import com.rollinup.server.datasource.apiservice.model.request.registration.GetAdminAccessTokenBody
import com.rollinup.server.datasource.apiservice.model.response.registration.GetAdminAccessTokenResponse
import com.rollinup.server.datasource.apiservice.model.response.registration.GetRegistrationAccessTokenResponse
import com.rollinup.server.util.Utils.handleBody
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.URLProtocol
import io.ktor.http.path

class RegistrationApiDataSource(
    private val client: HttpClient,
    config: Configuration
) : RegistrationApi {

    val registrationHost = config.fetchProperty("KEYCLOAK_ADMIN_BASE_URL")
    val realms = config.fetchProperty("KEYCLOAK_REALMS")
    val masterRealms = config.fetchProperty("KEYCLOAK_MASTER_REALMS")

    override suspend fun getAccessToken(
        body: GetAdminAccessTokenBody
    ): GetAdminAccessTokenResponse {
        return client.post {
            url {
                protocol = URLProtocol.HTTPS
                host = registrationHost
                path("admin/realm/$masterRealms/protocol/openid-connect/token")
                setBody(body)
            }
        }.handleBody()

    }

    override suspend fun getRegistrationToken(
        token: String
    ): GetRegistrationAccessTokenResponse {
        return client.post {
            url {
                protocol = URLProtocol.HTTPS
                host = registrationHost
                path("")
                bearerAuth(token)
            }
        }.body()
    }


}