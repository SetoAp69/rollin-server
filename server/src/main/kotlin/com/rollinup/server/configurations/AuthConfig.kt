package com.rollinup.server.configurations

import com.auth0.jwk.JwkProviderBuilder
import io.ktor.client.HttpClient
import io.ktor.client.engine.apache.Apache
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.URLBuilder
import io.ktor.http.toURI
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.OAuthServerSettings
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.auth.oauth
import io.ktor.server.response.respond

fun Application.configureAuthentication() {
    val jwkEndpointUrl = URLBuilder("http://127.0.0.1:8080/realms/test/protocol/openid-connect/certs").build().toURI().toURL() /*TODO replace with env value*/
    val jwkProvider = JwkProviderBuilder(jwkEndpointUrl).build()

    install(Authentication) {
        oauth("keycloakOAuth") {
            client = HttpClient(
                engineFactory = Apache
            )
            providerLookup = {
                OAuthServerSettings.OAuth2ServerSettings(
                    name = "keycloak",
                    authorizeUrl = "http://127.0.0.1:8080/realms/test/protocol/openid-connect/auth" /*TODO replace with env value*/,
                    accessTokenUrl = "http://127.0.0.1:8080/realms/test/protocol/openid-connect/token" /*TODO replace with env value*/,
                    clientId = "rollin_test",
                    clientSecret = "",
                    accessTokenRequiresBasicAuth = false,
                    requestMethod = HttpMethod.Post,
                    defaultScopes = listOf("roles")
                )
            }
            urlProvider = {
                "http://127.0.0.1:8089/tasks" /*TODO replace with env value*/
            }
        }

        jwt("auth-jwt") {
            realm = "test"
            verifier(jwkProvider = jwkProvider) {
                withIssuer("http://127.0.0.1:8080/realms/test") /*TODO replace with env value*/
            }

            validate{cred->
                JWTPrincipal(cred.payload)
            }
            challenge { defaultScheme, realm ->
                call.respond(HttpStatusCode.Unauthorized, "Token is Not Authorized")
            }
        }
    }


}