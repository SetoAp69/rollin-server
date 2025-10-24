package com.rollinup.server.datasource.database.repository.attendance

import com.rollinup.server.datasource.database.model.AttendanceStatus
import com.rollinup.server.datasource.database.model.attendance.AttendanceByClassEntity
import com.rollinup.server.datasource.database.model.attendance.AttendanceByStudentEntity
import com.rollinup.server.datasource.database.model.attendance.AttendanceEntity
import com.rollinup.server.datasource.database.model.attendance.AttendanceSummaryEntity
import com.rollinup.server.datasource.database.table.AttendanceTable
import com.rollinup.server.datasource.database.table.ClassTable
import com.rollinup.server.datasource.database.table.PermitTable
import com.rollinup.server.datasource.database.table.UserTable
import com.rollinup.server.model.ApprovalStatus
import com.rollinup.server.model.request.attendance.AttendanceSummaryQueryParams
import com.rollinup.server.model.request.attendance.CreateAttendanceBody
import com.rollinup.server.model.request.attendance.EditAttendanceBody
import com.rollinup.server.model.request.attendance.GetAttendanceByClassQueryParams
import com.rollinup.server.model.request.attendance.GetAttendanceByStudentQueryParams
import com.rollinup.server.util.Utils
import com.rollinup.server.util.Utils.getOffset
import com.rollinup.server.util.addFilter
import com.rollinup.server.util.addOffset
import com.rollinup.server.util.likePattern
import org.jetbrains.exposed.v1.core.Case
import org.jetbrains.exposed.v1.core.JoinType
import org.jetbrains.exposed.v1.core.Op
import org.jetbrains.exposed.v1.core.SqlExpressionBuilder.eq
import org.jetbrains.exposed.v1.core.SqlExpressionBuilder.inList
import org.jetbrains.exposed.v1.core.SqlExpressionBuilder.neq
import org.jetbrains.exposed.v1.core.alias
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.compoundOr
import org.jetbrains.exposed.v1.core.intLiteral
import org.jetbrains.exposed.v1.core.or
import org.jetbrains.exposed.v1.core.sum
import org.jetbrains.exposed.v1.jdbc.andWhere
import org.jetbrains.exposed.v1.jdbc.batchUpsert
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.update
import org.jetbrains.exposed.v1.jdbc.upsert
import java.time.Instant
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.UUID

class AttendanceRepositoryImpl() : AttendanceRepository {
//    override fun getAttendanceList(queryParams: AttendanceQueryParams): List<AttendanceEntity> {
//        val student = UserTable.alias("student")
//        val query = AttendanceTable
//            .join(
//                joinType = JoinType.INNER,
//                otherTable = student,
//                onColumn = AttendanceTable.userId,
//                otherColumn = student[UserTable.user_id],
//            )
//            .join(
//                joinType = JoinType.LEFT,
//                otherTable = ClassTable,
//                onColumn = student[UserTable.classX],
//                otherColumn = ClassTable._id,
//            )
//            .join(
//                joinType = JoinType.LEFT,
//                otherTable = PermitTable,
//                onColumn = AttendanceTable.permit,
//                otherColumn = PermitTable._id,
//            )
//            .select(
//                AttendanceTable.columns
//                        + student[UserTable.user_id]
//                        + student[UserTable.username]
//                        + student[UserTable.firstName]
//                        + student[UserTable.lastName]
//                        + ClassTable.name
//                        + PermitTable.startTime
//                        + PermitTable.endTime
//                        + PermitTable.reason
//                        + PermitTable.type
//            )
//
//        val sortField = AttendanceTable.sortField + PermitTable.sortField + ClassTable.sortField
//
//        with(queryParams) {
//            query.addFilter(studentId) {
//                if (it.isNotBlank()) {
//                    andWhere {
//                        student[UserTable.user_id] eq UUID.fromString(it)
//                    }
//                }
//            }
//            query.addFilter(status) { status ->
//                andWhere {
//                    AttendanceTable.status inList status.map { AttendanceStatus.fromValue(it) }
//                }
//            }
////            query.addFilter(xClass) { classList ->
////                andWhere {
////                    ClassTable.key inList classList
////                }
////            }
//            query.addFilter(dateRange) { range ->
//                andWhere {
//                    val from = LocalDate.ofInstant(
//                        Instant.ofEpochMilli(range.first()),
//                        Utils.getOffset()
//                    )
//
//
//                    val to = LocalDate.ofInstant(
//                        Instant.ofEpochMilli(range.last()),
//                        Utils.getOffset()
//                    )
//
//                    AttendanceTable.date.between(from, to)
//                }
//            }
//            query.addFilter(search) { searchQuery ->
//                if (searchQuery.isNotBlank()) {
//                    andWhere {
//                        listOf(
//                            student[UserTable.firstName] like searchQuery.likePattern(),
//                            student[UserTable.lastName] like searchQuery.likePattern(),
//                            ClassTable.name like searchQuery.likePattern(),
//                            PermitTable.reason like searchQuery.likePattern(),
//                            AttendanceTable.status inList AttendanceStatus.like(searchQuery)
//                        ).compoundOr()
//                    }
//                }
//            }
//            query.addFilter(sortBy) { sortBy ->
//                if (sortBy.isNotBlank() && order?.isNotBlank() ?: false) {
//                    sortField[sortBy]?.let {
//                        orderBy(it to SortOrder.valueOf(order))
//                    }
//                }
//            }
//        }
//
//        return query.map { row ->
//            AttendanceEntity.fromResultRow(
//                row = row,
//                student = student
//            )
//        }
//    }

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

