package com.rollinup.server.route.auth

import com.rollinup.server.model.auth.LoginRequest
import io.ktor.server.auth.OAuthAccessTokenResponse
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route

fun Route.authRoute() {

    authenticate("keycloakOAuth") {
        get("/login") {
            val principal: OAuthAccessTokenResponse.OAuth2? = call.principal()
            call.respondText("${principal?.accessToken}")
        }
        get("/"){

        }
    }
    authenticate("auth-jwt"){
        get("/auth"){
            val principal = call.principal<JWTPrincipal>()
            val username = principal!!.payload.getClaim("id").asString()
            val expiresAt = principal.expiresAt?.time?.minus(System.currentTimeMillis())
            call.respondText("Hello, $username! Token is expired at $expiresAt ms.")
        }
    }

    route("/login"){
       post {
           if(call.receive<LoginRequest>().jwt.isNotEmpty()){
                authenticate("jwt-auth") {

                }

           }else{

           }
       }
    }

}