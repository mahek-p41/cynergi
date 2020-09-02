package com.cynergisuite.middleware.vendor.payment.term.schedule.infrastructure

import com.cynergisuite.extensions.getIntOrNull
import com.cynergisuite.extensions.insertReturning
import com.cynergisuite.extensions.updateReturning
import com.cynergisuite.middleware.company.infrastructure.CompanyRepository
import com.cynergisuite.middleware.vendor.payment.term.VendorPaymentTermEntity
import com.cynergisuite.middleware.vendor.payment.term.schedule.VendorPaymentTermScheduleEntity
import javax.transaction.Transactional
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.sql.ResultSet
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VendorPaymentTermScheduleRepository @Inject constructor(
   private val companyRepository: CompanyRepository,
   private val jdbc: NamedParameterJdbcTemplate
) {
   private val logger: Logger = LoggerFactory.getLogger(VendorPaymentTermScheduleRepository::class.java)

   fun upsert(vpts: VendorPaymentTermScheduleEntity, vpt: VendorPaymentTermEntity): VendorPaymentTermScheduleEntity =
      if (vpts.id == null) {
         insert(vpts, vpt)
      } else {
         update(vpts, vpt)
      }

   @Transactional
   fun insert(entity: VendorPaymentTermScheduleEntity, vendorPaymentTerm: VendorPaymentTermEntity): VendorPaymentTermScheduleEntity {
      logger.debug("Inserting VendorPaymentTermSchedule {}", entity)

      return jdbc.insertReturning(
         """
         INSERT INTO vendor_payment_term_schedule(payment_term_id, due_month, due_days, due_percent, schedule_order_number)
         VALUES (
            :payment_term_id,
            :due_month,
            :due_days,
            :due_percent,
            :schedule_order_number
         )
         RETURNING
            *
         """.trimIndent(),
         mapOf(
            "payment_term_id" to vendorPaymentTerm.myId(),
            "due_month" to entity.dueMonth,
            "due_days" to entity.dueDays,
            "due_percent" to entity.duePercent,
            "schedule_order_number" to entity.scheduleOrderNumber
         ),
         RowMapper { rs, _ -> mapDdlRow(rs) }
      )
   }

   @Transactional
   fun update(entity: VendorPaymentTermScheduleEntity, vendorPaymentTerm: VendorPaymentTermEntity): VendorPaymentTermScheduleEntity {
      logger.debug("Updating VendorPaymentTermSchedule {}", entity)

      return jdbc.updateReturning(
         """
         UPDATE vendor_payment_term_schedule
         SET
            payment_term_id = :payment_term_id,
            due_month = :due_month,
            due_days = :due_days,
            due_percent = :due_percent,
            schedule_order_number = :schedule_order_number
         WHERE id = :id
         RETURNING
            *
         """.trimIndent(),
         mapOf(
            "payment_term_id" to vendorPaymentTerm.myId(),
            "due_month" to entity.dueMonth,
            "due_days" to entity.dueDays,
            "due_percent" to entity.duePercent,
            "schedule_order_number" to entity.scheduleOrderNumber,
            "id" to entity.id
         ),
         RowMapper { rs, _ -> mapDdlRow(rs) }
      )
   }

   @Transactional
   fun deleteNotIn(vpt: VendorPaymentTermEntity, scheduleRecords: List<VendorPaymentTermScheduleEntity>): List<VendorPaymentTermScheduleEntity> {
      val result = mutableListOf<VendorPaymentTermScheduleEntity>()

      jdbc.query(
         """
         DELETE FROM vendor_payment_term_schedule
         WHERE payment_term_id = :payment_term_id
               AND id NOT IN(:ids)
         RETURNING
            *
         """.trimIndent(),
         mapOf(
            "payment_term_id" to vpt.id,
            "ids" to scheduleRecords.asSequence().map { it.id }.toList()
         )
      ) { rs ->
         result.add(
            VendorPaymentTermScheduleEntity(
               id = rs.getLong("id"),
               dueMonth = rs.getInt("due_month"),
               dueDays = rs.getInt("due_days"),
               duePercent = rs.getBigDecimal("due_percent"),
               scheduleOrderNumber = rs.getInt("schedule_order_number")
            )
         )
      }

      return result
   }

   private fun mapRow(rs: ResultSet): VendorPaymentTermScheduleEntity {
      return VendorPaymentTermScheduleEntity(id = rs.getLong("vpts_id"), dueMonth = rs.getIntOrNull("vpts_due_month"), dueDays = rs.getIntOrNull("vpts_due_days")!!, duePercent = rs.getBigDecimal("vpts_due_percent"), scheduleOrderNumber = rs.getInt("vpts_schedule_order_number"))
   }

   private fun mapDdlRow(rs: ResultSet): VendorPaymentTermScheduleEntity {
      return VendorPaymentTermScheduleEntity(id = rs.getLong("id"), dueMonth = rs.getIntOrNull("due_month"), dueDays = rs.getIntOrNull("due_days")!!, duePercent = rs.getBigDecimal("due_percent"), scheduleOrderNumber = rs.getInt("schedule_order_number"))
   }
}
