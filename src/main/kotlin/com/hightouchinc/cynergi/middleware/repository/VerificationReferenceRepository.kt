package com.hightouchinc.cynergi.middleware.repository

import com.hightouchinc.cynergi.middleware.entity.VerificationReference
import com.hightouchinc.cynergi.middleware.entity.helper.SimpleIdentifiableEntity
import com.hightouchinc.cynergi.middleware.extensions.findFirstOrNull
import com.hightouchinc.cynergi.middleware.extensions.insertReturning
import com.hightouchinc.cynergi.middleware.extensions.updateReturning
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.sql.ResultSet
import java.time.OffsetDateTime
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VerificationReferenceRepository @Inject constructor(
   private val jdbc: NamedParameterJdbcTemplate
) : Repository<VerificationReference> {
   private val logger: Logger = LoggerFactory.getLogger(VerificationReferenceRepository::class.java)
   private val simpleVerificationReferenceRowMapper = VerificationReferenceRowMapper()

   override fun findOne(id: Long): VerificationReference? {
      val found = jdbc.findFirstOrNull("SELECT * FROM verification_reference ca WHERE ca.id = :id", mapOf("id" to id), simpleVerificationReferenceRowMapper)

      logger.trace("searching for {} resulted in {}", id, found)

      return found
   }

   override fun exists(id: Long): Boolean {
      val exists = jdbc.queryForObject("SELECT EXISTS(SELECT id FROM verification_reference WHERE id = :id)", mapOf("id" to id), Boolean::class.java)!!

      logger.trace("Checking if ID: {} exists resulted in {}", id, exists)

      return exists
   }

   override fun insert(entity: VerificationReference): VerificationReference {
      logger.trace("Inserting {}", entity)

      return jdbc.insertReturning("""
         INSERT INTO verification_reference(address, has_home_phone, known, leave_message, rating, relationship, reliable, time_frame, verify_phone, verification_id)
         VALUES (:address, :has_home_phone, :known, :leave_message, :rating, :relationship, :reliable, :time_frame, :verify_phone, :verification_id)
         RETURNING
            *
         """.trimIndent(),
         mapOf(
            "address" to entity.address,
            "has_home_phone" to entity.hasHomePhone,
            "known" to entity.known,
            "leave_message" to entity.leaveMessage,
            "rating" to entity.rating,
            "relationship" to entity.relationship,
            "reliable" to entity.reliable,
            "time_frame" to entity.timeFrame,
            "verify_phone" to entity.verifyPhone,
            "verification_id" to entity.verification.entityId()
         ),
         simpleVerificationReferenceRowMapper
      )
   }

   override fun update(entity: VerificationReference): VerificationReference {
      logger.trace("Updating {}", entity)

      return jdbc.updateReturning("""
         UPDATE verification_reference
         SET
            address = :address,
            has_home_phone = :has_home_phone,
            known = :known,
            leave_message = :leave_message,
            rating = :rating,
            relationship = :relationship,
            reliable = :reliable,
            time_frame = :time_frame,
            verify_phone = :verify_phone,
            verification_id = :verification_id
         WHERE id = :id
         RETURNING
            *
         """.trimIndent(),
         mapOf(
            "id" to entity.id,
            "address" to entity.address,
            "has_home_phone" to entity.hasHomePhone,
            "known" to entity.known,
            "leave_message" to entity.leaveMessage,
            "rating" to entity.rating,
            "relationship" to entity.relationship,
            "reliable" to entity.reliable,
            "time_frame" to entity.timeFrame,
            "verify_phone" to entity.verifyPhone,
            "verification_id" to entity.verification.entityId()
         ),
         simpleVerificationReferenceRowMapper
      )
   }
}

private class VerificationReferenceRowMapper : RowMapper<VerificationReference> {
   override fun mapRow(rs: ResultSet, rowNum: Int): VerificationReference =
      VerificationReference(
         id = rs.getLong("id"),
         uuRowId = rs.getObject("uu_row_id", UUID::class.java),
         timeCreated = rs.getObject("time_created", OffsetDateTime::class.java),
         timeUpdated = rs.getObject("time_updated", OffsetDateTime::class.java),
         address = rs.getBoolean("address"),
         hasHomePhone = rs.getBoolean("has_home_phone"),
         known = rs.getInt("known"),
         leaveMessage = rs.getBoolean("leave_message"),
         rating = rs.getString("rating"),
         relationship = rs.getBoolean("relationship"),
         reliable = rs.getBoolean("reliable"),
         timeFrame = rs.getInt("time_frame"),
         verifyPhone = rs.getBoolean("verify_phone"),
         verification = SimpleIdentifiableEntity(id = rs.getLong("verification_id"))
      )
}
