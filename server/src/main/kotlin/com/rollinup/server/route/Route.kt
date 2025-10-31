package com.rollinup.server.route

import com.rollinup.server.route.attendance.attendanceRoute
import com.rollinup.server.route.auth.authRoute
import com.rollinup.server.route.file.fileRoute
import com.rollinup.server.route.generalsetting.generalSettingRoute
import com.rollinup.server.route.holiday.holidayRoute
import com.rollinup.server.route.permit.permitRoute
import com.rollinup.server.route.user.userRouteNew

sealed class Route(
    val path: String,
    val route: io.ktor.server.routing.Route.() -> Unit,
) {
    object Auth : Route(
        path = "/auth",
        route = {
            authRoute()
        }
    )

    object User : Route(
        path = "/user",
        route = {
            userRouteNew()
        }
    )

    object Attendance : Route(
        path = "/attendance",
        route = {
            attendanceRoute()
        }
    )

    object Permit : Route(
        path = "/permit",
        route = {
            permitRoute()
        }
    )

    object File : Route(
        path = "/file",
        route = {
            fileRoute()
        }
    )

    object GeneralSetting : Route(
        path = "/general-setting",
        route = {
            generalSettingRoute()
        }
    )

    object Holiday : Route(
        path = "/holiday",
        route = {
            holidayRoute()
        }
    )

}