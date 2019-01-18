package com.hightouchinc.cynergi.middleware.repository

import com.hightouchinc.cynergi.middleware.entity.VerificationEmployment
import com.hightouchinc.cynergi.middleware.extensions.findFirstOrNull
import com.hightouchinc.cynergi.middleware.extensions.insertReturning
import com.hightouchinc.cynergi.middleware.extensions.updateReturning
import io.micronaut.spring.tx.annotation.Transactional
import org.apache.commons.lang3.StringUtils.EMPTY
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
class VerificationEmploymentRepository(
   private val jdbc: NamedParameterJdbcTemplate
) : Repository<VerificationEmployment> {
   private val logger: Logger = LoggerFactory.getLogger(VerificationAutoRepository::class.java)
   private val simpleVerificationEmploymentRowMapper: RowMapper<VerificationEmployment> = VerificationEmploymentRowMapper()
   private val prefixedVerificationEmploymentRowMapper: RowMapper<VerificationEmployment> = VerificationEmploymentRowMapper(rowPrefix = "ve_")

   override fun findOne(id: Long): VerificationEmployment? {
      val found = jdbc.findFirstOrNull("SELECT * FROM verification_employment WHERE id = :id", mapOf("id" to id), simpleVerificationEmploymentRowMapper)

      logger.trace("searching for {} resulted in {}", id, found)

      return found
   }

   override fun exists(id: Long): Boolean {
      val exists = jdbc.queryForObject("SELECT EXISTS(SELECT id FROM verification_employment WHERE id = :id)", mapOf("id" to id), Boolean::class.java)!!

      logger.trace("Checking if ID: {} exists resulted in {}", id, exists)

      return exists
   }

   override fun insert(entity: VerificationEmployment): VerificationEmployment {
      logger.trace("Inserting {}", entity)

      return jdbc.insertReturning("""
         INSERT INTO verification_employment(department, hire_date, leave_message, name, reliable, title)
         VALUES(:department, :hire_date, :leave_message, :name, :reliable, :title)
         RETURNING
            *
         """.trimIndent(),
         mapOf(
            "department" to entity.department,
            "hire_date" to entity.hireDate,
            "leave_message" to entity.leaveMessage,
            "name" to entity.name,
            "reliable" to entity.reliable,
            "title" to entity.title
         ),
         simpleVerificationEmploymentRowMapper
      )
   }

   override fun update(entity: VerificationEmployment): VerificationEmployment {
      logger.trace("Updating {}", entity)

      return jdbc.updateReturning("""
         UPDATE verification_employment
         SET
            department = :department,
            hire_date = :hire_date,
            leave_message = :leave_message,
            name = :name,
            reliable = :reliable,
            title = :title
         WHERE id = :id
         RETURNING
            *
         """.trimIndent(),
         mapOf(
            "id" to entity.id,
            "department" to entity.department,
            "hire_date" to entity.hireDate,
            "leave_message" to entity.leaveMessage,
            "name" to entity.name,
            "reliable" to entity.reliable,
            "title" to entity.title
         ),
         simpleVerificationEmploymentRowMapper
      )
   }

   @Transactional
   fun upsert(existing: VerificationEmployment?, requestedChange: VerificationEmployment): VerificationEmployment? {
      return if (existing == null) {
         insert(entity = requestedChange)
      } else {
         update(entity = requestedChange)
      }
   }

   fun mapRowPrefixedRow(rs: ResultSet, row: Int = 0): VerificationEmployment? =
      rs.getString("ve_id")?.let { prefixedVerificationEmploymentRowMapper.mapRow(rs, row) }
}

private class VerificationEmploymentRowMapper(
   private val rowPrefix: String = EMPTY
) : RowMapper<VerificationEmployment> {
   override fun mapRow(rs: ResultSet, rowNum: Int): VerificationEmployment =
      VerificationEmployment(
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
