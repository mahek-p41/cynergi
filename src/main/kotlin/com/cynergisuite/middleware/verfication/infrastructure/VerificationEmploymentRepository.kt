package com.cynergisuite.middleware.verfication.infrastructure

import com.cynergisuite.domain.SimpleLegacyIdentifiableEntity
import com.cynergisuite.extensions.findFirstOrNull
import com.cynergisuite.extensions.getLocalDateOrNull
import com.cynergisuite.extensions.getOffsetDateTime
import com.cynergisuite.extensions.insertReturning
import com.cynergisuite.extensions.queryForObject
import com.cynergisuite.extensions.updateReturning
import com.cynergisuite.middleware.verfication.VerificationEmployment
import io.micronaut.transaction.annotation.ReadOnly
import org.apache.commons.lang3.StringUtils.EMPTY
import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.core.mapper.RowMapper
import org.jdbi.v3.core.statement.StatementContext
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.sql.ResultSet
import jakarta.inject.Singleton
import javax.transaction.Transactional

@Singleton
class VerificationEmploymentRepository(
   private val jdbc: Jdbi
) {
   private val logger: Logger = LoggerFactory.getLogger(VerificationAutoRepository::class.java)
   private val simpleVerificationEmploymentRowMapper: RowMapper<VerificationEmployment> = VerificationEmploymentRowMapper()
   private val prefixedVerificationEmploymentRowMapper: RowMapper<VerificationEmployment> = VerificationEmploymentRowMapper(columnPrefix = "ve_")

   @ReadOnly
   fun findOne(id: Long): VerificationEmployment? {
      val found = jdbc.findFirstOrNull("SELECT * FROM verification_employment WHERE id = :id", mapOf("id" to id), simpleVerificationEmploymentRowMapper)

      logger.trace("Searching for VerificationEmployment: {} resulted in {}", id, found)

      return found
   }

   @ReadOnly
   fun exists(id: Long): Boolean {
      val exists = jdbc.queryForObject("SELECT EXISTS(SELECT id FROM verification_employment WHERE id = :id)", mapOf("id" to id), Boolean::class.java)

      logger.trace("Checking if VerificationEmployment: {} exists resulted in {}", id, exists)

      return exists
   }

   @Transactional
   fun insert(entity: VerificationEmployment): VerificationEmployment {
      logger.debug("Inserting verification_employment  {}", entity)

      return jdbc.insertReturning(
         """
         INSERT INTO verification_employment(department, hire_date, leave_message, name, reliable, title, verification_id)
         VALUES(:department, :hire_date, :leave_message, :name, :reliable, :title, :verification_id)
         RETURNING
            *
         """.trimIndent(),
         mapOf(
            "department" to entity.department,
            "hire_date" to entity.hireDate,
            "leave_message" to entity.leaveMessage,
            "name" to entity.name,
            "reliable" to entity.reliable,
            "title" to entity.title,
            "verification_id" to entity.verification.myId()
         ),
         simpleVerificationEmploymentRowMapper
      )
   }

   @Transactional
   fun update(entity: VerificationEmployment): VerificationEmployment {
      logger.debug("Updating verification_employment {}", entity)

      return jdbc.updateReturning(
         """
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

   fun mapRowPrefixedRow(rs: ResultSet, ctx: StatementContext): VerificationEmployment? =
      rs.getString("ve_id")?.let { prefixedVerificationEmploymentRowMapper.map(rs, ctx) }
}

private class VerificationEmploymentRowMapper(
   private val columnPrefix: String = EMPTY
) : RowMapper<VerificationEmployment> {
   override fun map(rs: ResultSet, ctx: StatementContext): VerificationEmployment =
      VerificationEmployment(
         id = rs.getLong("${columnPrefix}id"),
         timeCreated = rs.getOffsetDateTime("${columnPrefix}time_created"),
         timeUpdated = rs.getOffsetDateTime("${columnPrefix}time_updated"),
         department = rs.getString("${columnPrefix}department"),
         hireDate = rs.getLocalDateOrNull("${columnPrefix}hire_date"),
         leaveMessage = rs.getBoolean("${columnPrefix}leave_message"),
         name = rs.getString("${columnPrefix}name"),
         reliable = rs.getBoolean("${columnPrefix}reliable"),
         title = rs.getString("${columnPrefix}title"),
         verification = SimpleLegacyIdentifiableEntity(id = rs.getLong("${columnPrefix}verification_id"))
      )
}
