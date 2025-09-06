package com.rollinup.server

import com.rollinup.server.configurations.module
import io.ktor.server.application.Application
import io.ktor.server.application.serverConfig
import io.ktor.server.engine.connector
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty

fun maiasdasdasdn(){
    embeddedServer(
        factory = Netty,
        serverConfig {
            developmentMode = true
            module(Application::module)
        },
        configure = {
            connector {
                host = "0.0.0.0"
                port = SERVER_PORT
            }
        },
    ).start(wait = true)

}

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

//fun Application.module() {
////    configureSocket()
////    install(plugin = ContentNegotiation){
////        json()
////    }
////    configureRouting()
//    val repository = TaskRepositoryImpl()
//
//    install(Koin){
//        modules(
//            taskDataModule
//        )
//    }
//
//    configureDatabase()
//    configureSerialization(repository)
//}

