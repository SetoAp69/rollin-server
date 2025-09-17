import java.util.Properties

plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.ktor)
    alias(libs.plugins.kotlin.serialization)
    application
}

group = "com.rollinup.server"
version = "1.0.0"


project.ext.set("development", "development")

application {

    ktor {
        development = true

    }


    mainClass.set("com.rollinup.server.ApplicationKt")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

dependencies {

    implementation(libs.logback)

    //Ktor Server
    implementation(libs.ktor.serverCore)
    implementation(libs.ktor.serverNetty)

    //Engine
    implementation(libs.ktor.client.apache)
    implementation(libs.ktor.client.cio)

    //Auth
    implementation(libs.ktor.auth)
    implementation(libs.ktor.auth.jwt)

    //Call Logging
    implementation(libs.ktor.server.call.logging)
    implementation(libs.ktor.client.logging)

    //web socket
    implementation(libs.ktor.websockets)

    //Serialization
    implementation(libs.ktor.serialization.json)

    //Negotiation
    implementation(libs.ktor.content.negotiation)
    implementation(libs.ktor.content.negotiation.client)

    //Exposed
    implementation(libs.exposed.core)
    implementation(libs.exposed.jdbc)
    implementation(libs.exposed.r2dbc)
    implementation(libs.h2)
    implementation(libs.exposed.dao)

    // Koin
    implementation(libs.koin.core)
    implementation(libs.koin.ktor)
    implementation(libs.koin.logger)

    //Gson
    implementation(libs.gson)

    //DB DRiver
//    implementation("org.postgresql:postgresql:42.7.3")
    implementation(libs.postgresql)

    //Testing
    testImplementation(libs.kotlin.testJunit)
    testImplementation(libs.ktor.serverTestHost)
    testImplementation(libs.json.path)

}

