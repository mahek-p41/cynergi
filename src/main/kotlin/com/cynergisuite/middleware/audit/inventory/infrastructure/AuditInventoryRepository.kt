package com.cynergisuite.middleware.audit.inventory.infrastructure


import com.cynergisuite.middleware.audit.AuditEntity
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import javax.inject.Inject
import javax.inject.Singleton
import javax.transaction.Transactional

@Singleton
class AuditInventoryRepository @Inject constructor(
   private val jdbc: NamedParameterJdbcTemplate
) {
   private val logger: Logger = LoggerFactory.getLogger(AuditInventoryRepository::class.java)

   @Transactional
   fun createInventorySnapshot(entity: AuditEntity) {
      // Inventory snapshot creates snapshot for all inventory items in statuses ('N', 'R')
      logger.debug("Create inventory snapshot for audit {}", entity.id)

      val affectedRows = jdbc.update(
         """
         INSERT INTO audit_inventory
         (audit_id, dataset, serial_number, lookup_key, lookup_key_type, barcode, alt_id, brand, model_number, product_code, description, received_date, original_cost, actual_cost, model_category, times_rented, total_revenue, remaining_value, sell_price, assigned_value, idle_days, condition, returned_date, location, status, primary_location, location_type)
         SELECT :audit_id, dataset, serial_number, lookup_key, lookup_key_type, barcode, alt_id, brand, model_number, product_code, description, received_date, original_cost, actual_cost, model_category, times_rented, total_revenue, remaining_value, sell_price, assigned_value, idle_days, condition, returned_date, location, status, primary_location, location_type
         FROM fastinfo_prod_import.inventory_vw i
            JOIN company comp ON i.dataset = comp.dataset_code
         WHERE i.primary_location = :store_number
               AND i.location = :store_number
               AND (i.status IN ('N', 'R')
                     OR i.lookup_key IN (SELECT lookup_key FROM audit_detail WHERE audit_id = :audit_id))
               AND comp.id = :company_id
         """.trimIndent(),
         mapOf(
            "audit_id" to entity.id,
            "store_number" to entity.store.myNumber(),
            "company_id" to entity.store.myCompany().myId()
         )
      )

      logger.info("Inserted rows {}", affectedRows)
   }
}
