package com.cynergisuite.middleware.audit.exception.note.infrastructure

import com.cynergisuite.domain.SimpleIdentifiableEntity
import com.cynergisuite.extensions.getOffsetDateTime
import com.cynergisuite.extensions.getUuid
import com.cynergisuite.extensions.insertReturning
import com.cynergisuite.middleware.audit.exception.note.AuditExceptionNote
import com.cynergisuite.middleware.employee.Employee
import org.apache.commons.lang3.StringUtils.EMPTY
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.sql.ResultSet
import javax.inject.Singleton

@Singleton
class AuditExceptionNoteRepository(
   private val jdbc: NamedParameterJdbcTemplate
) {
   fun insert(note: AuditExceptionNote): AuditExceptionNote =
      jdbc.insertReturning("""
         INSERT INTO audit_exception_note (note, entered_by, audit_exception_id)
         VALUES (:note, :entered_by, :audit_exception_id)
         RETURNING 
            *
         """.trimIndent(),
         mapOf(
            "note" to note.note,
            "entered_by" to note.enteredBy.number,
            "audit_exception_id" to note.auditException.entityId()
         ),
         RowMapper { rs, _ -> mapRow(rs, note.enteredBy)!! }
      )

   fun upsert(note: AuditExceptionNote): AuditExceptionNote =
      if (note.id == null) {
         insert(note)
      } else {
         note
      }

   fun mapRow(rs: ResultSet, enteredBy: Employee, columnPrefix: String = EMPTY): AuditExceptionNote? =
      if (rs.getString("${columnPrefix}id") != null) {
         AuditExceptionNote(
            id = rs.getLong("${columnPrefix}id"),
            uuRowId = rs.getUuid("${columnPrefix}uu_row_id"),
            timeCreated = rs.getOffsetDateTime("${columnPrefix}time_created"),
            timeUpdated = rs.getOffsetDateTime("${columnPrefix}time_updated"),
            note = rs.getString("${columnPrefix}note"),
            enteredBy = enteredBy,
            auditException = SimpleIdentifiableEntity(rs.getLong("${columnPrefix}audit_exception_id"))
         )
      } else {
         null
      }
}
