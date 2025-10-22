package com.rollinup.server.datasource.database.repository.attendance

import com.rollinup.server.datasource.database.model.AttendanceStatus
import com.rollinup.server.datasource.database.model.attendance.AttendanceEntity
import com.rollinup.server.datasource.database.model.attendance.AttendanceSummaryEntity
import com.rollinup.server.datasource.database.table.AttendanceTable
import com.rollinup.server.datasource.database.table.ClassTable
import com.rollinup.server.datasource.database.table.PermitTable
import com.rollinup.server.datasource.database.table.UserTable
import com.rollinup.server.model.request.attendance.AttendanceQueryParams
import com.rollinup.server.model.request.attendance.CreateAttendanceBody
import com.rollinup.server.util.Utils
import com.rollinup.server.util.Utils.getOffset
import com.rollinup.server.util.addFilter
import com.rollinup.server.util.likePattern
import org.jetbrains.exposed.v1.core.Case
import org.jetbrains.exposed.v1.core.JoinType
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.SqlExpressionBuilder.eq
import org.jetbrains.exposed.v1.core.SqlExpressionBuilder.neq
import org.jetbrains.exposed.v1.core.alias
import org.jetbrains.exposed.v1.core.compoundOr
import org.jetbrains.exposed.v1.core.count
import org.jetbrains.exposed.v1.core.intLiteral
import org.jetbrains.exposed.v1.core.sum
import org.jetbrains.exposed.v1.jdbc.andWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.selectAll
import java.time.Instant
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.UUID

class AttendanceRepositoryImpl() : AttendanceRepository {
    override fun getAttendanceList(queryParams: AttendanceQueryParams): List<AttendanceEntity> {
        val student = UserTable.alias("student")
        val query = AttendanceTable
            .join(
                joinType = JoinType.INNER,
                otherTable = student,
                onColumn = AttendanceTable.userId,
                otherColumn = student[UserTable.user_id],
            )
            .join(
                joinType = JoinType.LEFT,
                otherTable = ClassTable,
                onColumn = student[UserTable.classX],
                otherColumn = ClassTable._id,
            )
            .join(
                joinType = JoinType.LEFT,
                otherTable = PermitTable,
                onColumn = AttendanceTable.permit,
                otherColumn = PermitTable._id,
            )
            .select(
                AttendanceTable.columns
                        + student[UserTable.user_id]
                        + student[UserTable.username]
                        + student[UserTable.firstName]
                        + student[UserTable.lastName]
                        + ClassTable.name
                        + PermitTable.startTime
                        + PermitTable.endTime
                        + PermitTable.reason
                        + PermitTable.type
            )

        val sortField = AttendanceTable.sortField + PermitTable.sortField + ClassTable.sortField

        with(queryParams) {
            query.addFilter(studentId) {
                if (it.isNotBlank()) {
                    andWhere {
                        student[UserTable.user_id] eq UUID.fromString(it)
                    }
                }
            }
            query.addFilter(status) { status ->
                andWhere {
                    AttendanceTable.status inList status.map { AttendanceStatus.fromValue(it) }
                }
            }
            query.addFilter(xClass) { classList ->
                andWhere {
                    ClassTable.key inList classList
                }
            }
            query.addFilter(dateRange) { range ->
                andWhere {
                    val from = LocalDate.ofInstant(
                        Instant.ofEpochMilli(range.first()),
                        Utils.getOffset()
                    )


                    val to = LocalDate.ofInstant(
                        Instant.ofEpochMilli(range.last()),
                        Utils.getOffset()
                    )

                    AttendanceTable.date.between(from, to)
                }
            }
            query.addFilter(search) { searchQuery ->
                if (searchQuery.isNotBlank()) {
                    andWhere {
                        listOf(
                            student[UserTable.firstName] like searchQuery.likePattern(),
                            student[UserTable.lastName] like searchQuery.likePattern(),
                            ClassTable.name like searchQuery.likePattern(),
                            PermitTable.reason like searchQuery.likePattern(),
                            AttendanceTable.status inList AttendanceStatus.like(searchQuery)
                        ).compoundOr()
                    }
                }
            }
            query.addFilter(sortBy) { sortBy ->
                if (sortBy.isNotBlank() && order?.isNotBlank() ?: false) {
                    sortField[sortBy]?.let {
                        orderBy(it to SortOrder.valueOf(order))
                    }
                }
            }
        }

        return query.map { row ->
            AttendanceEntity.fromResultRow(
                row = row,
                student = student
            )
        }
    }

