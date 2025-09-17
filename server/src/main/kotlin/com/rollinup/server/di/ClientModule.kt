package com.rollinup.server.di

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import org.koin.dsl.module

object ClientModule {
    val module = module {
        single {
            HttpClient(engineFactory = CIO) {
                install(plugin = ContentNegotiation) {
                    json()
                }
                install(plugin = Logging) {
                    level = LogLevel.ALL
                }
                HttpResponseValidator {
                    validateResponse { response ->
                        val error = response.status
                        if (error.value !in 200..299) {
                            throw Exception(error.description)
                        }
                    }
                }
            }
        }
    }

}