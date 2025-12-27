package com.rollinup.server.service.dashboard

import com.rollinup.server.model.Role
import com.rollinup.server.model.response.Response
import com.rollinup.server.model.response.dashboard.GetDashboardResponse
import java.time.LocalDate

interface DashboardService{
    suspend fun getDashboardData(id:String, role: Role, date: LocalDate): Response<GetDashboardResponse>
}