    override fun createAttendanceData(body: CreateAttendanceBody): String {
        return AttendanceTable.insert { statement ->
            statement[userId] = UUID.fromString(body.studentUserId)
            statement[latitude] = body.latitude
            statement[longitude] = body.longitude
            statement[attachment] = body.attachment
            statement[checkedInAt] =
                OffsetDateTime.ofInstant(Instant.ofEpochMilli(body.checkedInAt), getOffset())
        }[AttendanceTable._id].toString()
    }


    override fun getSummary(queryParams: AttendanceSummaryQueryParams): AttendanceSummaryEntity {
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


        byStatusQuery.addFilter(queryParams.classX) {
            andWhere {
                ClassTable.key eq it
            }
        }

        byStatusQuery.addFilter(queryParams.studentUserId) {
            andWhere {
                AttendanceTable.userId eq UUID.fromString(it)
            }
        }

        byStatusQuery.addFilter(queryParams.dateRange) { range ->
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


        byReasonQuery.addFilter(queryParams.classX) {
            andWhere {
                ClassTable.key eq it
            }
        }

        byReasonQuery.addFilter(queryParams.studentUserId) {
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


        val sickCount = (byReasonQuery.single().getOrNull(sickExpression) ?: 0).toLong()
        val otherCount = (byReasonQuery.single().getOrNull(otherExpression) ?: 0).toLong()

        return AttendanceSummaryEntity.fromResultRow(
            statusCount = statusCount,
            sickCount = sickCount,
            otherCount = otherCount
        )
    }

    override fun isCheckedIn(userId: String): Boolean {
        return true
    }

    override fun getAttendanceListByClass(
        queryParams: GetAttendanceByClassQueryParams,
        classKey: Int,
    ): List<AttendanceByClassEntity> {

        val date = queryParams.date?.let {
            LocalDate.ofInstant(Instant.ofEpochMilli(it), getOffset())
        }

        val query = UserTable
            .join(
                otherTable = ClassTable,
                joinType = JoinType.INNER,
                onColumn = UserTable.classX,
                otherColumn = ClassTable._id,
            )
            .join(
                otherTable = AttendanceTable,
                joinType = JoinType.LEFT,
                additionalConstraint = {
                    (UserTable.user_id eq AttendanceTable.userId) and
                            (if (date != null) AttendanceTable.date eq date else Op.TRUE)
                }
            )
            .join(
                otherTable = PermitTable,
                joinType = JoinType.LEFT,
                onColumn = AttendanceTable.permit,
                otherColumn = PermitTable._id
            )
            .selectAll()

        with(queryParams) {
            query.addFilter(status) { status ->
                andWhere {
                    val attendanceStatus = status.map { AttendanceStatus.fromValue(it) }
                    buildList {
                        add(AttendanceTable.status inList attendanceStatus)
                        if (attendanceStatus.contains(AttendanceStatus.ALPHA)) add(
                            AttendanceTable.userId.isNull()
                        )
                    }.compoundOr()
                }
            }

            query.addFilter(queryParams.date) {
                val date = LocalDate
                    .ofInstant(
                        Instant.ofEpochMilli(it),
                        getOffset()
                    )

                andWhere {
                    (AttendanceTable.date eq date) or (AttendanceTable._id.isNull())
                }
            }

            query.addFilter(search) { searchQuery ->
                if (searchQuery.isNotBlank()) {
                    andWhere {
                        listOf(
                            UserTable.firstName like searchQuery.likePattern(),
                            UserTable.lastName like searchQuery.likePattern(),
                            ClassTable.name like searchQuery.likePattern(),
                            PermitTable.reason like searchQuery.likePattern(),
                            AttendanceTable.status inList AttendanceStatus.like(searchQuery)
                        ).compoundOr()
                    }
                }
            }

            query
                .andWhere {
                    ClassTable.key eq classKey
                }

            query
                .addOffset(limit, page)

            query
                .orderBy(AttendanceTable.date)

        }

        return query.map { row ->
            AttendanceByClassEntity.fromResultRow(row)
        }


    }

    override fun getAttendanceListByStudent(
        queryParams: GetAttendanceByStudentQueryParams,
        studentId: String,
    ): List<AttendanceByStudentEntity> {

        val query = AttendanceTable
            .join(
                otherTable = PermitTable,
                joinType = JoinType.LEFT,
                onColumn = AttendanceTable.permit,
                otherColumn = PermitTable._id
            )
            .selectAll()



        with(queryParams) {
            query.addFilter(dateRange) {
                val from = LocalDate
                    .ofInstant(
                        Instant.ofEpochMilli(it.first()),
                        getOffset()
                    )
                val to = LocalDate
                    .ofInstant(
                        Instant.ofEpochMilli(it.last()),
                        getOffset()
                    )

                andWhere {
                    AttendanceTable.date.between(from, to)
                }
            }
            query.addFilter(search) {
                if (it.isNotBlank()) {
                    andWhere {
                        listOf(
                            PermitTable.approvalStatus inList ApprovalStatus.like(it),
                            PermitTable.reason like it.likePattern()
                        ).compoundOr()
                    }
                }
            }
            query.andWhere {
                AttendanceTable.userId eq UUID.fromString(studentId)
            }

            query.addOffset(limit, page)
        }

        return query.map { row ->
            AttendanceByStudentEntity.fromResultRow(row)
        }

    }

    override fun updateAttendanceData(
        listId: List<String>,
        body: EditAttendanceBody,
    ) {

        AttendanceTable
            .update(
                where = {
                    AttendanceTable._id inList listId.map { UUID.fromString(it) }
                }
            ) { statement ->
                body.status?.let {
                    statement[status] = it
                }
                body.checkedInAt?.let {
                    val offsetCheckInTime =
                        OffsetDateTime.ofInstant(Instant.ofEpochMilli(it), getOffset())
                    statement[checkedInAt] = offsetCheckInTime
                }
                if (listOf(body.location.longitude, body.location.latitude).all { it != null }) {
                    statement[latitude] = body.location.latitude
                    statement[longitude] = body.location.longitude
                }
            }

    }

    override fun updatePermit(id: String, permitId: String?) {
        AttendanceTable
            .update(
                where = {
                    AttendanceTable._id eq UUID.fromString(id)
                }
            ) { statement ->
                statement[permit] = permitId?.let { UUID.fromString(it) }
            }

    }

    override fun createAttendanceFromPermit(
        permitId: String,
        studentId: String,
        duration: List<Long>,
        status: AttendanceStatus,
    ) {
        val from = Utils.getOffsetDateTime(duration.first())
        val to = Utils.getOffsetDateTime(duration.last())

        val dates = Utils.generateDateRange(from, to)

        AttendanceTable.batchUpsert(
            data = dates,
            keys = arrayOf(AttendanceTable.userId, AttendanceTable.date)
        ) { date ->
            this[AttendanceTable.userId] = UUID.fromString(studentId)
            this[AttendanceTable.date] = date
            this[AttendanceTable.status] = status
            this[AttendanceTable.permit] = UUID.fromString(permitId)
        }

    }


    override fun deleteAttendanceData(listId: List<String>) {
        val uuidList = listId.map { UUID.fromString(it) }

        AttendanceTable.deleteWhere {
            AttendanceTable._id inList uuidList
        }
    }

    override fun getAttendanceListByPermit(listId: List<String>): List<AttendanceEntity> {
        val permitUUID = listId.map { UUID.fromString(it) }

        return AttendanceTable
            .select(AttendanceTable._id)
            .where { AttendanceTable.permit inList permitUUID }
            .map { row ->
                AttendanceEntity.fromResultRow(row)
            }
    }

}