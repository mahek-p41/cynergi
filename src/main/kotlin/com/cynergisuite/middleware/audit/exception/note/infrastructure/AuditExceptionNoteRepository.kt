package com.cynergisuite.middleware.audit.exception.note.infrastructure

import com.cynergisuite.domain.SimpleIdentifiableEntity
import com.cynergisuite.extensions.getOffsetDateTime
import com.cynergisuite.extensions.getUuid
import com.cynergisuite.extensions.insertReturning
import com.cynergisuite.middleware.audit.exception.note.AuditExceptionNote
import com.cynergisuite.middleware.employee.EmployeeEntity
import org.apache.commons.lang3.StringUtils.EMPTY
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.sql.ResultSet
import javax.inject.Singleton

@Singleton
class AuditExceptionNoteRepository(
   private val jdbc: NamedParameterJdbcTemplate
) {
   private val logger: Logger = LoggerFactory.getLogger(AuditExceptionNoteRepository::class.java)

   fun insert(note: AuditExceptionNote): AuditExceptionNote {
      logger.debug("Inserting AuditExceptionNote {}", note)

      return jdbc.insertReturning(
         """
         INSERT INTO audit_exception_note (note, entered_by, audit_exception_id)
         VALUES (:note, :entered_by, :audit_exception_id)
         RETURNING
            *
         """.trimIndent(),
         mapOf(
            "note" to note.note,
            "entered_by" to note.enteredBy.number,
            "audit_exception_id" to note.auditException.myId()
         )
      ) { rs, _ ->
         AuditExceptionNote(
            id = rs.getUuid("id"),
            timeCreated = rs.getOffsetDateTime("time_created"),
            timeUpdated = rs.getOffsetDateTime("time_updated"),
            note = rs.getString("note"),
            enteredBy = note.enteredBy,
            auditException = SimpleIdentifiableEntity(rs.getUuid("audit_exception_id"))
         )
      }
   }

   fun upsert(note: AuditExceptionNote): AuditExceptionNote =
      if (note.id == null) {
         insert(note)
      } else {
         note
      }

   fun mapRow(rs: ResultSet, enteredBy: EmployeeEntity, columnPrefix: String = EMPTY): AuditExceptionNote? =
      if (rs.getString("${columnPrefix}id") != null) {
         AuditExceptionNote(
            id = rs.getUuid("${columnPrefix}id"),
            timeCreated = rs.getOffsetDateTime("${columnPrefix}time_created"),
            timeUpdated = rs.getOffsetDateTime("${columnPrefix}time_updated"),
            note = rs.getString("${columnPrefix}note"),
            enteredBy = enteredBy,
            auditException = SimpleIdentifiableEntity(rs.getUuid("${columnPrefix}audit_exception_id"))
         )
      } else {
         null
      }
}
