package com.cynergisuite.middleware.inventory.infrastructure

import com.cynergisuite.domain.infrastructure.RepositoryPage
import com.cynergisuite.extensions.getOffsetDateTime
import com.cynergisuite.middleware.inventory.Inventory
import org.intellij.lang.annotations.Language
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.sql.ResultSet
import javax.inject.Singleton

@Singleton
class InventoryRepository(
   private val jdbc: NamedParameterJdbcTemplate
) {
   private val logger: Logger = LoggerFactory.getLogger(InventoryRepository::class.java)

   @Language("PostgreSQL")
   private val selectBase = """
      SELECT
         i.id AS id,
         i.time_created AS time_created,
         i.time_updated AS time_updated,
         i.serial_number AS serial_number,
         i.barcode_number AS barcode_number,
         i.location AS location,
         i.status AS status,
         i.make_model_number AS make_model_number,
         i.model_category AS model_category,
         i.product_code AS product_code,
         i.description AS description
      FROM fastinfo_prod_import.inventory_vw i
   """.trimIndent()

   fun findAll(pageRequest: InventoryPageRequest): RepositoryPage<Inventory> {
      var totalElements: Long? = null
      val elements = mutableListOf<Inventory>()
      val statuses: List<String> = pageRequest.inventoryStatus ?: emptyList()
      val params = mutableMapOf<String, Any>("location" to pageRequest.storeNumber!!)

      logger.debug("Finding all Inventory with {} and {}", pageRequest, params)

      if (statuses.isNotEmpty()) {
         params["statuses"] = statuses
      }

      jdbc.query(
         """
         WITH paged AS (
            $selectBase
         )
         SELECT
            p.*,
            count(*) OVER() as total_elements
         FROM paged AS p
         WHERE location = :location ${ if (statuses.isNotEmpty()) "AND status IN (:statuses)" else "" }
         ORDER BY ${pageRequest.sortBy} ${pageRequest.sortDirection}
         LIMIT ${pageRequest.size}
            OFFSET ${pageRequest.offset()}
         """.trimIndent(),
         params
      ) { rs ->
         if (totalElements == null) {
            totalElements = rs.getLong("total_elements")
         }

         elements.add(mapRow(rs))
      }

      return RepositoryPage(
         elements = elements,
         totalElements = totalElements ?: 0
      )
   }

   fun mapRow(rs: ResultSet): Inventory =
      Inventory(
         id = rs.getLong("id"),
         timeCreated = rs.getOffsetDateTime("time_created"),
         timeUpdated = rs.getOffsetDateTime("time_updated"),
         serialNumber = rs.getString("serial_number"),
         barcodeNumber = rs.getString("barcode_number"),
         location = rs.getInt("location"),
         status = rs.getString("status"),
         makeModelNumber = rs.getString("make_model_number"),
         modelCategory = rs.getString("model_category"),
         productCode = rs.getString("product_code"),
         description = rs.getString("description")
      )
}

/*private class InventoryPageRequest(
   private val statuses: List<InventoryStatus>,
   private val location: Int,
   pageRequest: PageRequest
) : PageRequest by pageRequest {
   override fun filterWhere(): String = "location = :location AND status IN (:statuses)"
   override fun filterWhereParams(): Map<String, Any> = mapOf("location" to location, "statuses" to statuses)
}*/
