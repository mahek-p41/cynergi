package com.cynergisuite.middleware.vendor.payment.term.schedule.infrastructure

import com.cynergisuite.extensions.getIntOrNull
import com.cynergisuite.extensions.getUuid
import com.cynergisuite.extensions.insertReturning
import com.cynergisuite.extensions.query
import com.cynergisuite.extensions.updateReturning
import com.cynergisuite.middleware.vendor.payment.term.VendorPaymentTermEntity
import com.cynergisuite.middleware.vendor.payment.term.schedule.VendorPaymentTermScheduleEntity
import org.jdbi.v3.core.Jdbi
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.sql.ResultSet
import javax.inject.Inject
import javax.inject.Singleton
import javax.transaction.Transactional

@Singleton
class VendorPaymentTermScheduleRepository @Inject constructor(
   private val jdbc: Jdbi
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
         INSERT INTO vendor_payment_term_schedule(vendor_payment_term_id, due_month, due_days, due_percent, schedule_order_number)
         VALUES (
            :vendor_payment_term_id,
            :due_month,
            :due_days,
            :due_percent,
            :schedule_order_number
         )
         RETURNING
            *
         """.trimIndent(),
         mapOf(
            "vendor_payment_term_id" to vendorPaymentTerm.myId(),
            "due_month" to entity.dueMonth,
            "due_days" to entity.dueDays,
            "due_percent" to entity.duePercent,
            "schedule_order_number" to entity.scheduleOrderNumber
         )
      ) { rs, _ -> mapDdlRow(rs) }
   }

   @Transactional
   fun update(entity: VendorPaymentTermScheduleEntity, vendorPaymentTerm: VendorPaymentTermEntity): VendorPaymentTermScheduleEntity {
      logger.debug("Updating VendorPaymentTermSchedule {}", entity)

      return jdbc.updateReturning(
         """
         UPDATE vendor_payment_term_schedule
         SET
            vendor_payment_term_id = :vendor_payment_term_id,
            due_month = :due_month,
            due_days = :due_days,
            due_percent = :due_percent,
            schedule_order_number = :schedule_order_number
         WHERE id = :id
         RETURNING
            *
         """.trimIndent(),
         mapOf(
            "vendor_payment_term_id" to vendorPaymentTerm.myId(),
            "due_month" to entity.dueMonth,
            "due_days" to entity.dueDays,
            "due_percent" to entity.duePercent,
            "schedule_order_number" to entity.scheduleOrderNumber,
            "id" to entity.id
         )
      ) { rs, _ -> mapDdlRow(rs) }
   }

   @Transactional
   fun deleteNotIn(vpt: VendorPaymentTermEntity, scheduleRecords: List<VendorPaymentTermScheduleEntity>): List<VendorPaymentTermScheduleEntity> {
      val result = mutableListOf<VendorPaymentTermScheduleEntity>()

      jdbc.query(
         """
         DELETE FROM vendor_payment_term_schedule
         WHERE vendor_payment_term_id = :vendor_payment_term_id
               AND id NOT IN(<ids>)
         RETURNING
            *
         """.trimIndent(),
         mapOf(
            "vendor_payment_term_id" to vpt.id,
            "ids" to scheduleRecords.asSequence().map { it.id }.toList()
         )
      ) { rs, _ ->
         result.add(
            VendorPaymentTermScheduleEntity(
               id = rs.getUuid("id"),
               dueMonth = rs.getInt("due_month"),
               dueDays = rs.getInt("due_days"),
               duePercent = rs.getBigDecimal("due_percent"),
               scheduleOrderNumber = rs.getInt("schedule_order_number")
            )
         )
      }

      return result
   }

   private fun mapDdlRow(rs: ResultSet): VendorPaymentTermScheduleEntity {
      return VendorPaymentTermScheduleEntity(id = rs.getUuid("id"), dueMonth = rs.getIntOrNull("due_month"), dueDays = rs.getIntOrNull("due_days")!!, duePercent = rs.getBigDecimal("due_percent"), scheduleOrderNumber = rs.getInt("schedule_order_number"))
   }
}
