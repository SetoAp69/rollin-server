package com.rollinup.server.datasource.apiservice.model.response.registration


import com.google.gson.annotations.SerializedName

data class GetRegistrationAccessTokenResponse(
    @SerializedName("id")
    val id: String = "",
    @SerializedName("clientId")
    val clientId: String = "",
    @SerializedName("name")
    val name: String = "",
    @SerializedName("description")
    val description: String = "",
    @SerializedName("rootUrl")
    val rootUrl: String = "",
    @SerializedName("adminUrl")
    val adminUrl: String = "",
    @SerializedName("baseUrl")
    val baseUrl: String = "",
    @SerializedName("surrogateAuthRequired")
    val surrogateAuthRequired: Boolean = false,
    @SerializedName("enabled")
    val enabled: Boolean = false,
    @SerializedName("alwaysDisplayInConsole")
    val alwaysDisplayInConsole: Boolean = false,
    @SerializedName("clientAuthenticatorType")
    val clientAuthenticatorType: String = "",
    @SerializedName("secret")
    val secret: String = "",
    @SerializedName("registrationAccessToken")
    val registrationAccessToken: String = "",
    @SerializedName("redirectUris")
    val redirectUris: List<String> = listOf(),
    @SerializedName("webOrigins")
    val webOrigins: List<String> = listOf(),
    @SerializedName("notBefore")
    val notBefore: Int = 0,
    @SerializedName("bearerOnly")
    val bearerOnly: Boolean = false,
    @SerializedName("consentRequired")
    val consentRequired: Boolean = false,
    @SerializedName("standardFlowEnabled")
    val standardFlowEnabled: Boolean = false,
    @SerializedName("implicitFlowEnabled")
    val implicitFlowEnabled: Boolean = false,
    @SerializedName("directAccessGrantsEnabled")
    val directAccessGrantsEnabled: Boolean = false,
    @SerializedName("serviceAccountsEnabled")
    val serviceAccountsEnabled: Boolean = false,
    @SerializedName("authorizationServicesEnabled")
    val authorizationServicesEnabled: Boolean = false,
    @SerializedName("publicClient")
    val publicClient: Boolean = false,
    @SerializedName("frontchannelLogout")
    val frontchannelLogout: Boolean = false,
    @SerializedName("protocol")
    val protocol: String = "",
    @SerializedName("attributes")
    val attributes: Attributes = Attributes(),
    @SerializedName("authenticationFlowBindingOverrides")
    val authenticationFlowBindingOverrides: AuthenticationFlowBindingOverrides = AuthenticationFlowBindingOverrides(),
    @SerializedName("fullScopeAllowed")
    val fullScopeAllowed: Boolean = false,
    @SerializedName("nodeReRegistrationTimeout")
    val nodeReRegistrationTimeout: Int = 0,
    @SerializedName("defaultClientScopes")
    val defaultClientScopes: List<String> = listOf(),
    @SerializedName("optionalClientScopes")
    val optionalClientScopes: List<String> = listOf()
) {
    data class Attributes(
        @SerializedName("request.object.signature.alg")
        val requestObjectSignatureAlg: String = "",
        @SerializedName("client.secret.creation.time")
        val clientSecretCreationTime: String = "",
        @SerializedName("request.object.encryption.alg")
        val requestObjectEncryptionAlg: String = "",
        @SerializedName("client.introspection.response.allow.jwt.claim.enabled")
        val clientIntrospectionResponseAllowJwtClaimEnabled: String = "",
        @SerializedName("standard.token.exchange.enabled")
        val standardTokenExchangeEnabled: String = "",
        @SerializedName("frontchannel.logout.session.required")
        val frontchannelLogoutSessionRequired: String = "",
        @SerializedName("oauth2.device.authorization.grant.enabled")
        val oauth2DeviceAuthorizationGrantEnabled: String = "",
        @SerializedName("use.jwks.url")
        val useJwksUrl: String = "",
        @SerializedName("backchannel.logout.revoke.offline.tokens")
        val backchannelLogoutRevokeOfflineTokens: String = "",
        @SerializedName("use.refresh.tokens")
        val useRefreshTokens: String = "",
        @SerializedName("realm_client")
        val realmClient: String = "",
        @SerializedName("oidc.ciba.grant.enabled")
        val oidcCibaGrantEnabled: String = "",
        @SerializedName("client.use.lightweight.access.token.enabled")
        val clientUseLightweightAccessTokenEnabled: String = "",
        @SerializedName("backchannel.logout.session.required")
        val backchannelLogoutSessionRequired: String = "",
        @SerializedName("request.object.required")
        val requestObjectRequired: String = "",
        @SerializedName("client_credentials.use_refresh_token")
        val clientCredentialsUseRefreshToken: String = "",
        @SerializedName("jwks.url")
        val jwksUrl: String = "",
        @SerializedName("access.token.header.type.rfc9068")
        val accessTokenHeaderTypeRfc9068: String = "",
        @SerializedName("tls.client.certificate.bound.access.tokens")
        val tlsClientCertificateBoundAccessTokens: String = "",
        @SerializedName("require.pushed.authorization.requests")
        val requirePushedAuthorizationRequests: String = "",
        @SerializedName("acr.loa.map")
        val acrLoaMap: String = "",
        @SerializedName("display.on.consent.screen")
        val displayOnConsentScreen: String = "",
        @SerializedName("request.object.encryption.enc")
        val requestObjectEncryptionEnc: String = "",
        @SerializedName("token.response.type.bearer.lower-case")
        val tokenResponseTypeBearerLowerCase: String = ""
    )

    class AuthenticationFlowBindingOverrides
}