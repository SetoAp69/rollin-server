package com.rollinup.server.di

import com.rollinup.server.datasource.database.repository.generalsetting.GeneralSettingRepository
import com.rollinup.server.datasource.database.repository.attendance.AttendanceRepository
import com.rollinup.server.datasource.database.repository.attendance.AttendanceRepositoryImpl
import com.rollinup.server.datasource.database.repository.generalsetting.GeneralSettingRepositoryImpl
import com.rollinup.server.datasource.database.repository.holiday.HolidayRepository
import com.rollinup.server.datasource.database.repository.holiday.HolidayRepositoryImpl
import com.rollinup.server.datasource.database.repository.permit.PermitRepository
import com.rollinup.server.datasource.database.repository.permit.PermitRepositoryImpl
import com.rollinup.server.datasource.database.repository.refreshtoken.RefreshTokenRepository
import com.rollinup.server.datasource.database.repository.refreshtoken.RefreshTokenRepositoryImpl
import com.rollinup.server.datasource.database.repository.resetpassword.ResetPasswordRepository
import com.rollinup.server.datasource.database.repository.resetpassword.ResetPasswordRepositoryImpl
import com.rollinup.server.datasource.database.repository.user.UserRepository
import com.rollinup.server.datasource.database.repository.user.UserRepositoryImpl
import org.koin.dsl.module

object RepositoryModule {
    val module = module {

        single<ResetPasswordRepository> {
            ResetPasswordRepositoryImpl()
        }

        single<UserRepository> {
            UserRepositoryImpl()
        }

        single<RefreshTokenRepository> {
            RefreshTokenRepositoryImpl()
        }

        single<AttendanceRepository> {
            AttendanceRepositoryImpl()
        }

        single<PermitRepository> {
            PermitRepositoryImpl()
        }

        single<GeneralSettingRepository> {
            GeneralSettingRepositoryImpl()
        }

        single<HolidayRepository>{
            HolidayRepositoryImpl()
        }
    }
}