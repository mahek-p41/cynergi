package com.cynergisuite.middleware.vendor.payment.term.schedule.infrastructure

import com.cynergisuite.extensions.getIntOrNull
import com.cynergisuite.extensions.getUuid
import com.cynergisuite.extensions.insertReturning
import com.cynergisuite.extensions.query
import com.cynergisuite.extensions.softDelete
import com.cynergisuite.extensions.update
import com.cynergisuite.extensions.updateReturning
import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.vendor.payment.term.VendorPaymentTermEntity
import com.cynergisuite.middleware.vendor.payment.term.schedule.VendorPaymentTermScheduleEntity
import io.micronaut.transaction.annotation.ReadOnly
import jakarta.inject.Inject
import jakarta.inject.Singleton
import org.jdbi.v3.core.Jdbi
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.sql.ResultSet
import java.util.UUID
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

   @ReadOnly
   fun findAllByVendorPaymentTerm(id: UUID): MutableList<VendorPaymentTermScheduleEntity> {
      val elements = mutableListOf<VendorPaymentTermScheduleEntity>()

      jdbc.query(
         """
            SELECT
               vpts.id                    AS vpts_id,
               vpts.time_created          AS vpts_time_created,
               vpts.time_updated          AS vpts_time_updated,
               vpts.vendor_payment_term_id       AS vpts_vendor_payment_term_id,
               vpts.due_month             AS vpts_due_month,
               vpts.due_days              AS vpts_due_days,
               vpts.due_percent           AS vpts_due_percent,
               vpts.schedule_order_number AS vpts_schedule_order_number
            FROM vendor_payment_term_schedule vpts
            WHERE vpts.vendor_payment_term_id = :id
      """,
         mapOf("id" to id)
      ) { rs, _ ->
         elements.add(mapRow(rs))
      }
      return elements
   }

   @Transactional
   fun deleteNotIn(id: UUID, scheduleRecords: List<VendorPaymentTermScheduleEntity>) {

      jdbc.update(
         """
         UPDATE vendor_payment_term_schedule
         SET deleted = TRUE
         WHERE vendor_payment_term_id = :vendor_payment_term_id
               AND id NOT IN(<ids>)
               AND deleted = FALSE
         RETURNING
            *
         """.trimIndent(),
         mapOf(
            "vendor_payment_term_id" to id,
            "ids" to scheduleRecords.asSequence().map { it.id }.toList()
         )
      )
   }

   @Transactional
   fun deleteByVendorPaymentTerm(id: UUID) {
      logger.debug("Deleting VendorPaymentTermSchedule with id={}", id)

      val affectedRows = jdbc.softDelete(
         """
         UPDATE vendor_payment_term_schedule
         SET deleted = TRUE
         WHERE vendor_payment_term_id = :vpt AND deleted = FALSE
         """,
         mapOf("id" to id, "vpt" to id),
         "vendor_payment_term_schedule",
         "vendor_payment_term_id"
      )

      logger.info("Affected rows: {}", affectedRows)
   }

   private fun mapRow(rs: ResultSet): VendorPaymentTermScheduleEntity {
      return VendorPaymentTermScheduleEntity(
         id = rs.getUuid("vpts_id"),
         dueMonth = rs.getIntOrNull("vpts_due_month"),
         dueDays = rs.getIntOrNull("vpts_due_days")!!,
         duePercent = rs.getBigDecimal("vpts_due_percent"),
         scheduleOrderNumber = rs.getInt("vpts_schedule_order_number")
      )
   }

   private fun mapDdlRow(rs: ResultSet): VendorPaymentTermScheduleEntity {
      return VendorPaymentTermScheduleEntity(id = rs.getUuid("id"), dueMonth = rs.getIntOrNull("due_month"), dueDays = rs.getIntOrNull("due_days")!!, duePercent = rs.getBigDecimal("due_percent"), scheduleOrderNumber = rs.getInt("schedule_order_number"))
   }
}
