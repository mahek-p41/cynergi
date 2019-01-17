package com.hightouchinc.cynergi.middleware.repository

import com.hightouchinc.cynergi.middleware.entity.VerificationAuto
import com.hightouchinc.cynergi.middleware.extensions.findFirstOrNull
import com.hightouchinc.cynergi.middleware.extensions.insertReturning
import com.hightouchinc.cynergi.middleware.extensions.ofPairs
import com.hightouchinc.cynergi.middleware.extensions.updateReturning
import io.micronaut.spring.tx.annotation.Transactional
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
class VerificationAutoRepository(
   private val jdbc: NamedParameterJdbcTemplate
) : Repository<VerificationAuto> {
   private val logger: Logger = LoggerFactory.getLogger(VerificationAutoRepository::class.java)
   private val simpleVerificationAutoRowMapper: RowMapper<VerificationAuto> = VerificationAutoRowMapper()
   private val prefixedVerificationAutoRowMapper: RowMapper<VerificationAuto> = VerificationAutoRowMapper(rowPrefix = "va_")

   override fun findOne(id: Long): VerificationAuto? {
      val found = jdbc.findFirstOrNull("SELECT * FROM verification_auto ca WHERE ca.id = :id", Maps.mutable.ofPairs("id" to id), simpleVerificationAutoRowMapper)

      logger.trace("searching for {} resulted in {}", id, found)

      return found
   }

   override fun exists(id: Long): Boolean {
      val exists = jdbc.queryForObject("SELECT EXISTS(SELECT id FROM verification_auto WHERE id = :id)", Maps.mutable.ofPairs("id" to id), Boolean::class.java)!!

      logger.trace("Checking if ID: {} exists resulted in {}", id, exists)

      return exists
   }

   @Transactional
   override fun insert(entity: VerificationAuto): VerificationAuto {
      logger.trace("Inserting {}", entity)

      return jdbc.insertReturning(
         """
         INSERT INTO verification_auto(address, comment, dealer_phone, diff_address, diff_employee, diff_phone, dmv_verify, employer, last_payment, name, next_payment, note, payment_frequency, payment, pending_action, phone, previous_loan, purchase_date, related)
         VALUES (:address, :comment, :dealerPhone, :diffAddress, :diffEmployee, :diffPhone, :dmvVerify, :employer, :lastPayment, :name, :nextPayment, :note, :paymentFrequency, :payment, :pendingAction, :phone, :previousLoan, :purchaseDate, :related)
         RETURNING
            *
         """.trimIndent(),
         Maps.mutable.ofPairs(
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
   override fun update(entity: VerificationAuto): VerificationAuto {
      logger.trace("Updating {}", entity)

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
         Maps.mutable.ofPairs(
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
   fun upsert(existing: VerificationAuto?, requestedChange: VerificationAuto?): VerificationAuto? {
      return if (existing == null && requestedChange != null) {
         insert(entity = requestedChange)
      } else if (existing != null && requestedChange != null) {
         update(entity = requestedChange)
      } else {
         null
      }
   }

   fun mapRowPrefixedRow(rs: ResultSet, row: Int): VerificationAuto? =
      rs.getString("va_id")?.let { prefixedVerificationAutoRowMapper.mapRow(rs, row) }
}

private class VerificationAutoRowMapper(
   private val rowPrefix: String = EMPTY
) : RowMapper<VerificationAuto> {
   override fun mapRow(rs: ResultSet, rowNum: Int): VerificationAuto =
      VerificationAuto(
         id = rs.getLong("${rowPrefix}id"),
         uuRowId = rs.getObject("${rowPrefix}uu_row_id", UUID::class.java),
         timeCreated = rs.getObject("${rowPrefix}time_created", OffsetDateTime::class.java),
         timeUpdated = rs.getObject("${rowPrefix}time_updated", OffsetDateTime::class.java),
         address = rs.getBoolean("${rowPrefix}address"),
         comment = rs.getString("${rowPrefix}comment"),
         dealerPhone = rs.getString("${rowPrefix}dealer_phone"),
         diffAddress = rs.getString("${rowPrefix}diff_address"),
         diffEmployee = rs.getString("${rowPrefix}diff_employee"),
         diffPhone = rs.getString("${rowPrefix}diff_phone"),
         dmvVerify = rs.getBoolean("${rowPrefix}dmv_verify"),
         employer = rs.getBoolean("${rowPrefix}employer"),
         lastPayment = rs.getObject("${rowPrefix}last_payment", LocalDate::class.java),
         name = rs.getString("${rowPrefix}name"),
         nextPayment = rs.getObject("${rowPrefix}next_payment", LocalDate::class.java),
         note = rs.getString("${rowPrefix}note"),
         paymentFrequency = rs.getString("${rowPrefix}payment_frequency"),
         payment = rs.getBigDecimal("${rowPrefix}payment"),
         pendingAction = rs.getString("${rowPrefix}pending_action"),
         phone = rs.getBoolean("${rowPrefix}phone"),
         previousLoan = rs.getBoolean("${rowPrefix}previous_loan"),
         purchaseDate = rs.getObject("${rowPrefix}purchase_date", LocalDate::class.java),
         related = rs.getString("${rowPrefix}related")
      )
}
