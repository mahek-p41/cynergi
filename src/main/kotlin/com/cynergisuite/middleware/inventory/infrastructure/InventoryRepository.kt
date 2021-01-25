package com.cynergisuite.middleware.inventory.infrastructure

import com.cynergisuite.domain.PageRequest
import com.cynergisuite.domain.StandardPageRequest
import com.cynergisuite.domain.infrastructure.DatasetRequiringRepository
import com.cynergisuite.domain.infrastructure.RepositoryPage
import com.cynergisuite.extensions.findFirstOrNull
import com.cynergisuite.extensions.getLocalDateOrNull
import com.cynergisuite.middleware.audit.AuditEntity
import com.cynergisuite.middleware.audit.status.CREATED
import com.cynergisuite.middleware.audit.status.IN_PROGRESS
import com.cynergisuite.middleware.company.Company
import com.cynergisuite.middleware.company.infrastructure.CompanyRepository
import com.cynergisuite.middleware.inventory.InventoryEntity
import com.cynergisuite.middleware.inventory.location.InventoryLocationType
import com.cynergisuite.middleware.location.infrastructure.LocationRepository
import com.cynergisuite.middleware.store.infrastructure.StoreRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.sql.ResultSet
import javax.inject.Singleton

@Singleton
class InventoryRepository(
   private val companyRepository: CompanyRepository,
   private val jdbc: NamedParameterJdbcTemplate,
   private val locationRepository: LocationRepository,
   private val storeRepository: StoreRepository
) : DatasetRequiringRepository {
   private val logger: Logger = LoggerFactory.getLogger(InventoryRepository::class.java)

   private val selectBase =
      """
      WITH company AS (
         ${companyRepository.companyBaseQuery()}
      )
      SELECT
         i.id                          AS id,
         i.serial_number               AS serial_number,
         i.lookup_key                  AS lookup_key,
         i.lookup_key_type             AS lookup_key_type,
         i.barcode                     AS barcode,
         i.alt_id                      AS alt_id,
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
         i.dataset                     AS dataset,
         comp.id                       AS comp_id,
         comp.uu_row_id                AS comp_uu_row_id,
         comp.time_created             AS comp_time_created,
         comp.time_updated             AS comp_time_updated,
         comp.name                     AS comp_name,
         comp.doing_business_as        AS comp_doing_business_as,
         comp.client_code              AS comp_client_code,
         comp.client_id                AS comp_client_id,
         comp.dataset_code             AS comp_dataset_code,
         comp.federal_id_number        AS comp_federal_id_number,
         comp.address_id               AS address_id,
         comp.address_name             AS address_name,
         comp.address_address1         AS address_address1,
         comp.address_address2         AS address_address2,
         comp.address_city             AS address_city,
         comp.address_state            AS address_state,
         comp.address_postal_code      AS address_postal_code,
         comp.address_latitude         AS address_latitude,
         comp.address_longitude        AS address_longitude,
         comp.address_country          AS address_country,
         comp.address_county           AS address_county,
         comp.address_phone            AS address_phone,
         comp.address_fax              AS address_fax,
         primaryStore.id               AS primary_store_id,
         primaryStore.number           AS primary_store_number,
         primaryStore.name             AS primary_store_name,
         primaryStore.dataset          AS primary_store_dataset,
         currentStore.id               AS current_store_id,
         currentStore.number           AS current_store_number,
         currentStore.name             AS current_store_name,
         currentStore.dataset          AS current_store_dataset,
         iltd.id                       AS location_type_id,
         iltd.value                    AS location_type_value,
         iltd.description              AS location_type_description,
         iltd.localization_code        AS location_type_localization_code
      FROM company comp
           JOIN fastinfo_prod_import.inventory_vw i ON comp.dataset_code = i.dataset
           LEFT JOIN address ON comp.address_id = address.id
           JOIN fastinfo_prod_import.store_vw primaryStore ON comp.dataset_code = primaryStore.dataset AND i.primary_location = primaryStore.number
           LEFT OUTER JOIN fastinfo_prod_import.store_vw currentStore ON comp.dataset_code = currentStore.dataset AND i.location = currentStore.number
           JOIN inventory_location_type_domain iltd ON i.location_type = iltd.id
      """.trimIndent()

   private val selectFromAuditInventory =
      """
      SELECT
         i.id AS id,
         i.serial_number AS serial_number,
         i.lookup_key AS lookup_key,
         i.lookup_key_type AS lookup_key_type,
         i.barcode AS barcode,
         i.alt_id AS alt_id,
         i.brand AS brand,
         i.model_number AS model_number,
         i.product_code AS product_code,
         i.description AS description,
         i.received_date AS received_date,
         i.original_cost AS original_cost,
         i.actual_cost AS actual_cost,
         i.model_category AS model_category,
         i.times_rented AS times_rented,
         i.total_revenue AS total_revenue,
         i.remaining_value AS remaining_value,
         i.sell_price AS sell_price,
         i.assigned_value AS assigned_value,
         i.idle_days AS idle_days,
         i.condition AS condition,
         i.returned_date AS returned_date,
         i.status AS status,
         i.dataset AS dataset,
         comp.id AS comp_id,
         comp.uu_row_id AS comp_uu_row_id,
         comp.time_created AS comp_time_created,
         comp.time_updated AS comp_time_updated,
         comp.name AS comp_name,
         comp.doing_business_as AS comp_doing_business_as,
         comp.client_code AS comp_client_code,
         comp.client_id AS comp_client_id,
         comp.dataset_code AS comp_dataset_code,
         comp.federal_id_number AS comp_federal_id_number,
         primaryStore.id AS primary_store_id,
         primaryStore.number AS primary_store_number,
         primaryStore.name AS primary_store_name,
         primaryStore.dataset AS primary_store_dataset,
         currentStore.id AS current_store_id,
         currentStore.number AS current_store_number,
         currentStore.name AS current_store_name,
         currentStore.dataset AS current_store_dataset,
         iltd.id AS location_type_id,
         iltd.value AS location_type_value,
         iltd.description AS location_type_description,
         iltd.localization_code AS location_type_localization_code
      FROM company comp
           JOIN audit_inventory i ON comp.dataset_code = i.dataset
           JOIN fastinfo_prod_import.store_vw primaryStore ON comp.dataset_code = primaryStore.dataset AND i.primary_location = primaryStore.number
           LEFT OUTER JOIN fastinfo_prod_import.store_vw currentStore ON comp.dataset_code = currentStore.dataset AND i.location = currentStore.number
           JOIN inventory_location_type_domain iltd ON i.location_type = iltd.id
      """.trimIndent()

   fun findOne(id: Long, company: Company): InventoryEntity? {
      logger.debug("Finding Inventory by ID with {}", id)

      val inventory = jdbc.findFirstOrNull(
         """
         $selectBase
         WHERE comp.id = :comp_id
               AND i.id = :id
         """.trimIndent(),
         mapOf(
            "comp_id" to company.myId(),
            "id" to id
         )
      ) { rs, _ -> mapRow(rs) }

      logger.debug("Search for Inventory by ID {} produced {}", id, inventory)

      return inventory
   }

   override fun exists(id: Long, company: Company): Boolean {
      val exists = jdbc.queryForObject(
         """
         SELECT count(i.id) > 0
         FROM company comp
              JOIN fastinfo_prod_import.inventory_vw i ON comp.dataset_code = i.dataset
         WHERE i.id = :i_id AND comp.id = :comp_id
         """.trimIndent(),
         mapOf("i_id" to id, "comp_id" to company.myId()), Boolean::class.java
      )!!

      logger.trace("Checking if Inventory: {} exists resulted in {}", id, exists)

      return exists
   }

   fun doesNotExist(id: Long, company: Company): Boolean =
      !exists(id, company)

   fun findByLookupKey(lookupKey: String, company: Company): InventoryEntity? {
      logger.debug("Finding Inventory by barcode with {}", lookupKey)

      val inventory = jdbc.findFirstOrNull(
         """
         $selectBase
         WHERE i.lookup_key = :lookup_key
               AND comp.id = :comp_id
         """.trimIndent(),
         mapOf(
            "lookup_key" to lookupKey,
            "comp_id" to company.myId()
         )
      ) { rs, _ -> mapRow(rs) }

      logger.debug("Search for Inventory by barcode {} produced {}", lookupKey, inventory)

      return inventory
   }

   fun findAll(pageRequest: InventoryPageRequest, company: Company): RepositoryPage<InventoryEntity, InventoryPageRequest> {
      var totalElements: Long? = null
      val elements = mutableListOf<InventoryEntity>()
      val statuses: List<String> = pageRequest.inventoryStatus?.toList() ?: emptyList()
      val params = mutableMapOf<String, Any?>(
         "location" to pageRequest.storeNumber!!,
         "comp_id" to company.myId()!!,
         "limit" to pageRequest.size(),
         "offset" to pageRequest.offset()
      )

      if (statuses.isNotEmpty()) {
         params["statuses"] = statuses
      }
      if (!pageRequest.locationType.isNullOrBlank()) {
         params["location_type"] = pageRequest.locationType!!
      }

      val sql =
         """
      WITH paged AS (
         $selectBase
         WHERE i.primary_location = :location
               AND i.location = :location
               AND comp.id = :comp_id
               ${if (params.containsKey("statuses")) "AND i.status IN (:statuses)" else ""}
               ${if (params.containsKey("location_type")) "AND iltd.value = :location_type" else ""}
      )
      SELECT
         p.*,
         count(*) OVER() as total_elements
      FROM paged AS p
      ORDER BY ${pageRequest.sortBy} ${pageRequest.sortDirection}
      LIMIT :limit
         OFFSET :offset
         """.trimIndent()

      logger.debug("Querying Inventory {} {} {}", pageRequest, params, sql)

      jdbc.query(sql, params) { rs ->
         if (totalElements == null) {
            totalElements = rs.getLong("total_elements")
         }

         elements.add(mapRow(rs))
      }

      return RepositoryPage(
         requested = pageRequest,
         elements = elements,
         totalElements = totalElements ?: 0
      )
   }

   fun findUnscannedIdleInventory(audit: AuditEntity): List<InventoryEntity> {
      var pageResult = findUnscannedIdleInventory(audit, StandardPageRequest(page = 1, size = 1000, sortBy = "id", sortDirection = "ASC"))
      val inventories: MutableList<InventoryEntity> = mutableListOf()

      while (pageResult.elements.isNotEmpty()) {
         inventories.addAll(pageResult.elements)
         pageResult = findUnscannedIdleInventory(audit, pageResult.requested.nextPage())
      }

      return inventories
   }

   fun findUnscannedIdleInventory(audit: AuditEntity, pageRequest: PageRequest): RepositoryPage<InventoryEntity, PageRequest> {
      var totalElements: Long? = null
      val company = audit.store.myCompany()
      val elements = mutableListOf<InventoryEntity>()
      val params = mutableMapOf(
         "audit_id" to audit.id,
         "comp_id" to company.myId(),
         "limit" to pageRequest.size(),
         "offset" to pageRequest.offset()
      )

      val sql =
      """
      WITH paged AS (
         ${ if (audit.currentStatus() == CREATED || audit.currentStatus() == IN_PROGRESS) selectBase
            else selectFromAuditInventory
         }
            JOIN audit a ON (a.company_id = comp.id AND a.store_number = i.location)
         WHERE
            comp.id = :comp_id
            AND a.id = :audit_id
            AND i.status in ('N', 'R')
            AND i.serial_number NOT IN (SELECT serial_number
                                        FROM audit_detail
                                        WHERE audit_id = :audit_id)
      )
      SELECT
         p.*,
         count(*) OVER() as total_elements
      FROM paged AS p
      ORDER BY ${pageRequest.sortBy()} ${pageRequest.sortDirection()}
      LIMIT :limit
         OFFSET :offset
      """.trimIndent()

      logger.debug("find unscanned idle inventory {}/{}", sql, params)

      jdbc.query(sql, params) { rs ->
         if (totalElements == null) {
            totalElements = rs.getLong("total_elements")
         }

         elements.add(mapRow(rs))
      }

      return RepositoryPage(
         requested = pageRequest,
         elements = elements,
         totalElements = totalElements ?: 0
      )
   }

   fun mapRow(rs: ResultSet): InventoryEntity {
      val company = companyRepository.mapRow(rs, "comp_")

      return InventoryEntity(
         id = rs.getLong("id"),
         serialNumber = rs.getString("serial_number"),
         lookupKey = rs.getString("lookup_key"),
         lookupKeyType = rs.getString("lookup_key_type"),
         barcode = rs.getString("barcode"),
         altId = rs.getString("alt_id"),
         brand = rs.getString("brand"),
         modelNumber = rs.getString("model_number"),
         productCode = rs.getString("product_code"),
         description = rs.getString("description"),
         receivedDate = rs.getLocalDateOrNull("received_date"),
         originalCost = rs.getBigDecimal("original_cost"),
         actualCost = rs.getBigDecimal("actual_cost"),
         modelCategory = rs.getString("model_category"),
         timesRented = rs.getInt("times_rented"),
         totalRevenue = rs.getBigDecimal("total_revenue"),
         remainingValue = rs.getBigDecimal("remaining_value"),
         sellPrice = rs.getBigDecimal("sell_price"),
         assignedValue = rs.getBigDecimal("assigned_value"),
         idleDays = rs.getInt("idle_days"),
         condition = rs.getString("condition"),
         returnedDate = rs.getLocalDateOrNull("returned_date"),
         location = locationRepository.maybeMapRow(rs, company, "current_store_"),
         status = rs.getString("status"),
         primaryLocation = storeRepository.mapRow(rs, company, "primary_store_"),
         locationType = InventoryLocationType(
            id = rs.getLong("location_type_id"),
            value = rs.getString("location_type_value"),
            description = rs.getString("location_type_description"),
            localizationCode = rs.getString("location_type_localization_code")
         )
      )
   }
}
