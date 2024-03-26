package com.cynergisuite.middleware.audit.inventory.infrastructure

import com.cynergisuite.extensions.update
import com.cynergisuite.middleware.audit.AuditEntity
import com.cynergisuite.middleware.inventory.infrastructure.InventoryRepository
import jakarta.inject.Inject
import jakarta.inject.Singleton
import org.jdbi.v3.core.Jdbi
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.transaction.Transactional

@Singleton
class AuditInventoryRepository @Inject constructor(
   private val inventoryRepository: InventoryRepository,
   private val jdbc: Jdbi
) {
   private val logger: Logger = LoggerFactory.getLogger(AuditInventoryRepository::class.java)

   private val selectInventoryInformation =
      """
      i.uu_row_id                   AS id,
      i.serial_number               AS serial_number,
      i.lookup_key                  AS lookup_key,
      i.lookup_key_type             AS lookup_key_type,
      i.barcode                     AS barcode,
      i.alt_id                      AS alternate_id,
      i.brand                       AS brand,
      i.model_number                AS model_number,
      i.product_code                AS product_code,
      i.description                 AS description,
      i.received_date               AS received_date,
      i.original_cost               AS original_cost,
      i.actual_cost                 AS actual_cost,
      i.model_category              AS model_category,
      i.times_rented                AS times_rented,
      i.total_revenue               AS total_revenue,
      i.remaining_value             AS remaining_value,
      i.sell_price                  AS sell_price,
      i.assigned_value              AS assigned_value,
      i.idle_days                   AS idle_days,
      i.condition                   AS condition,
      i.returned_date               AS returned_date,
      i.status                      AS status,
      i.dataset                     AS dataset
      """.trimIndent()

      val selectAuditInventoryAndException =
      """
      ${inventoryRepository.selectCompanyAndAddress},
      ${inventoryRepository.selectStoreInformation},
      ${inventoryRepository.selectAudit},
      ${selectInventoryInformation},
      ${inventoryRepository.selectException},
      ${inventoryRepository.selectScannedBy},
      count(*) OVER() AS total_elements
      """.trimIndent()

   @Transactional
   fun createInventorySnapshot(entity: AuditEntity) {
      // Inventory snapshot creates snapshot for all inventory items in statuses ('N', 'R', 'D')
      logger.debug("Create inventory snapshot for audit {}", entity.id)

      val affectedRows = jdbc.update(
         """
         INSERT INTO audit_inventory
         (audit_id, dataset, serial_number, lookup_key, lookup_key_type, barcode, alt_id, brand, model_number, product_code, description, received_date, original_cost, actual_cost, model_category, times_rented, total_revenue, remaining_value, sell_price, assigned_value, idle_days, condition, returned_date, location, status, primary_location, location_type)
         SELECT :audit_id, dataset, serial_number, lookup_key, lookup_key_type, barcode, alternate_id, brand, model_number, product_code, description, received_date, original_cost, actual_cost, model_category, times_rented, total_revenue, remaining_value, sell_price, assigned_value, idle_days, condition, returned_date, location, status, primary_location, location_type
         FROM inventory i
            JOIN company comp ON i.dataset = comp.dataset_code AND comp.deleted = FALSE
         WHERE i.primary_location = :store_number
               AND comp.id = :company_id
               AND (
                  (i.status = 'D' OR (i.status IN ('N','R') AND i.location = :store_number))
                  OR i.lookup_key IN (SELECT lookup_key FROM audit_detail WHERE audit_id = :audit_id)
                  )
         """.trimIndent(),
         mapOf(
            "audit_id" to entity.id,
            "store_number" to entity.store.myNumber(),
            "company_id" to entity.store.myCompany().id
         )
      )

      logger.info("Inserted rows {}", affectedRows)
   }
}
