package com.hightouchinc.cynergi.middleware.repository

import com.hightouchinc.cynergi.middleware.entity.Verification
import com.hightouchinc.cynergi.middleware.entity.VerificationReference
import com.hightouchinc.cynergi.middleware.entity.helper.SimpleIdentifiableEntity
import com.hightouchinc.cynergi.middleware.extensions.findFirstOrNull
import com.hightouchinc.cynergi.middleware.extensions.getOffsetDateTime
import com.hightouchinc.cynergi.middleware.extensions.getUUID
import com.hightouchinc.cynergi.middleware.extensions.insertReturning
import com.hightouchinc.cynergi.middleware.extensions.updateReturning
import io.micronaut.spring.tx.annotation.Transactional
import org.apache.commons.lang3.StringUtils.EMPTY
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.sql.ResultSet
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VerificationReferenceRepository @Inject constructor(
   private val jdbc: NamedParameterJdbcTemplate
) : Repository<VerificationReference> {
   private val logger: Logger = LoggerFactory.getLogger(VerificationReferenceRepository::class.java)
   private val simpleVerificationReferenceRowMapper = VerificationReferenceRowMapper()
   private val prefixedVerificationReferenceRowMapper = VerificationReferenceRowMapper("vr_")

   override fun findOne(id: Long): VerificationReference? {
      val found = jdbc.findFirstOrNull("SELECT * FROM verification_reference WHERE id = :id", mapOf("id" to id), simpleVerificationReferenceRowMapper)

      logger.trace("searching for VerificationLandlord: {} resulted in {}", id, found)

      return found
   }

   fun findAll(verification: Verification): List<VerificationReference> {
      val vid = verification.id
      val result = if (vid != null) {
         jdbc.query("SELECT * FROM verification_reference WHERE verification_id = :vid", mapOf("vid" to verification.id), simpleVerificationReferenceRowMapper)
      } else {
         emptyList()
      }

      logger.trace("Searching for all children of VerificationLandlord: {} and found {}", verification, result)

      return result
   }

   override fun exists(id: Long): Boolean {
      val exists = jdbc.queryForObject("SELECT EXISTS(SELECT id FROM verification_reference WHERE id = :id)", mapOf("id" to id), Boolean::class.java)!!

      logger.trace("Checking if VerificationLandlord: {} exists resulted in {}", id, exists)

      return exists
   }

   @Transactional
   override fun insert(entity: VerificationReference): VerificationReference {
      logger.trace("Inserting verification_reference {}", entity)

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

   @Transactional
   override fun update(entity: VerificationReference): VerificationReference {
      logger.trace("Updating verification_reference {}", entity)

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

   @Transactional
   fun upsert(entity: VerificationReference): VerificationReference {
      return if (entity.id == null) {
         insert(entity)
      } else {
         update(entity)
      }
   }

   @Transactional
   fun deleteAll(entities: Collection<VerificationReference>): Int =
      jdbc.update(
         "DELETE FROM verification_reference WHERE id IN (:ids)",
         mapOf("ids" to entities.asSequence().filter { it.id != null }.map { it.id }.toSet())
      )

   fun mapRowPrefixedRow(rs: ResultSet, row: Int = 0): VerificationReference? =
      rs.getString("vr_id")?.let { prefixedVerificationReferenceRowMapper.mapRow(rs, row) }
}

private class VerificationReferenceRowMapper(
   private val rowPrefix: String = EMPTY
) : RowMapper<VerificationReference> {
   override fun mapRow(rs: ResultSet, rowNum: Int): VerificationReference =
      VerificationReference(
         id = rs.getLong("${rowPrefix}id"),
         uuRowId = rs.getUUID("${rowPrefix}uu_row_id"),
         timeCreated = rs.getOffsetDateTime("${rowPrefix}time_created"),
         timeUpdated = rs.getOffsetDateTime("${rowPrefix}time_updated"),
         address = rs.getBoolean("${rowPrefix}address"),
         hasHomePhone = rs.getBoolean("${rowPrefix}has_home_phone"),
         known = rs.getInt("${rowPrefix}known"),
         leaveMessage = rs.getBoolean("${rowPrefix}leave_message"),
         rating = rs.getString("${rowPrefix}rating"),
         relationship = rs.getBoolean("${rowPrefix}relationship"),
         reliable = rs.getBoolean("${rowPrefix}reliable"),
         timeFrame = rs.getInt("${rowPrefix}time_frame"),
         verifyPhone = rs.getBoolean("${rowPrefix}verify_phone"),
         verification = SimpleIdentifiableEntity(id = rs.getLong("${rowPrefix}verification_id"))
      )
}
