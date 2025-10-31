package com.rollinup.server.cache.holiday

import com.rollinup.server.datasource.database.repository.holiday.HolidayRepository
import com.rollinup.server.util.Config
import com.rollinup.server.util.manager.TransactionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.postgresql.PGConnection
import java.sql.DriverManager

class HolidayListener(
    private val holidayCache: HolidayCache,
    private val transactionManager: TransactionManager,
    private val holidayRepository: HolidayRepository,
    private val holidayEventBus: HolidayEventBus,
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
        statement.execute("LISTEN holiday_update;")

        initCache()

        while (isActive) {
            pgConn.notifications?.let { notifications ->
                for (notif in notifications) {
                    if (notif == null) return@let
                    println(" Received notification: ${notif.name} - payload: ${notif.parameter}")
                }

                val data = transactionManager.suspendTransaction {
                    holidayRepository.getHolidayList()
                }
                holidayCache.update(data)

                holidayEventBus.emit(data)
            }
            delay(1000)
        }
    }

    suspend fun initCache() {
        transactionManager.suspendTransaction {
            val data = holidayRepository.getHolidayList()
            holidayCache.update(data)
        }
    }
}
