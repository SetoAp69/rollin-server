package com.rollinup.server.mapper

import com.rollinup.server.datasource.database.model.generalsetting.GeneralSetting
import com.rollinup.server.model.response.generalsetting.GetGeneralSettingResponse

class GeneralSettingMapper {
    fun mapGetGeneralSettingResponse(data: GeneralSetting) =
        GetGeneralSettingResponse(
            semesterStart = data.semesterStart.toString(),
            semesterEnd = data.semesterEnd.toString(),
            updatedAt = data.updatedAt.toString(),
            schoolPeriodStart = data.schoolPeriodStart.toString(),
            schoolPeriodEnd = data.schoolPeriodEnd.toString(),
            checkInPeriodStart = data.checkInPeriodStart.toString(),
            checkInPeriodEnd = data.checkInPeriodEnd.toString(),
            latitude = data.lat,
            longitude = data.long,
            radius = data.rad,
            modifiedBy = GetGeneralSettingResponse.ModifiedBy(
                id = data.modifiedBy,
                name = data.modifiedByName
            ),
        )
}