package com.cynergisuite.middleware.audit.inventory.infrastructure

import com.cynergisuite.extensions.update
import com.cynergisuite.middleware.audit.AuditEntity
import jakarta.inject.Inject
import jakarta.inject.Singleton
import org.jdbi.v3.core.Jdbi
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.transaction.Transactional

@Singleton
class AuditInventoryRepository @Inject constructor(
   private val jdbc: Jdbi
) {
   private val logger: Logger = LoggerFactory.getLogger(AuditInventoryRepository::class.java)

   @Transactional
   fun createInventorySnapshot(entity: AuditEntity) {
      // Inventory snapshot creates snapshot for all inventory items in statuses ('N', 'R', 'D')
      logger.debug("Create inventory snapshot for audit {}", entity.id)

      val affectedRows = jdbc.update(
         """
         INSERT INTO audit_inventory
         (audit_id, dataset, serial_number, lookup_key, lookup_key_type, barcode, alt_id, brand, model_number, product_code, description, received_date, original_cost, actual_cost, model_category, times_rented, total_revenue, remaining_value, sell_price, assigned_value, idle_days, condition, returned_date, location, status, primary_location, location_type)
         SELECT :audit_id, dataset, serial_number, lookup_key, lookup_key_type, barcode, alt_id, brand, model_number, product_code, description, received_date, original_cost, actual_cost, model_category, times_rented, total_revenue, remaining_value, sell_price, assigned_value, idle_days, condition, returned_date, location, status, primary_location, location_type
         FROM fastinfo_prod_import.inventory_vw i
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
