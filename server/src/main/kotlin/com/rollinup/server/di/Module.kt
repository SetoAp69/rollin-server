package com.rollinup.server.di

import com.rollinup.server.configurations.Configuration
import com.rollinup.server.repository.task.TaskRepository
import com.rollinup.server.repository.task.TaskRepositoryImpl
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val appModule = module {
    singleOf(::TaskRepositoryImpl) bind TaskRepository::class
    single { Configuration() }
}