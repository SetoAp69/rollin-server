package com.rollinup.server.datasource.apiservice.repository

import com.rollinup.server.Constant
import com.rollinup.server.datasource.apiservice.mapper.RegistrationMapper
import com.rollinup.server.datasource.apiservice.model.request.registration.GetAdminAccessTokenBody
import com.rollinup.server.model.Result
import com.rollinup.server.model.register.AdminAuth
import com.rollinup.server.model.register.RegistrationAccess
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class RegistrationRepositoryImpl(
    private val registrationApiDataSource: com.rollinup.server.datasource.apiservice.datasource.RegistrationApiDataSource,
    private val registrationMapper: RegistrationMapper,
    private val ioDispatcher: CoroutineDispatcher
) : RegistrationRepository {

    override fun getAccessToken(body: GetAdminAccessTokenBody): Flow<Result<AdminAuth>> =
        flow {
            try {
                val result = registrationApiDataSource.getAccessToken(body)
                emit(Result.Success(registrationMapper.mapAdminAuth(result)))
            } catch (e: Exception) {
                emit(Result.Error(Constant.RESPONSE_ERROR))
            }
        }.catch {
            emit(Result.Error(Constant.RESPONSE_ERROR))
        }.flowOn(ioDispatcher)

    override fun getRegistrationToken(token: String): Flow<Result<RegistrationAccess>> =
        flow {
            try {
                val result = registrationApiDataSource.getRegistrationToken(token)
                emit(Result.Success(registrationMapper.mapRegistrationAccess(result)))
            } catch (e: Exception) {
                emit(Result.Error(Constant.RESPONSE_ERROR))
            }
        }.catch {
            emit(Result.Error(Constant.RESPONSE_ERROR))
        }.flowOn(ioDispatcher)

}