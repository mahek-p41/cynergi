package com.cynergisuite.middleware.repository

import com.cynergisuite.middleware.entity.VerificationAuto
import com.cynergisuite.middleware.entity.helper.SimpleIdentifiableEntity
import com.cynergisuite.middleware.extensions.findFirstOrNull
import com.cynergisuite.middleware.extensions.getLocalDateOrNull
import com.cynergisuite.middleware.extensions.getOffsetDateTime
import com.cynergisuite.middleware.extensions.getUuid
import com.cynergisuite.middleware.extensions.insertReturning
import com.cynergisuite.middleware.extensions.updateReturning
import io.micronaut.spring.tx.annotation.Transactional
import org.apache.commons.lang3.StringUtils.EMPTY
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.sql.ResultSet
import javax.inject.Singleton

@Singleton
class VerificationAutoRepository(
   private val jdbc: NamedParameterJdbcTemplate
) : Repository<VerificationAuto> {
   private val logger: Logger = LoggerFactory.getLogger(VerificationAutoRepository::class.java)
   private val simpleVerificationAutoRowMapper: RowMapper<VerificationAuto> = VerificationAutoRowMapper()
   private val prefixedVerificationAutoRowMapper: RowMapper<VerificationAuto> = VerificationAutoRowMapper(columnPrefix = "va_")

   override fun findOne(id: Long): VerificationAuto? {
      val found = jdbc.findFirstOrNull("SELECT * FROM verification_auto WHERE id = :id", mapOf("id" to id), simpleVerificationAutoRowMapper)

      logger.trace("Searching for VerificationAuto: {} resulted in {}", id, found)

      return found
   }

   override fun exists(id: Long): Boolean {
      val exists = jdbc.queryForObject("SELECT EXISTS(SELECT id FROM verification_auto WHERE id = :id)", mapOf("id" to id), Boolean::class.java)!!

      logger.trace("Checking if VerificationAuto: {} exists resulted in {}", id, exists)

      return exists
   }

   @Transactional
   override fun insert(entity: VerificationAuto): VerificationAuto {
      logger.debug("Inserting verification_auto {}", entity)

      return jdbc.insertReturning(
         """
         INSERT INTO verification_auto(address, comment, dealer_phone, diff_address, diff_employee, diff_phone, dmv_verify, employer, last_payment, name, next_payment, note, payment_frequency, payment, pending_action, phone, previous_loan, purchase_date, related, verification_id)
         VALUES (:address, :comment, :dealerPhone, :diffAddress, :diffEmployee, :diffPhone, :dmvVerify, :employer, :lastPayment, :name, :nextPayment, :note, :paymentFrequency, :payment, :pendingAction, :phone, :previousLoan, :purchaseDate, :related, :verification_id)
         RETURNING
            *
         """.trimIndent(),
         mapOf(
            "address" to entity.address,
            "comment" to entity.comment,
            "dealerPhone" to entity.dealerPhone,
            "diffAddress" to entity.diffAddress,
            "diffEmployee" to entity.diffEmployee,
            "diffPhone" to entity.diffPhone,
            "dmvVerify" to entity.dmvVerify,
            "employer" to entity.employer,
            "lastPayment" to entity.lastPayment,
            "name" to entity.name,
            "nextPayment" to entity.nextPayment,
            "note" to entity.note,
            "paymentFrequency" to entity.paymentFrequency,
            "payment" to entity.payment,
            "pendingAction" to entity.pendingAction,
            "phone" to entity.phone,
            "previousLoan" to entity.previousLoan,
            "purchaseDate" to entity.purchaseDate,
            "related" to entity.related,
            "verification_id" to entity.verification.entityId()
         ),
         simpleVerificationAutoRowMapper
      )
   }

   @Transactional
   override fun update(entity: VerificationAuto): VerificationAuto {
      logger.debug("Updating verification_auto {}", entity)

      return jdbc.updateReturning(
         """
         UPDATE verification_auto
         SET
            address = :address,
            comment = :comment,
            dealer_phone = :dealerPhone,
            diff_address = :diffAddress,
            diff_employee = :diffEmployee,
            diff_phone = :diffPhone,
            dmv_verify = :dmvVerify,
            employer = :employer,
            last_payment = :lastPayment,
            name = :name,
            next_payment = :nextPayment,
            note = :note,
            payment_frequency = :paymentFrequency,
            payment = :payment,
            pending_action = :pendingAction,
            phone = :phone,
            previous_loan = :previousLoan,
            purchase_date = :purchaseDate,
            related = :related
         WHERE id = :id
         RETURNING
            *
         """.trimIndent(),
         mapOf(
            "id" to entity.id,
            "address" to entity.address,
            "comment" to entity.comment,
            "dealerPhone" to entity.dealerPhone,
            "diffAddress" to entity.diffAddress,
            "diffEmployee" to entity.diffEmployee,
            "diffPhone" to entity.diffPhone,
            "dmvVerify" to entity.dmvVerify,
            "employer" to entity.employer,
            "lastPayment" to entity.lastPayment,
            "name" to entity.name,
            "nextPayment" to entity.nextPayment,
            "note" to entity.note,
            "paymentFrequency" to entity.paymentFrequency,
            "payment" to entity.payment,
            "pendingAction" to entity.pendingAction,
            "phone" to entity.phone,
            "previousLoan" to entity.previousLoan,
            "purchaseDate" to entity.purchaseDate,
            "related" to entity.related
         ),
         simpleVerificationAutoRowMapper
      )
   }

   @Transactional
   fun upsert(existing: VerificationAuto?, requestedChange: VerificationAuto): VerificationAuto? {
      return if (existing == null) {
         insert(entity = requestedChange)
      } else {
         update(entity = requestedChange)
      }
   }

   fun mapRowPrefixedRow(rs: ResultSet, row: Int = 0): VerificationAuto? =
      rs.getString("va_id")?.let { prefixedVerificationAutoRowMapper.mapRow(rs, row) }
}

