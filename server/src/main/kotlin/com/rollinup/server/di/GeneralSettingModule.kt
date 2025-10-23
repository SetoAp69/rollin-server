package com.rollinup.server.di

import com.rollinup.server.util.generalsetting.GeneralSettingCache
import org.koin.dsl.module

object GeneralSettingModule {
    val module = module {
        single<GeneralSettingCache> {
            GeneralSettingCache()
        }

    }
}