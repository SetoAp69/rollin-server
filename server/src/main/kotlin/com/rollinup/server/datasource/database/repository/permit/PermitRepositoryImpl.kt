package com.rollinup.server.datasource.database.repository.permit

import com.rollinup.server.datasource.database.model.permit.PermitByIdEntity
import com.rollinup.server.datasource.database.model.permit.PermitListEntity
import com.rollinup.server.datasource.database.table.ClassTable
import com.rollinup.server.datasource.database.table.PermitTable
import com.rollinup.server.datasource.database.table.UserTable
import com.rollinup.server.datasource.database.model.ApprovalStatus
import com.rollinup.server.model.request.permit.CreatePermitBody
import com.rollinup.server.model.request.permit.EditPermitBody
import com.rollinup.server.model.request.permit.GetPermitQueryParams
import com.rollinup.server.util.Utils
import com.rollinup.server.util.addFilter
import com.rollinup.server.util.addOffset
import com.rollinup.server.util.likePattern
import org.jetbrains.exposed.v1.core.JoinType
import org.jetbrains.exposed.v1.core.SqlExpressionBuilder.inList
import org.jetbrains.exposed.v1.core.alias
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.compoundOr
import org.jetbrains.exposed.v1.jdbc.andWhere
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.update
import java.time.Instant
import java.time.OffsetDateTime
import java.util.UUID

class PermitRepositoryImpl() : PermitRepository {
    override fun getPermitList(
        queryParams: GetPermitQueryParams,
        studentId: String?,
        classKey: Int?,
    ): List<PermitListEntity> {
        val query = PermitTable
            .join(
                otherTable = UserTable,
                joinType = JoinType.INNER,
                onColumn = PermitTable.user_id,
                otherColumn = UserTable.user_id
            )
            .join(
                otherTable = ClassTable,
                joinType = JoinType.INNER,
                onColumn = ClassTable._id,
                otherColumn = UserTable.classX
            )
            .selectAll()

        query.addFilter(studentId) {
            if (it.isNotBlank()) {
                andWhere {
                    UserTable.user_id eq UUID.fromString(it)
                }
            }
        }

        query.addFilter(classKey) {
            andWhere {
                ClassTable.key eq it
            }
        }

        with(queryParams) {
            query.addFilter(listId) { listId ->
                andWhere {
                    PermitTable._id inList listId.map { UUID.fromString(it) }
                }
            }

            query.addFilter(isActive) {
                andWhere {
                    if (it)
                        PermitTable.approvalStatus eq ApprovalStatus.APPROVAL_PENDING
                    else
                        PermitTable.approvalStatus neq ApprovalStatus.APPROVAL_PENDING
                }
            }

            query.addFilter(dateRange) { dRange ->
                andWhere {
                    val dates = dRange.map { Utils.getOffsetDateTime(it) }

                    (PermitTable.startTime.greaterEq(dates.first())) and (PermitTable.endTime.lessEq(
                        dates.last()
                    ))
                }
            }


            query.addFilter(date) {
                andWhere {
                    val d = Utils.getOffsetDateTime(it)
                    (PermitTable.startTime.greaterEq(d)) and (PermitTable.endTime.lessEq(d))
                }
            }

            query.addFilter(search) { s ->
                if (s.isNotBlank()) {
                    andWhere {
                        listOf(
                            UserTable.firstName like s.likePattern(),
                            UserTable.lastName like s.likePattern(),
                            ClassTable.name like s.likePattern(),
                            PermitTable.reason like s.likePattern(),
                            PermitTable.approvalStatus inList ApprovalStatus.like(s)
                        ).compoundOr()
                    }
                }
            }

            query.addFilter(status) { status ->
                andWhere {
                    PermitTable.approvalStatus inList status.map { ApprovalStatus.fromValue(it) }
                }
            }

            query.addOffset(limit, page)
        }

        return query
            .sortedByDescending {
                PermitTable.createdAt
            }
            .map { row ->
                PermitListEntity.fromResultRow(row)
            }

    }

    override fun getPermitById(id: String): PermitByIdEntity? {
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
                joinType = JoinType.INNER,
                onColumn = ClassTable._id,
                otherColumn = student[UserTable.classX]
            )
            .join(
                otherTable = approver,
                joinType = JoinType.LEFT,
                onColumn = approver[UserTable.user_id],
                otherColumn = PermitTable.approvedBy
            )
            .selectAll()

        val result = query.firstOrNull()?.let { row ->
            PermitByIdEntity.fromResultRow(row = row, student = student, approver = approver)
        }

        return result
    }

    override fun createPermit(body: CreatePermitBody): String {
        val from = OffsetDateTime.ofInstant(
            Instant.ofEpochMilli(body.duration.first()),
            Utils.getOffset()
        )

        val to = OffsetDateTime.ofInstant(
            Instant.ofEpochMilli(body.duration.last()),
            Utils.getOffset()
        )

        val approvedAt = body.approvedAt?.let {
            OffsetDateTime.ofInstant(Instant.ofEpochMilli(it), Utils.getOffset())
        }

        val newPermitId = PermitTable.insert { statement ->
            statement[user_id] = UUID.fromString(body.studentId)
            statement[startTime] = from
            statement[endTime] = to
            statement[reason] = body.reason
            statement[type] = body.type
            statement[note] = body.note
            statement[attachment] = body.attachment
            statement[approvedBy] = body.approvedBy?.let { UUID.fromString(it) }
            statement[approvalStatus] = body.approvalStatus ?: ApprovalStatus.APPROVAL_PENDING
            statement[this.approvedAt] = approvedAt
            statement[approvalNote] = body.approvalNote
        }[PermitTable._id]

        return newPermitId.toString()

    }

    override fun editPermit(
        listId: List<String>,
        body: EditPermitBody,
    ) {
        PermitTable.update(
            where = {
                PermitTable._id inList listId.map { UUID.fromString(it) }
            }
        ) { statement ->
            with(body) {
                duration?.let {
                    val from = OffsetDateTime.ofInstant(
                        Instant.ofEpochMilli(it.first()),
                        Utils.getOffset()
                    )
                    val to = OffsetDateTime.ofInstant(
                        Instant.ofEpochMilli(it.first()),
                        Utils.getOffset()
                    )
                    statement[PermitTable.startTime] = from
                    statement[PermitTable.endTime] = to
                }
                reason?.let { statement[PermitTable.reason] = it }
                type?.let { statement[PermitTable.type] = it }
                attachment?.let { statement[PermitTable.attachment] = it }
                note?.let { statement[PermitTable.note] = it }
                approvedBy?.let { statement[PermitTable.approvedBy] = UUID.fromString(it) }
                approvalNote?.let { statement[PermitTable.approvalNote] = it }
                approvalStatus?.let { statement[PermitTable.approvalStatus] = it }
                approvedAt?.let {
                    val approvedAt = OffsetDateTime.ofInstant(
                        Instant.ofEpochMilli(it),
                        Utils.getOffset()
                    )
                    statement[PermitTable.approvedAt] = approvedAt
                }

            }
        }
    }

    override fun deletePermit(listId: List<String>) {
        val uuidList = listId.map { UUID.fromString(it) }

        PermitTable.deleteWhere {
            PermitTable._id inList uuidList
        }
    }


}