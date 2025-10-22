package com.rollinup.server.datasource.database.repository.permit

import com.rollinup.server.datasource.database.model.AttendanceStatus
import com.rollinup.server.datasource.database.model.permit.PermitEntity
import com.rollinup.server.datasource.database.table.AttendanceTable
import com.rollinup.server.datasource.database.table.ClassTable
import com.rollinup.server.datasource.database.table.PermitTable
import com.rollinup.server.datasource.database.table.UserTable
import com.rollinup.server.model.ApprovalStatus
import com.rollinup.server.model.PermitType
import com.rollinup.server.model.request.attendance.AttendanceQueryParams
import com.rollinup.server.util.addFilter
import com.rollinup.server.util.likePattern
import org.jetbrains.exposed.v1.core.JoinType
import org.jetbrains.exposed.v1.core.alias
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.compoundOr
import org.jetbrains.exposed.v1.core.statements.UpsertSqlExpressionBuilder.inList
import org.jetbrains.exposed.v1.jdbc.andWhere
import org.jetbrains.exposed.v1.jdbc.select
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.util.UUID

class PermitRepositoryImpl() : PermitRepository {
    override fun getPermitList(queryParams: AttendanceQueryParams): List<PermitEntity> {
        val student = UserTable.alias("student")
        val approver = UserTable.alias("approver")
        val query = PermitTable
            .join(
                otherTable = student,
                joinType = JoinType.INNER,
                onColumn = PermitTable.user_id,
                otherColumn = student[UserTable.user_id]
            )
            .join(
                otherTable = ClassTable,
                joinType = JoinType.LEFT,
                onColumn = ClassTable._id,
                otherColumn = student[UserTable.classX]
            )
            .join(
                otherTable = approver,
                joinType = JoinType.LEFT,
                onColumn = PermitTable.approvedBy,
                otherColumn = approver[UserTable.user_id]
            )
            .join(
                otherTable = AttendanceTable,
                joinType = JoinType.LEFT,
                onColumn = PermitTable.attendanceId,
                otherColumn = AttendanceTable._id
            )
            .select(
                PermitTable.columns
                        + student[UserTable.user_id]
                        + student[UserTable.username]
                        + student[UserTable.firstName]
                        + student[UserTable.lastName]
                        + approver[UserTable.user_id]
                        + approver[UserTable.username]
                        + approver[UserTable.firstName]
                        + approver[UserTable.lastName]
            )


        query.addFilter(queryParams.search) { it ->
            if (it.isNotBlank()) {
                andWhere {
                    listOf(
                        student[UserTable.firstName] like it.likePattern(),
                        approver[UserTable.firstName] like it.likePattern(),
                        approver[UserTable.lastName] like it.likePattern(),
                        ClassTable.name like it.likePattern(),
                        AttendanceTable.status inList AttendanceStatus.like(it),
                        PermitTable.approvalStatus inList ApprovalStatus.like(it),
                        PermitTable.type inList PermitType.like(it)
                    ).compoundOr()
                }
            }
        }
        query.addFilter(queryParams.studentId){
            if(it.isNotBlank()){
                andWhere {
                    PermitTable.user_id eq UUID.fromString(it)
                }
            }
        }
        query.addFilter(queryParams.xClass) {
            andWhere {
                ClassTable.key inList it
            }
        }
        query.addFilter(queryParams.status){status->
            andWhere {
                val statusList = status.map { ApprovalStatus.fromValue(it)  }
                PermitTable.approvalStatus inList statusList
            }
        }
        query.addFilter(queryParams.dateRange){dateRange->
            andWhere {
                val from = OffsetDateTime.ofInstant(
                    Instant.ofEpochMilli(dateRange.first()),
                    ZoneOffset.UTC
                )
                val to = OffsetDateTime.ofInstant(
                    Instant.ofEpochMilli(dateRange.last()),
                    ZoneOffset.UTC
                )
                PermitTable.startTime.greaterEq(from) and PermitTable.endTime.lessEq(to)
            }
        }

        val result = query.map { row->
            PermitEntity.fromResultRow(
                row = row ,
                student = student,
                approver = approver
            )
        }

        return result
    }
}