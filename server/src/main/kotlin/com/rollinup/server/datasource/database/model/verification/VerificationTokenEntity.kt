package com.rollinup.server.datasource.database.model.verification

import com.rollinup.server.datasource.database.table.VerificationTokenTable
import org.jetbrains.exposed.v1.core.ResultRow
import java.time.Instant

data class VerificationTokenEntity(
    val id: String = "",
    val token: String = "",
    val salt: String = "",
    val expiredAt: Instant = Instant.now(),
) {
    companion object {
        fun fromRow(resultRow: ResultRow) = VerificationTokenEntity(
            expiredAt = resultRow[VerificationTokenTable.expiredAt],
            id = resultRow[VerificationTokenTable.user_id].toString(),
            token = resultRow[VerificationTokenTable.token],
            salt = resultRow[VerificationTokenTable.salt],
        )
    }
}
