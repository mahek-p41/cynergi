package com.hightouchinc.cynergi.middleware.repository

import com.hightouchinc.cynergi.middleware.entity.ChecklistAuto
import com.hightouchinc.cynergi.middleware.extensions.ofPairs
import com.hightouchinc.cynergi.middleware.repository.spi.RepositoryBase
import io.micronaut.spring.tx.annotation.Transactional
import org.eclipse.collections.impl.factory.Maps
import org.intellij.lang.annotations.Language
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID
import javax.inject.Singleton

@Singleton
class ChecklistAutoRepository(
   jdbc: NamedParameterJdbcTemplate
) : RepositoryBase<ChecklistAuto>(
   tableName = "checklist_auto",
   jdbc = jdbc,
   entityRowMapper = CHECKLIST_AUTO_ROW_MAPPER
) {
   private companion object {
      val CHECKLIST_AUTO_ROW_MAPPER: RowMapper<ChecklistAuto> = RowMapper { rs, _ ->
         ChecklistAuto(
            id = rs.getLong("id"),
            uuRowId = rs.getObject("uu_row_id", UUID::class.java),
            timeCreated = rs.getObject("time_created", LocalDateTime::class.java),
            timeUpdated = rs.getObject("time_updated", LocalDateTime::class.java),
            address = rs.getBoolean("address"),
            comment = rs.getString("comment"),
            dealerPhone = rs.getString("dealer_phone"),
            diffAddress = rs.getString("diff_address"),
            diffEmployee = rs.getString("diff_employee"),
            diffPhone = rs.getString("diff_phone"),
            dmvVerify = rs.getBoolean("dmv_verify"),
            employer = rs.getBoolean("employer"),
            lastPayment = rs.getObject("last_payment", LocalDate::class.java),
            name = rs.getString("name"),
            nextPayment = rs.getObject("next_payment", LocalDate::class.java),
            note = rs.getString("note"),
            paymentFrequency = rs.getString("payment_frequency"),
            payment = rs.getBigDecimal("payment"),
            pendingAction = rs.getString("pending_action"),
            phone = rs.getBoolean("phone"),
            previousLoan = rs.getBoolean("previous_loan"),
            purchaseDate = rs.getObject("purchase_date", LocalDate::class.java),
            related = rs.getString("related")
         )
      }

      @Language("PostgreSQL")
      val CHECKLIST_AUTO_INSERT = """
         INSERT INTO Checklist_Auto(address, comment, dealer_phone, diff_address, diff_employee, diff_phone, dmv_verify, employer, last_payment, name, next_payment, note, payment_frequency, payment, pending_action, phone, previous_loan, purchase_date, related)
         VALUES (:address, :comment, :dealerPhone, :diffAddress, :diffEmployee, :diffPhone, :dmvVerify, :employer, :lastPayment, :name, :nextPayment, :note, :paymentFrequency, :payment, :pendingAction, :phone, :previousLoan, :purchaseDate, :related)
         RETURNING *
       """.trimIndent()

      val CHECKLIST_AUTO_UPDATE = """
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
      """.trimIndent()
   }

   @Transactional
   override fun insert(entity: ChecklistAuto): ChecklistAuto =
      jdbc.queryForObject(
         CHECKLIST_AUTO_INSERT,
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
         CHECKLIST_AUTO_ROW_MAPPER
      )!!

   @Transactional
   override fun update(entity: ChecklistAuto): ChecklistAuto =
      jdbc.queryForObject(
         CHECKLIST_AUTO_UPDATE,
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
         CHECKLIST_AUTO_ROW_MAPPER
      )!!
}


