package com.hightouchinc.cynergi.middleware.repository

import com.hightouchinc.cynergi.middleware.entity.ChecklistAuto
import com.hightouchinc.cynergi.middleware.extensions.findFirstOrNull
import com.hightouchinc.cynergi.middleware.extensions.ofPairs
import io.micronaut.spring.tx.annotation.Transactional
import org.apache.commons.lang3.StringUtils
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
class ChecklistAutoRepository(
   private val jdbc: NamedParameterJdbcTemplate
) : Repository<ChecklistAuto> {
   private companion object {
      val logger: Logger = LoggerFactory.getLogger(ChecklistAutoRepository::class.java)
      val SIMPLE_CHECKLIST_AUTO_ROW_MAPPER: RowMapper<ChecklistAuto> = ChecklistAutoRowMapper()
      val FIND_CHECKLIST_AUTO_ROW_MAPPER: RowMapper<ChecklistAuto> = ChecklistAutoRowMapper(tableAlias = "ca_")
   }

   fun mapRowPrefixedRow(rs: ResultSet, row: Int): ChecklistAuto? =
      rs.getString("ca_id")?.let { FIND_CHECKLIST_AUTO_ROW_MAPPER.mapRow(rs, row) }

   override fun findOne(id: Long): ChecklistAuto? {
      val fetched: ChecklistAuto? = jdbc.findFirstOrNull("SELECT * FROM checklist_auto ca WHERE ca.id = :id", Maps.mutable.ofPairs("id" to id), SIMPLE_CHECKLIST_AUTO_ROW_MAPPER)

      logger.trace("fetched {} resulted in {}", id, fetched)

      return fetched
   }

   override fun exists(id: Long): Boolean =
      jdbc.queryForObject("SELECT EXISTS(SELECT id FROM checklist_auto WHERE id = :id)", mapOf("id" to id), Boolean::class.java)!!

   @Transactional
   override fun insert(entity: ChecklistAuto): ChecklistAuto =
      jdbc.queryForObject(
         """
         INSERT INTO Checklist_Auto(address, comment, dealer_phone, diff_address, diff_employee, diff_phone, dmv_verify, employer, last_payment, name, next_payment, note, payment_frequency, payment, pending_action, phone, previous_loan, purchase_date, related)
         VALUES (:address, :comment, :dealerPhone, :diffAddress, :diffEmployee, :diffPhone, :dmvVerify, :employer, :lastPayment, :name, :nextPayment, :note, :paymentFrequency, :payment, :pendingAction, :phone, :previousLoan, :purchaseDate, :related)
         RETURNING *
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
         SIMPLE_CHECKLIST_AUTO_ROW_MAPPER
      )!!

   @Transactional
   override fun update(entity: ChecklistAuto): ChecklistAuto =
      jdbc.queryForObject(
         """
         UPDATE Checklist_Auto
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
         SIMPLE_CHECKLIST_AUTO_ROW_MAPPER
      )!!
}

private class ChecklistAutoRowMapper(
   private val tableAlias: String = StringUtils.EMPTY
): RowMapper<ChecklistAuto> {
   override fun mapRow(rs: ResultSet, rowNum: Int): ChecklistAuto? {
      return ChecklistAuto(
         id = rs.getLong("${tableAlias}id"),
         uuRowId = rs.getObject("${tableAlias}uu_row_id", UUID::class.java),
         timeCreated = rs.getObject("${tableAlias}time_created", OffsetDateTime::class.java),
         timeUpdated = rs.getObject("${tableAlias}time_updated", OffsetDateTime::class.java),
         address = rs.getBoolean("${tableAlias}address"),
         comment = rs.getString("${tableAlias}comment"),
         dealerPhone = rs.getString("${tableAlias}dealer_phone"),
         diffAddress = rs.getString("${tableAlias}diff_address"),
         diffEmployee = rs.getString("${tableAlias}diff_employee"),
         diffPhone = rs.getString("${tableAlias}diff_phone"),
         dmvVerify = rs.getBoolean("${tableAlias}dmv_verify"),
         employer = rs.getBoolean("${tableAlias}employer"),
         lastPayment = rs.getObject("${tableAlias}last_payment", LocalDate::class.java),
         name = rs.getString("${tableAlias}name"),
         nextPayment = rs.getObject("${tableAlias}next_payment", LocalDate::class.java),
         note = rs.getString("${tableAlias}note"),
         paymentFrequency = rs.getString("${tableAlias}payment_frequency"),
         payment = rs.getBigDecimal("${tableAlias}payment"),
         pendingAction = rs.getString("${tableAlias}pending_action"),
         phone = rs.getBoolean("${tableAlias}phone"),
         previousLoan = rs.getBoolean("${tableAlias}previous_loan"),
         purchaseDate = rs.getObject("${tableAlias}purchase_date", LocalDate::class.java),
         related = rs.getString("${tableAlias}related")
      )
   }
}
