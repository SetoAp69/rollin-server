plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.ktor)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kotlinx.kover)
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

kover {
    val excludedPackages = listOf(
        "com.rollinup.server.util.*",
        "com.rollinup.server.socket.*",
        "com.rollinup.server.*.di.*",
        "com.rollinup.server.configurations.*",
        "com.rollinup.server.datasource.*",
        "kotlinx.coroutines.*",
    )

    val includedPackages = listOf(
        "com.rollinup.server.model.*",
        "com.rollinup.server.route.*",
        "com.rollinup.server.service.*"
    )

    reports{
        filters {
            excludes {
                packages(
                    excludedPackages
                )
            }
            includes {
                packages(
                    includedPackages
                )
            }
        }

    }



}

dependencies {

    implementation(libs.logback)

    //Ktor Server
    implementation(libs.ktor.serverCore)
    implementation(libs.ktor.serverNetty)
    implementation(libs.ktor.server.status.pages)
    implementation(libs.ktor.server.call.logging)
    implementation(libs.ktor.server.request.validation)

    //Engine
    implementation(libs.ktor.client.apache)
    implementation(libs.ktor.client.cio)
    implementation(libs.ktor.client.logging)

    //Auth
    implementation(libs.ktor.auth)
    implementation(libs.ktor.auth.jwt)


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
    implementation(libs.exposed.java.time)

    // Koin
    implementation(libs.koin.core)
    implementation(libs.koin.ktor)
    implementation(libs.koin.logger)

    //Gson
    implementation(libs.gson)

    //DB DRiver
//    implementation("org.postgresql:postgresql:42.7.3")
    implementation(libs.postgresql)

    //Email
    implementation(libs.common.email)

    //Testing
    testImplementation(libs.kotlin.testJunit)
    testImplementation(libs.ktor.serverTestHost)
    testImplementation(libs.json.path)
    testImplementation(libs.mockk)

}

