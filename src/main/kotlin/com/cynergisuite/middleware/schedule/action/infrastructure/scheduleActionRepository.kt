package com.cynergisuite.middleware.schedule.action.infrastructure


import com.cynergisuite.extensions.getOffsetDateTime
import com.cynergisuite.extensions.getUuid
import com.cynergisuite.extensions.insertReturning
import com.cynergisuite.middleware.audit.Audit
import com.cynergisuite.middleware.audit.action.AuditAction
import com.cynergisuite.middleware.audit.status.infrastructure.AuditStatusRepository
import com.cynergisuite.middleware.employee.infrastructure.EmployeeRepository
import io.micronaut.spring.tx.annotation.Transactional
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.sql.ResultSet
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class scheduleActionRepository @Inject constructor{
   //private val auditStatusRepository: AuditStatusRepository,
   //private val employeeRepository: EmployeeRepository,
   private val jdbc: NamedParameterJdbcTemplate
) {
   private val logger: Logger = LoggerFactory.getLogger(ScheduleActionRepository::class.java)

//   @Transactional
//   fun insert(parent: Audit, entity: AuditAction): AuditAction {
//      logger.debug("Inserting audit_action {}", entity)
//
//      return jdbc.insertReturning("""
//         INSERT INTO audit_action(changed_by, status_id, audit_id)
//         VALUES (:changed_by, :status_id, :audit_id)
//         RETURNING
//            *
//         """.trimIndent(),
//         mapOf(
//            "changed_by" to entity.changedBy.number,
//            "status_id" to entity.status.id,
//            "audit_id" to parent.id
//         ),
//         RowMapper { rs, _ ->
//            AuditAction(
//               rs.getLong("id"),
//               rs.getUuid("uu_row_id"),
//               rs.getOffsetDateTime("time_created"),
//               rs.getOffsetDateTime("time_updated"),
//               entity.status,
//               changedBy = entity.changedBy
//            )
//         }
//      )
//   }

   fun upsert(parent: Audit, entity: AuditAction): AuditAction {
      logger.debug("Upserting AuditAction {} {}", entity, parent)

      return if (entity.id != null) {
         logger.trace("Not necessary to insert {}", entity)

         entity
      } else {
         logger.trace("Inserting {}", entity)

         insert(parent, entity)
      }
   }

   fun mapRowOrNull(rs: ResultSet, rowPrefix: String = "aa_"): AuditAction? =
      rs.getString("${rowPrefix}id")?.let { mapRow(rs, rowPrefix) }

   fun mapRow(rs: ResultSet, rowPrefix: String = "aa_"): AuditAction =
      AuditAction(
         rs.getLong("${rowPrefix}id"),
         rs.getUuid("${rowPrefix}uu_row_id"),
         rs.getOffsetDateTime("${rowPrefix}time_created"),
         rs.getOffsetDateTime("${rowPrefix}time_updated"),
         auditStatusRepository.mapRow(rs, "astd_"),
         employeeRepository.mapRow(rs, "aer_")
      )
}