private class VerificationAutoRowMapper(
   private val columnPrefix: String = EMPTY
) : RowMapper<VerificationAuto> {
   override fun mapRow(rs: ResultSet, rowNum: Int): VerificationAuto =
      VerificationAuto(
         id = rs.getLong("${columnPrefix}id"),
         uuRowId = rs.getUuid("${columnPrefix}uu_row_id"),
         timeCreated = rs.getOffsetDateTime("${columnPrefix}time_created"),
         timeUpdated = rs.getOffsetDateTime("${columnPrefix}time_updated"),
         address = rs.getBoolean("${columnPrefix}address"),
         comment = rs.getString("${columnPrefix}comment"),
         dealerPhone = rs.getString("${columnPrefix}dealer_phone"),
         diffAddress = rs.getString("${columnPrefix}diff_address"),
         diffEmployee = rs.getString("${columnPrefix}diff_employee"),
         diffPhone = rs.getString("${columnPrefix}diff_phone"),
         dmvVerify = rs.getBoolean("${columnPrefix}dmv_verify"),
         employer = rs.getBoolean("${columnPrefix}employer"),
         lastPayment = rs.getLocalDateOrNull("${columnPrefix}last_payment"),
         name = rs.getString("${columnPrefix}name"),
         nextPayment = rs.getLocalDateOrNull("${columnPrefix}next_payment"),
         note = rs.getString("${columnPrefix}note"),
         paymentFrequency = rs.getString("${columnPrefix}payment_frequency"),
         payment = rs.getBigDecimal("${columnPrefix}payment"),
         pendingAction = rs.getString("${columnPrefix}pending_action"),
         phone = rs.getBoolean("${columnPrefix}phone"),
         previousLoan = rs.getBoolean("${columnPrefix}previous_loan"),
         purchaseDate = rs.getLocalDateOrNull("${columnPrefix}purchase_date"),
         related = rs.getString("${columnPrefix}related"),
         verification = SimpleIdentifiableEntity(id = rs.getLong("${columnPrefix}verification_id"))
      )
}
