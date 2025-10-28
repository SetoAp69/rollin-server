package com.rollinup.server.di

import com.rollinup.server.generalsetting.GeneralSettingCache
import com.rollinup.server.generalsetting.GeneralSettingEventBus
import com.rollinup.server.generalsetting.GeneralSettingListener
import org.koin.dsl.module

object GeneralSettingModule {
    val module = module {
        single<GeneralSettingCache> {
            GeneralSettingCache()
        }

        single {
            GeneralSettingListener(
                generalSettingCache = get(),
                transactionManager = get(),
                generalSettingRepository = get(),
                generalSettingEventBus = get()
            )
        }
        single { GeneralSettingEventBus() }

    }
}