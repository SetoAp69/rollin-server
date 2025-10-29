package com.rollinup.server.cache.generalsetting

import com.rollinup.server.datasource.database.repository.generalsetting.GeneralSettingRepository
import com.rollinup.server.util.Config
import com.rollinup.server.util.manager.TransactionManager
import com.rollinup.server.util.notFoundException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.postgresql.PGConnection
import java.sql.DriverManager

class GeneralSettingListener(
    private val generalSettingCache: GeneralSettingCache,
    private val transactionManager: TransactionManager,
    private val generalSettingRepository: GeneralSettingRepository,
    private val generalSettingEventBus: GeneralSettingEventBus,
) {
    val config = Config.getDbConfig()

    fun startListening() = CoroutineScope(Dispatchers.IO).launch {
        val conn = DriverManager.getConnection(
            config.url,
            config.username,
            config.password
        )

        conn.autoCommit = true
        val pgConn = conn.unwrap(PGConnection::class.java)

        val statement = conn.createStatement()
        statement.execute("LISTEN general_setting_update;")

        initSettings()

        while (isActive) {
            pgConn.notifications?.let { notifications ->
                for (notif in notifications) {
                    if (notif == null) return@let
                    println(" Received notification: ${notif.name} - payload: ${notif.parameter}")

                }

                val newSetting = transactionManager.suspendTransaction {
                    generalSettingRepository.getGeneralSetting()
                        ?: throw "general setting".notFoundException()
                }
                generalSettingCache.update(newSetting)
                generalSettingEventBus.emit(newSetting)

            }
            delay(1000)

        }
    }

    suspend fun initSettings() {
        transactionManager.suspendTransaction {
            val newSetting =
                generalSettingRepository.getGeneralSetting()
                    ?: throw "general setting".notFoundException()

            generalSettingCache.update(newSetting)
        }
    }
}