    override fun getAttendanceById(id: String): AttendanceEntity? {
        val student = UserTable.alias("student")
        val approver = UserTable.alias("approver")
        val query = AttendanceTable
            .join(
                otherTable = student,
                joinType = JoinType.INNER,
                onColumn = AttendanceTable.userId,
            )
            .join(
                otherTable = ClassTable,
                joinType = JoinType.INNER,
                onColumn = student[UserTable.classX]
            )
            .join(
                otherTable = PermitTable,
                joinType = JoinType.LEFT,
                onColumn = AttendanceTable.permit,
            )
            .join(
                otherTable = approver,
                joinType = JoinType.LEFT,
                onColumn = PermitTable.approvedBy,
            )
            .selectAll()
            .where { AttendanceTable.userId eq UUID.fromString(id) }
            .firstOrNull()

        return query?.let {
            AttendanceEntity.fromResultRowById(
                row = it,
                student = student,
                approver = approver
            )
        }
    }

    override fun createAttendanceData(body: CreateAttendanceBody) {
        AttendanceTable.insert { statement ->
            statement[userId] = UUID.fromString(body.studentUserId)
            statement[latitude] = body.location.latitude
            statement[longitude] = body.location.longitude
            statement[checkedInAt] = OffsetDateTime.ofInstant(Instant.now(), getOffset())
        }
    }

    override fun getSummary(queryParams: AttendanceQueryParams): AttendanceSummaryEntity {
        val sickExpression = Case()
            .When(PermitTable.reason eq "sick", intLiteral(1))
            .Else(intLiteral(0))
            .sum()
            .alias("sick_count")

        val otherExpression = Case()
            .When(PermitTable.reason neq "sick", intLiteral(1))
            .Else(intLiteral(0))
            .sum()
            .alias("other_count")

        val byStatusQuery = AttendanceTable
            .join(
                otherTable = UserTable,
                joinType = JoinType.INNER,
                additionalConstraint = { AttendanceTable.userId eq UserTable.user_id }
            )
            .join(
                otherTable = ClassTable,
                joinType = JoinType.INNER,
                additionalConstraint = { UserTable.classX eq ClassTable._id }
            )
            .join(
                otherTable = PermitTable,
                joinType = JoinType.LEFT,
                additionalConstraint = { AttendanceTable.permit eq PermitTable._id }
            )
            .select(
                AttendanceTable.status
            )


        byStatusQuery.addFilter(queryParams.xClass) {
            andWhere {
                ClassTable.key inList it
            }
        }

        byStatusQuery.addFilter(queryParams.studentId) {
            andWhere {
                AttendanceTable.userId eq UUID.fromString(it)
            }
        }

        byStatusQuery.addFilter(queryParams.dateRange) { range ->
            andWhere {
                val from = LocalDate.ofInstant(
                    Instant.ofEpochMilli(range.first()),
                    Utils.getOffset()
                )

                val to = LocalDate.ofInstant(
                    Instant.ofEpochMilli(range.last()),
                    Utils.getOffset()
                )

                AttendanceTable.date.between(from, to)
            }
        }

        val byReasonQuery = AttendanceTable
            .join(
                UserTable,
                JoinType.INNER,
                additionalConstraint = { AttendanceTable.userId eq UserTable.user_id })
            .join(
                ClassTable,
                JoinType.INNER,
                additionalConstraint = { UserTable.classX eq ClassTable._id })
            .join(
                PermitTable,
                JoinType.LEFT,
                additionalConstraint = { AttendanceTable.permit eq PermitTable._id })
            .select(sickExpression, otherExpression)


        byReasonQuery.addFilter(queryParams.xClass) {
            andWhere {
                ClassTable.key inList it
            }
        }

        byReasonQuery.addFilter(queryParams.studentId) {
            andWhere {
                AttendanceTable.userId eq UUID.fromString(it)
            }
        }

        byReasonQuery.addFilter(queryParams.dateRange) { range ->
            andWhere {
                val from = LocalDate.ofInstant(
                    Instant.ofEpochMilli(range.first()),
                    getOffset()
                )

                val to = LocalDate.ofInstant(
                    Instant.ofEpochMilli(range.last()),
                    getOffset()
                )

                AttendanceTable.date.between(from, to)
            }
        }


        val statusCount = byStatusQuery
            .groupBy { it[AttendanceTable.status] }
            .mapValues { (_, rows) -> rows.size.toLong() }

//
//        val sickCount = (byReasonQuery.single().getOrNull(sickExpression) ?: 0).toLong()
//        val otherCount = (byReasonQuery.single().getOrNull(otherExpression) ?: 0).toLong()

        return AttendanceSummaryEntity.fromResultRow(
            statusCount = statusCount,
            sickCount = 0L,
            otherCount = 0L
        )
    }
}