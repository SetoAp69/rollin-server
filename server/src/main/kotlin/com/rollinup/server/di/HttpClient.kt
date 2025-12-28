package com.rollinup.server.di

import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

object HttpClient {
    operator fun invoke() = module {
        singleOf(::getHttpClient)
    }
}

private fun getHttpClient(): HttpClient {
    return HttpClient {
        expectSuccess = true
        install(ContentNegotiation){
            json()
        }
    }
}