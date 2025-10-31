package com.rollinup.server.datasource.database.repository.generalsetting

import com.rollinup.server.datasource.database.model.generalsetting.GeneralSetting
import com.rollinup.server.datasource.database.table.GeneralSettingTable
import com.rollinup.server.datasource.database.table.UserTable
import com.rollinup.server.model.request.generalsetting.EditGeneralSettingBody
import com.rollinup.server.util.Utils
import org.jetbrains.exposed.v1.core.JoinType
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.update
import java.util.UUID

class GeneralSettingRepositoryImpl : GeneralSettingRepository {
    override fun getGeneralSetting(): GeneralSetting? {
        val result = GeneralSettingTable
            .join(
                otherTable = UserTable,
                onColumn = GeneralSettingTable.modifiedBy,
                otherColumn = UserTable.user_id,
                joinType = JoinType.INNER,
            )
            .selectAll()
            .firstOrNull()
            ?.let {
                GeneralSetting.fromRow(it)
            }
        return result
    }

    override fun updateGeneralSetting(body: EditGeneralSettingBody, editBy: String) {
        GeneralSettingTable.update { statement ->
            statement[modifiedBy] = UUID.fromString(editBy)
            body.semesterStart?.let {
                statement[semesterStart] = Utils.getOffsetDateTime(
                    epochMilli = it,
                    offset = Utils.getOffset()
                )
            }

            body.semesterEnd?.let {
                statement[semesterEnd] = Utils.getOffsetDateTime(
                    epochMilli = it,
                    offset = Utils.getOffset()
                )
            }

            body.schoolPeriodStart?.let {
                statement[schoolPeriodStart] = Utils.getLocalTime(it)
            }

            body.schoolPeriodEnd?.let {
                statement[schoolPeriodEnd] = Utils.getLocalTime(it)
            }
            body.checkInPeriodStart?.let {
                statement[checkInPeriodStart] = Utils.getLocalTime(it)
            }
            body.checkInPeriodEnd?.let {
                statement[checkInPeriodEnd] = Utils.getLocalTime(it)
            }
            body.latitude?.let {
                statement[latitude] = it
            }
            body.longitude?.let {
                statement[longitude] = it
            }
            body.radius?.let {
                statement[geofenceRadius] = it
            }
        }

    }
}