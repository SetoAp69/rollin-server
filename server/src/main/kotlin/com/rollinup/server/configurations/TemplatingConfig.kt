package com.rollinup.server.configurations

import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.thymeleaf.Thymeleaf
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver

fun Application.configureTemplating() {
    install(Thymeleaf) {
        setTemplateResolver(ClassLoaderTemplateResolver().apply {
            prefix = "map/"
            suffix = ".html"
        })
    }
}