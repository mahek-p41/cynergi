package com.cynergisuite.middleware.verfication.infrastructure

import com.cynergisuite.domain.SimpleIdentifiableEntity
import com.cynergisuite.extensions.findFirstOrNull
import com.cynergisuite.extensions.getOffsetDateTime
import com.cynergisuite.extensions.insertReturning
import com.cynergisuite.extensions.updateReturning
import com.cynergisuite.middleware.verfication.VerificationLandlord
import io.micronaut.spring.tx.annotation.Transactional
import org.apache.commons.lang3.StringUtils.EMPTY
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.sql.ResultSet
import javax.inject.Singleton

@Singleton
class VerificationLandlordRepository(
   private val jdbc: NamedParameterJdbcTemplate
) {
   private val logger: Logger = LoggerFactory.getLogger(VerificationLandlordRepository::class.java)
   private val simpleVerificationLandlordRowMapper = VerificationLandlordRowMapper()
   private val prefixedVerificationLandlordRowMapper = VerificationLandlordRowMapper(columnPrefix = "vl_")

   fun findOne(id: Long): VerificationLandlord? {
      val found = jdbc.findFirstOrNull("SELECT * FROM verification_landlord WHERE id = :id", mapOf("id" to id), simpleVerificationLandlordRowMapper)

      logger.trace("searching for VerificationLandlord: {} resulted in {}", id, found)

      return found
   }

   fun exists(id: Long): Boolean {
      val exists = jdbc.queryForObject("SELECT EXISTS(SELECT id FROM verification_landlord WHERE id = :id)", mapOf("id" to id), Boolean::class.java)!!

      logger.trace("Checking if VerificationLandlord: {} exists resulted in {}", id, exists)

      return exists
   }

   @Transactional
   fun insert(entity: VerificationLandlord): VerificationLandlord {
      logger.trace("Inserting verification_landlord {}", entity)

      return jdbc.insertReturning("""
         INSERT INTO verification_landlord(address, alt_phone, lease_type, leave_message, length, name, paid_rent, phone, reliable, rent, verification_id)
         VALUES(:address, :alt_phone, :lease_type, :leave_message, :length, :name, :paid_rent, :phone, :reliable, :rent, :verification_id)
         RETURNING
            *
         """.trimIndent(),
         mapOf(
            "address" to entity.address,
            "alt_phone" to entity.altPhone,
            "lease_type" to entity.leaseType,
            "leave_message" to entity.leaveMessage,
            "length" to entity.length,
            "name" to entity.name,
            "paid_rent" to entity.paidRent,
            "phone" to entity.phone,
            "reliable" to entity.reliable,
            "rent" to entity.rent,
            "verification_id" to entity.verification.myId()
         ),
         simpleVerificationLandlordRowMapper
      )
   }

   @Transactional
   fun update(entity: VerificationLandlord): VerificationLandlord {
      logger.trace("Updating verification_landlord {}", entity)

      return jdbc.updateReturning("""
         UPDATE verification_landlord
         SET
            address = :address,
            alt_phone = :alt_phone,
            lease_type = :lease_type,
            leave_message = :leave_message,
            length = :length,
            name = :name,
            paid_rent = :paid_rent,
            phone = :phone,
            reliable = :reliable,
            rent = :rent
         WHERE id = :id
         RETURNING
            *
         """.trimIndent(),
         mapOf(
            "id" to entity.id,
            "address" to entity.address,
            "alt_phone" to entity.altPhone,
            "lease_type" to entity.leaseType,
            "leave_message" to entity.leaveMessage,
            "length" to entity.length,
            "name" to entity.name,
            "paid_rent" to entity.paidRent,
            "phone" to entity.phone,
            "reliable" to entity.reliable,
            "rent" to entity.rent
         ),
         simpleVerificationLandlordRowMapper
      )
   }

   @Transactional
   fun upsert(existing: VerificationLandlord?, requestedChange: VerificationLandlord): VerificationLandlord? {
      return if (existing == null) {
         insert(entity = requestedChange)
      } else {
         update(entity = requestedChange)
      }
   }

   fun mapRowPrefixedRow(rs: ResultSet, row: Int = 0): VerificationLandlord? =
      rs.getString("vl_id")?.let { prefixedVerificationLandlordRowMapper.mapRow(rs, row) }
}

private class VerificationLandlordRowMapper(
   private val columnPrefix: String = EMPTY
) : RowMapper<VerificationLandlord> {
   override fun mapRow(rs: ResultSet, rowNum: Int): VerificationLandlord =
      VerificationLandlord(
         id = rs.getLong("${columnPrefix}id"),
         timeCreated = rs.getOffsetDateTime("${columnPrefix}time_created"),
         timeUpdated = rs.getOffsetDateTime("${columnPrefix}time_updated"),
         address = rs.getBoolean("${columnPrefix}address"),
         altPhone = rs.getString("${columnPrefix}alt_phone"),
         leaseType = rs.getString("${columnPrefix}lease_type"),
         leaveMessage = rs.getBoolean("${columnPrefix}leave_message"),
         length = rs.getString("${columnPrefix}length"),
         name = rs.getString("${columnPrefix}name"),
         paidRent = rs.getString("${columnPrefix}paid_rent"),
         phone = rs.getBoolean("${columnPrefix}phone"),
         reliable = rs.getBoolean("${columnPrefix}reliable"),
         rent = rs.getBigDecimal("${columnPrefix}rent"),
         verification = SimpleIdentifiableEntity(id = rs.getLong("${columnPrefix}verification_id"))
      )
}
