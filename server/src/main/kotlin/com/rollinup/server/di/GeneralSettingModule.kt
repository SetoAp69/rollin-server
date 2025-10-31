package com.rollinup.server.di

import com.rollinup.server.cache.generalsetting.GeneralSettingCache
import com.rollinup.server.cache.generalsetting.GeneralSettingEventBus
import com.rollinup.server.cache.generalsetting.GeneralSettingListener
import org.koin.dsl.module

object GeneralSettingModule {
    val module = module {
        single<GeneralSettingCache> {
            GeneralSettingCache()
        }

        single<GeneralSettingListener> {
            GeneralSettingListener(
                generalSettingCache = get(),
                transactionManager = get(),
                generalSettingRepository = get(),
                generalSettingEventBus = get()
            )
        }
        single <GeneralSettingEventBus> { GeneralSettingEventBus() }

    }
}