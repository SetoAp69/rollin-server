package com.rollinup.server.di

import com.rollinup.server.mapper.AttendanceMapper
import com.rollinup.server.mapper.AuthMapper
import com.rollinup.server.mapper.GeneralSettingMapper
import com.rollinup.server.mapper.PermitMapper
import com.rollinup.server.mapper.UserMapper
import org.koin.dsl.module

object MapperModule {
    val module = module {

        single { UserMapper() }

        single { AuthMapper() }

        single{ PermitMapper() }

        single { AttendanceMapper() }

        single { GeneralSettingMapper() }
    }
}