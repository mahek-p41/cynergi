package com.cynergisuite.domain.infrastructure

import groovy.sql.Sql
import groovy.transform.CompileStatic
import io.micronaut.context.annotation.Requires
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import jakarta.inject.Inject
import jakarta.inject.Singleton
import javax.transaction.Transactional

@Singleton
@CompileStatic
@Requires(env = ["test", "load"])
class TruncateDatabaseService {

   private static final Logger logger = LoggerFactory.getLogger("TruncateDatabaseService")
   private final Sql sql

   @Inject
   TruncateDatabaseService(Sql sql) {
      this.sql = sql
   }

   @Transactional
   void truncate() {
      final List<String> tables = new ArrayList<>()

      logger.debug("Querying for tables to cleanup")
      sql.eachRow("""
          SELECT table_name AS tableName
          FROM information_schema.tables
          WHERE table_schema='public'
                AND table_type='BASE TABLE'
                AND table_name <> 'flyway_schema_history'
                AND table_name <> 'inventory'
                AND table_name NOT LIKE '%_type_domain'"""
      ) {rs ->
         final String table = rs.getString("tableName")

         tables.add("TRUNCATE TABLE $table CASCADE".toString())
      }

      if (tables.size() > 0) {
         sql.withBatch(tables.size()) {statement ->
            tables.forEach {statement.addBatch(it) }
         }
      }
   }

   @Transactional
   void loadInventory() {
      logger.debug("Copying inventory_vw data to inventory")
      sql.execute("""
          INSERT INTO inventory (dataset, serial_number, lookup_key, lookup_key_type, barcode, alternate_id, brand,model_number, product_code, description, received_date, original_cost, actual_cost, model_category, times_rented,total_revenue, remaining_value, sell_price, assigned_value, idle_days, condition, invoice_number, inv_invoice_expensed_date, inv_purchase_order_number, returned_date, location, status, primary_location, location_type, status_id, model_id, store_id, received_location, invoice_id, inventory_changed_sw, changes_sent_to_current_state_sw)
           SELECT dataset, serial_number, lookup_key, lookup_key_type, barcode, COALESCE(alt_id, '123') AS alternate_id, COALESCE(brand, 'default_brand') AS brand, model_number, product_code, description, received_date, original_cost, actual_cost, model_category, times_rented, total_revenue, remaining_value, sell_price, assigned_value, idle_days, condition, invoice_number, inv_invoice_expensed_date, inv_purchase_order_number, returned_date, location, status, primary_location, location_type, 1 AS status_id, null AS model_id, 1 AS store_id, 1 AS received_location, null AS invoice_id, false AS inventory_changed_sw, false AS changes_sent_to_current_state_sw
            FROM fastinfo_prod_import.inventory_vw"""
      )
   }
}

