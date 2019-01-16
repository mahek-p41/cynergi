package com.hightouchinc.cynergi.middleware.repository

import com.hightouchinc.cynergi.middleware.entity.ChecklistEmployment
import com.hightouchinc.cynergi.middleware.extensions.findFirstOrNull
import com.hightouchinc.cynergi.middleware.extensions.insertReturning
import com.hightouchinc.cynergi.middleware.extensions.ofPairs
import com.hightouchinc.cynergi.middleware.extensions.updateReturning
import org.apache.commons.lang3.StringUtils.EMPTY
import org.eclipse.collections.impl.factory.Maps
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.sql.ResultSet
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.UUID
import javax.inject.Singleton

@Singleton
class ChecklistEmploymentRepository(
   private val jdbc: NamedParameterJdbcTemplate
) : Repository<ChecklistEmployment> {
   private companion object {
      val logger: Logger = LoggerFactory.getLogger(ChecklistAutoRepository::class.java)
      val SIMPLE_CHECKLIST_EMPLOYMENT_ROW_MAPPER: RowMapper<ChecklistEmployment> = ChecklistEmploymentRowMapper()
      val PREFIXED_CHECKLIST_EMPLOYMENT_ROW_MAPPER: RowMapper<ChecklistEmployment> = ChecklistEmploymentRowMapper(rowPrefix = "ce_")
   }

   override fun findOne(id: Long): ChecklistEmployment? {
      val found = jdbc.findFirstOrNull("SELECT * FROM checklist_employment ce WHERE ce.id = :id", Maps.mutable.ofPairs("id" to id), SIMPLE_CHECKLIST_EMPLOYMENT_ROW_MAPPER)

      logger.trace("searching for {} resulted in {}", id, found)

      return found
   }

   override fun exists(id: Long): Boolean {
      val exists = jdbc.queryForObject("SELECT EXISTS(SELECT id FROM checklist_employment WHERE id = :id)", Maps.mutable.ofPairs("id" to id), Boolean::class.java)!!

      logger.trace("Checking if ID: {} exists resulted in {}", id, exists)

      return exists
   }

   override fun insert(entity: ChecklistEmployment): ChecklistEmployment {
      logger.trace("Inserting {}", entity)

      return jdbc.insertReturning("""
         INSERT INTO checklist_employment(department, hire_date, leave_message, name, reliable, title)
         VALUES(:department, :hire_date, :leave_message, :name, :reliable, :title)
         RETURNING
            *
         """.trimIndent(),
         Maps.mutable.ofPairs(
            "department" to entity.department,
            "hire_date" to entity.hireDate,
            "leave_message" to entity.leaveMessage,
            "name" to entity.name,
            "reliable" to entity.reliable,
            "title" to entity.title
         ),
         SIMPLE_CHECKLIST_EMPLOYMENT_ROW_MAPPER
      )
   }

   override fun update(entity: ChecklistEmployment): ChecklistEmployment {
      logger.trace("Updating {}", entity)

      return jdbc.updateReturning("""
         UPDATE checklist_employment
         SET
            department = :department,
            hire_date = :hire_date,
            leave_message = :leave_message,
            name = :name,
            reliable = :reliable,
            title = :title
         WHERE id = :id
         RETURN
            *
         """.trimIndent(),
         Maps.mutable.ofPairs(
            "id" to entity.id,
            "department" to entity.department,
            "hire_date" to entity.hireDate,
            "leave_message" to entity.leaveMessage,
            "name" to entity.name,
            "reliable" to entity.reliable,
            "title" to entity.title
         ),
         SIMPLE_CHECKLIST_EMPLOYMENT_ROW_MAPPER
      )
   }

   fun mapRowPrefixedRow(rs: ResultSet, row: Int): ChecklistEmployment? =
      rs.getString("ce_id")?.let { PREFIXED_CHECKLIST_EMPLOYMENT_ROW_MAPPER.mapRow(rs, row) }
}

private class ChecklistEmploymentRowMapper(
   private val rowPrefix: String = EMPTY
) : RowMapper<ChecklistEmployment> {
   override fun mapRow(rs: ResultSet, rowNum: Int): ChecklistEmployment =
      ChecklistEmployment(
         id = rs.getLong("${rowPrefix}id"),
         uuRowId = rs.getObject("${rowPrefix}uu_row_id", UUID::class.java),
         timeCreated = rs.getObject("${rowPrefix}time_created", OffsetDateTime::class.java),
         timeUpdated = rs.getObject("${rowPrefix}time_updated", OffsetDateTime::class.java),
         department = rs.getString("${rowPrefix}department"),
         hireDate = rs.getObject("${rowPrefix}hire_date", LocalDate::class.java),
         leaveMessage = rs.getBoolean("${rowPrefix}leave_message"),
         name = rs.getString("${rowPrefix}name"),
         reliable = rs.getBoolean("${rowPrefix}reliable"),
         title = rs.getString("${rowPrefix}title")
      )
}
