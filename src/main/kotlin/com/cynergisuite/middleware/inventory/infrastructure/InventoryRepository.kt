package com.cynergisuite.middleware.inventory.infrastructure

import com.cynergisuite.domain.InventoryInquiryFilterRequest
import com.cynergisuite.domain.PageRequest
import com.cynergisuite.domain.StandardPageRequest
import com.cynergisuite.domain.infrastructure.RepositoryPage
import com.cynergisuite.extensions.findFirstOrNull
import com.cynergisuite.extensions.getBigDecimalOrNull
import com.cynergisuite.extensions.getLocalDateOrNull
import com.cynergisuite.extensions.getUuid
import com.cynergisuite.extensions.query
import com.cynergisuite.extensions.queryForObject
import com.cynergisuite.extensions.queryPaged
import com.cynergisuite.extensions.update
import com.cynergisuite.middleware.address.AddressEntity
import com.cynergisuite.middleware.address.AddressRepository
import com.cynergisuite.middleware.audit.AuditEntity
import com.cynergisuite.middleware.audit.exception.AuditExceptionEntity
import com.cynergisuite.middleware.audit.exception.infrastructure.AuditExceptionRepository
import com.cynergisuite.middleware.audit.exception.note.AuditExceptionNote
import com.cynergisuite.middleware.audit.status.Created
import com.cynergisuite.middleware.audit.status.InProgress
import com.cynergisuite.middleware.authentication.user.infrastructure.SecurityGroupRepository
import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.company.infrastructure.CompanyRepository
import com.cynergisuite.middleware.employee.EmployeeEntity
import com.cynergisuite.middleware.inventory.InventoryEntity
import com.cynergisuite.middleware.inventory.InventoryInquiryDTO
import com.cynergisuite.middleware.inventory.location.InventoryLocationType
import com.cynergisuite.middleware.location.infrastructure.LocationRepository
import com.cynergisuite.middleware.store.Store
import com.cynergisuite.middleware.store.infrastructure.StoreRepository
import io.micronaut.transaction.annotation.ReadOnly
import jakarta.inject.Singleton
import org.jdbi.v3.core.Jdbi
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.sql.ResultSet
import java.util.UUID
import java.util.UUID
import javax.transaction.Transactional

@Singleton
class InventoryRepository(
   private val addressRepository: AddressRepository,
   private val auditExceptionRepository: AuditExceptionRepository,
   private val companyRepository: CompanyRepository,
   private val jdbc: Jdbi,
   private val locationRepository: LocationRepository,
   private val securityGroupRepository: SecurityGroupRepository,
   private val storeRepository: StoreRepository
) {
   private val logger: Logger = LoggerFactory.getLogger(InventoryRepository::class.java)

   private val selectBase =
      """
      SELECT
         i.id                          AS id,
         i.serial_number               AS serial_number,
         i.lookup_key                  AS lookup_key,
         i.lookup_key_type             AS lookup_key_type,
         i.barcode                     AS barcode,
         i.alternate_id                AS alternate_id,
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
         comp.time_created             AS comp_time_created,
         comp.time_updated             AS comp_time_updated,
         comp.name                     AS comp_name,
         comp.doing_business_as        AS comp_doing_business_as,
         comp.client_code              AS comp_client_code,
         comp.client_id                AS comp_client_id,
         comp.dataset_code             AS comp_dataset_code,
         comp.federal_id_number        AS comp_federal_id_number,
         comp.include_demo_inventory   AS comp_include_demo_inventory,
         compAddress.id                AS comp_address_id,
         compAddress.name              AS comp_address_name,
         compAddress.address1          AS comp_address_address1,
         compAddress.address2          AS comp_address_address2,
         compAddress.city              AS comp_address_city,
         compAddress.state             AS comp_address_state,
         compAddress.postal_code       AS comp_address_postal_code,
         compAddress.latitude          AS comp_address_latitude,
         compAddress.longitude         AS comp_address_longitude,
         compAddress.country           AS comp_address_country,
         compAddress.county            AS comp_address_county,
         compAddress.phone             AS comp_address_phone,
         compAddress.fax               AS comp_address_fax,
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
           JOIN inventory i ON comp.dataset_code = i.dataset
           LEFT JOIN address AS compAddress ON comp.address_id = compAddress.id AND compAddress.deleted = FALSE
           JOIN system_stores_fimvw primaryStore ON comp.dataset_code = primaryStore.dataset AND i.primary_location = primaryStore.number
           LEFT OUTER JOIN system_stores_fimvw currentStore ON comp.dataset_code = currentStore.dataset AND i.location = currentStore.number
           JOIN inventory_location_type_domain iltd ON i.location_type = iltd.id
      """.trimIndent()

   private val selectFromAuditInventory =
      """
      SELECT
         i.id                     AS id,
         i.audit_id               AS audit_id,
         i.serial_number          AS serial_number,
         i.lookup_key             AS lookup_key,
         i.lookup_key_type        AS lookup_key_type,
         i.barcode                AS barcode,
         i.alternate_id           AS alternate_id,
         i.brand                  AS brand,
         i.model_number           AS model_number,
         i.product_code           AS product_code,
         i.description            AS description,
         i.received_date          AS received_date,
         i.original_cost          AS original_cost,
         i.actual_cost            AS actual_cost,
         i.model_category         AS model_category,
         i.times_rented           AS times_rented,
         i.total_revenue          AS total_revenue,
         i.remaining_value         AS remaining_value,
         i.sell_price             AS sell_price,
         i.assigned_value         AS assigned_value,
         i.idle_days              AS idle_days,
         i.condition              AS condition,
         i.returned_date          AS returned_date,
         i.status                 AS status,
         i.dataset                AS dataset,
         comp.id                  AS comp_id,
         comp.time_created        AS comp_time_created,
         comp.time_updated        AS comp_time_updated,
         comp.name                AS comp_name,
         comp.doing_business_as   AS comp_doing_business_as,
         comp.client_code         AS comp_client_code,
         comp.client_id           AS comp_client_id,
         comp.dataset_code        AS comp_dataset_code,
         comp.federal_id_number   AS comp_federal_id_number,
         comp.include_demo_inventory AS comp_include_demo_inventory,
         compAddress.id           AS comp_address_id,
         compAddress.name         AS comp_address_name,
         compAddress.address1     AS comp_address_address1,
         compAddress.address2     AS comp_address_address2,
         compAddress.city         AS comp_address_city,
         compAddress.state        AS comp_address_state,
         compAddress.postal_code  AS comp_address_postal_code,
         compAddress.latitude     AS comp_address_latitude,
         compAddress.longitude    AS comp_address_longitude,
         compAddress.country      AS comp_address_country,
         compAddress.county       AS comp_address_county,
         compAddress.phone        AS comp_address_phone,
         compAddress.fax          AS comp_address_fax,
         primaryStore.id          AS primary_store_id,
         primaryStore.number      AS primary_store_number,
         primaryStore.name        AS primary_store_name,
         primaryStore.dataset     AS primary_store_dataset,
         currentStore.id          AS current_store_id,
         currentStore.number      AS current_store_number,
         currentStore.name        AS current_store_name,
         currentStore.dataset     AS current_store_dataset,
         iltd.id                  AS location_type_id,
         iltd.value               AS location_type_value,
         iltd.description         AS location_type_description,
         iltd.localization_code   AS location_type_localization_code,
         count(*) OVER() as total_elements
      FROM company comp
           JOIN audit_inventory i ON comp.dataset_code = i.dataset
           LEFT JOIN address AS compAddress ON comp.address_id = compAddress.id AND compAddress.deleted = FALSE
           JOIN system_stores_fimvw primaryStore ON comp.dataset_code = primaryStore.dataset AND i.primary_location = primaryStore.number
           LEFT OUTER JOIN system_stores_fimvw currentStore ON comp.dataset_code = currentStore.dataset AND i.location = currentStore.number
           JOIN inventory_location_type_domain iltd ON i.location_type = iltd.id
      """.trimIndent()

   @ReadOnly
   fun findOne(id: UUID, company: CompanyEntity): InventoryEntity? {
      logger.debug("Finding Inventory by ID with {}", id)

      val inventory = jdbc.findFirstOrNull(
         """
         $selectBase
         WHERE comp.id = :comp_id
               AND i.id = :id
         """.trimIndent(),
         mapOf(
            "comp_id" to company.id,
            "id" to id
         )
      ) { rs, _ -> mapRow(rs) }

      logger.debug("Search for Inventory by ID {} produced {}", id, inventory)

      return inventory
   }

   @ReadOnly
   fun exists(id: UUID, company: CompanyEntity): Boolean {
      val exists = jdbc.queryForObject(
         """
         SELECT count(i.id) > 0
         FROM company comp
              JOIN inventory i ON comp.dataset_code = i.dataset
         WHERE i.id = :i_id AND comp.id = :comp_id
         """.trimIndent(),
         mapOf("i_id" to id, "comp_id" to company.id),
         Boolean::class.java
      )

      logger.trace("Checking if Inventory: {} exists resulted in {}", id, exists)

      return exists
   }

   fun doesNotExist(id: UUID, company: CompanyEntity): Boolean =
      !exists(id, company)

   @ReadOnly
   fun findByLookupKey(lookupKey: String, company: CompanyEntity): InventoryEntity? {
      logger.debug("Finding Inventory by lookup key with {}", lookupKey)

      val inventory = jdbc.findFirstOrNull(
         """
         $selectBase
         WHERE i.lookup_key = :lookup_key
               AND comp.id = :comp_id
         """.trimIndent(),
         mapOf(
            "lookup_key" to lookupKey,
            "comp_id" to company.id
         )
      ) { rs, _ -> mapRow(rs) }

      logger.debug("Search for available Inventory by lookup key {} produced {}", lookupKey, inventory)

      return inventory
   }

   @ReadOnly
   fun findAll(
      pageRequest: InventoryPageRequest,
      company: CompanyEntity
   ): RepositoryPage<InventoryEntity, InventoryPageRequest> {
      var totalElements: Long? = null
      val elements = mutableListOf<InventoryEntity>()
      val statuses: List<String> = pageRequest.inventoryStatus?.toList() ?: emptyList()
      val params = mutableMapOf<String, Any?>(
         "location" to pageRequest.storeNumber!!,
         "comp_id" to company.id!!,
         "limit" to pageRequest.size(),
         "offset" to pageRequest.offset()
      )

      if (!pageRequest.locationType.isNullOrBlank()) {
         params["location_type"] = pageRequest.locationType!!
      }

      val statusFilterString = statuses.takeIf { it.isNotEmpty() }
         ?.let { buildStatusAndLocationTypeFilterString(it, params) }
         .orEmpty()

      val sql =
         """
      WITH paged AS (
         $selectBase
         WHERE i.primary_location = :location
               AND comp.id = :comp_id
               AND $statusFilterString
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

      jdbc.query(sql, params) { rs, _ ->
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

   @ReadOnly
   fun findUnscannedIdleInventory(audit: AuditEntity): List<InventoryEntity> {
      var pageResult = findUnscannedIdleInventory(
         audit,
         StandardPageRequest(page = 1, size = 1000, sortBy = "id", sortDirection = "ASC")
      )
      val inventories: MutableList<InventoryEntity> = mutableListOf()

      while (pageResult.elements.isNotEmpty()) {
         inventories.addAll(pageResult.elements)
         pageResult = findUnscannedIdleInventory(audit, pageResult.requested.nextPage())
      }

      return inventories
   }

   @ReadOnly
   fun findUnscannedIdleInventory(
      audit: AuditEntity,
      pageRequest: PageRequest
   ): RepositoryPage<InventoryEntity, PageRequest> {
      var totalElements: Long? = null
      val company = audit.store.myCompany()
      val elements = mutableListOf<InventoryEntity>()
      val params = mutableMapOf(
         "audit_id" to audit.id,
         "comp_id" to company.id,
         "limit" to pageRequest.size(),
         "offset" to pageRequest.offset()
      )

      val sql =
         """
      WITH paged AS (
         ${
            if (audit.currentStatus() == Created || audit.currentStatus() == InProgress) {
               "$selectBase JOIN audit a ON (a.company_id = comp.id AND a.store_number = i.primary_location)"
            } else {
               "$selectFromAuditInventory JOIN audit a ON (a.company_id = comp.id AND a.store_number = i.primary_location AND a.id = i.audit_id)"
            }
         }
         WHERE
            comp.id = :comp_id
            AND a.id = :audit_id
            AND i.status in ('N', 'R', 'D')
            AND i.lookup_key NOT IN (SELECT lookup_key
                                        FROM audit_detail
                                        WHERE audit_id = :audit_id)
            AND (comp.include_demo_inventory OR i.status != 'D')
      )
      SELECT
         p.*,
         auditException.id                                          AS auditException_id,
         auditException.exception_code                              AS auditException_exception_code,
         scannedBy.emp_id                                           AS auditExceptionEmployee_id,
         scannedBy.emp_type                                         AS auditExceptionEmployee_type,
         scannedBy.emp_number                                       AS auditExceptionEmployee_number,
         scannedBy.emp_last_name                                    AS auditExceptionEmployee_last_name,
         scannedBy.emp_first_name_mi                                AS auditExceptionEmployee_first_name_mi,
         scannedBy.emp_pass_code                                    AS auditExceptionEmployee_pass_code,
         scannedBy.emp_active                                       AS auditExceptionEmployee_active,
         scannedBy.emp_cynergi_system_admin                         AS auditExceptionEmployee_cynergi_system_admin,
         scannedBy.emp_alternative_store_indicator                  AS auditExceptionEmployee_alternative_store_indicator,
         scannedBy.emp_alternative_area                             AS auditExceptionEmployee_alternative_area,
         scannedBy.dept_id                                          AS auditExceptionEmployee_dept_id,
         scannedBy.dept_code                                        AS auditExceptionEmployee_dept_code,
         scannedBy.dept_description                                 AS auditExceptionEmployee_dept_description,
         scannedBy.store_id                                         AS auditExceptionEmployee_store_id,
         scannedBy.store_number                                     AS auditExceptionEmployee_store_number,
         scannedBy.store_name                                       AS auditExceptionEmployee_store_name,
         auditExceptionNote.id                                      AS auditExceptionNote_id,
         auditExceptionNote.time_created                            AS auditExceptionNote_time_created,
         auditExceptionNote.time_updated                            AS auditExceptionNote_time_updated,
         auditExceptionNote.note                                    AS auditExceptionNote_note,
         auditExceptionNoteEmployee.emp_id                          AS auditExceptionNoteEmployee_id,
         auditExceptionNoteEmployee.emp_type                        AS auditExceptionNoteEmployee_type,
         auditExceptionNoteEmployee.emp_number                      AS auditExceptionNoteEmployee_number,
         auditExceptionNoteEmployee.emp_last_name                   AS auditExceptionNoteEmployee_last_name,
         auditExceptionNoteEmployee.emp_first_name_mi               AS auditExceptionNoteEmployee_first_name_mi,
         auditExceptionNoteEmployee.emp_pass_code                   AS auditExceptionNoteEmployee_pass_code,
         auditExceptionNoteEmployee.emp_active                      AS auditExceptionNoteEmployee_active,
         auditExceptionNoteEmployee.emp_cynergi_system_admin        AS auditExceptionNoteEmployee_cynergi_system_admin,
         auditExceptionNoteEmployee.emp_alternative_store_indicator AS auditExceptionNoteEmployee_alternative_store_indicator,
         auditExceptionNoteEmployee.emp_alternative_area            AS auditExceptionNoteEmployee_alternative_area,
         auditExceptionNoteEmployee.dept_id                         AS auditExceptionNoteEmployee_dept_id,
         auditExceptionNoteEmployee.dept_code                       AS auditExceptionNoteEmployee_dept_code,
         auditExceptionNoteEmployee.dept_description                AS auditExceptionNoteEmployee_dept_description,
         auditExceptionNoteEmployee.store_id                        AS auditExceptionNoteEmployee_store_id,
         auditExceptionNoteEmployee.store_number                    AS auditExceptionNoteEmployee_store_number,
         auditExceptionNoteEmployee.store_name                      AS auditExceptionNoteEmployee_store_name
      FROM paged AS p
      LEFT OUTER JOIN audit_exception auditException on (auditException.audit_id = p.audit_id and auditException.lookup_key = p.lookup_key)
      LEFT OUTER JOIN system_employees_fimvw scannedBy ON auditException.scanned_by = scannedBy.emp_number AND p.comp_id = scannedBy.comp_id
      LEFT OUTER JOIN audit_exception_note auditExceptionNote on auditExceptionNote.audit_exception_id = auditException.id
      LEFT OUTER JOIN system_employees_fimvw auditExceptionNoteEmployee ON auditExceptionNote.entered_by = auditExceptionNoteEmployee.emp_number AND p.comp_id = auditExceptionNoteEmployee.comp_id
      LEFT OUTER JOIN audit_scan_area AS auditScanArea ON auditException.scan_area_id = auditScanArea.id
      WHERE auditException.scan_area_id is null
      ORDER BY CASE WHEN status = 'D' THEN 1 ELSE 0 END,${pageRequest.sortBy()} ${pageRequest.sortDirection()}
      LIMIT :limit
         OFFSET :offset
         """.trimIndent()

      logger.debug("find unscanned idle inventory {}/{}", sql, params)

      return jdbc.queryPaged(sql, params, pageRequest) { rs, elements ->
         var currentId: Long? = null
         var currentParentEntity: InventoryEntity? = null
         do {
            val tempId = rs.getLong("id")
            val address = addressRepository.mapAddressOrNull(rs, "comp_address_")
            val tempParentEntity: InventoryEntity = if (tempId != currentId) {
               currentId = tempId
               currentParentEntity = mapRowWithException(rs, address)
               elements.add(currentParentEntity)
               currentParentEntity
            } else {
               currentParentEntity!!
            }

            if (tempParentEntity.exception != null) {
               val noteBy = auditExceptionRepository.mapEmployee(rs, address, "auditExceptionNoteEmployee_")
               if (noteBy != null) {
                  auditExceptionRepository.mapRowAuditExceptionNote(rs, noteBy)?.also { tempParentEntity.exception.notes?.add(it) }
               }
            }

            if (totalElements == null) {
               totalElements = rs.getLong("total_elements")
            }
         } while (rs.next())
      }
   }

   @ReadOnly
   fun fetchInquiry(company: CompanyEntity, filterRequest: InventoryInquiryFilterRequest): RepositoryPage<InventoryInquiryDTO, PageRequest> {
      val params = mutableMapOf<String, Any?>("comp_id" to company.id, "limit" to filterRequest.size(), "offset" to filterRequest.offset())
      val whereClause = StringBuilder("WHERE comp.id = :comp_id ")
      val sortBy = StringBuilder("ORDER BY ")

      if (filterRequest.recvLoc != null) {
         params["recvLoc"] = filterRequest.recvLoc
         whereClause.append(" AND inv.primary_location = :recvLoc ")
      }

      if (filterRequest.serialNbr != null) {
         params["serialNbr"] = filterRequest.serialNbr
         whereClause.append(" AND UPPER(inv.serial_number) LIKE \'%${filterRequest.serialNbr!!.trim().uppercase()}%\'")
      }

      if (filterRequest.modelNbr != null) {
         params["modelNbr"] = filterRequest.modelNbr
         whereClause.append(" AND UPPER(inv.model_number) LIKE \'%${filterRequest.modelNbr!!.trim().uppercase()}%\'")
      }

      if (filterRequest.poNbr != null) {
         params["poNbr"] = filterRequest.poNbr!!.uppercase()
         whereClause.append(" AND UPPER(inv.inv_purchase_order_number) LIKE :poNbr ")
      }

      if (filterRequest.invoiceNbr != null) {
         params["invoiceNbr"] = filterRequest.invoiceNbr
         whereClause.append(" AND UPPER(inv.invoice_number) LIKE \'%${filterRequest.invoiceNbr!!.trim().uppercase()}%\'")
      }

      if (filterRequest.receivedDate != null) {
         params["receivedDate"] = filterRequest.receivedDate
         whereClause.append(" AND inv.received_date = :receivedDate ")
      }

      if (filterRequest.beginAltId != null && filterRequest.endAltId != null) {
         params["beginAltId"] = filterRequest.beginAltId!!.uppercase()
         params["endAltId"] = filterRequest.endAltId!!.uppercase()
         whereClause.append(" AND UPPER(inv.alternate_id) BETWEEN :beginAltId AND :endAltId ")
      }

      if (filterRequest.receivedDate != null) {
         sortBy.append("inv.received_date, inv.inv_purchase_order_number, inv.serial_number")
      } else if (filterRequest.poNbr != null) {
         sortBy.append("inv.inv_purchase_order_number, inv.received_date, inv.invoice_number, inv.model_number, inv.serial_number")
      } else if (filterRequest.invoiceNbr != null) {
         sortBy.append("inv.invoice_number, inv.model_number, inv.serial_number")
      } else if (filterRequest.modelNbr != null) {
         sortBy.append("inv.model_number, inv.serial_number")
      } else if (filterRequest.recvLoc != null) {
         sortBy.append("inv.primary_location, inv.serial_number")
      } else if (filterRequest.serialNbr != null) {
         sortBy.append("inv.serial_number")
      } else if (filterRequest.beginAltId != null && filterRequest.endAltId != null) {
         sortBy.append("inv.alternate_id")
      } else {
         sortBy.append("inv.inv_purchase_order_number, inv.invoice_number, inv.model_number, inv.serial_number")
      }

      return jdbc.queryPaged(
         """
            SELECT
               inv.model_number                 AS model_number,
               inv.serial_number                AS serial_number,
               inv.actual_cost                  AS landed_cost,
               inv.status                       AS status,
               inv.received_date                AS received_date,
               inv.inv_purchase_order_number    AS purchase_order_number,
               inv.invoice_number               AS invoice_number,
               inv.description                  AS description,
               inv.location                     AS current_location,
               inv.inv_invoice_expensed_date    AS invoice_expensed_date,
               inv.alternate_id                 AS alternate_id,
               count(*) OVER() AS total_elements
            FROM inventory inv
               JOIN company comp ON inv.dataset = comp.dataset_code AND comp.deleted = FALSE
            $whereClause
            $sortBy
            LIMIT :limit
            OFFSET :offset
         """.trimIndent(),
         params,
         filterRequest
      ) { rs, elements ->
         do {
            elements.add(mapInquiry(rs))
         } while (rs.next())
      }
   }

   @Transactional
   fun loadInventoryTable() {
      logger.debug("Checking if the inventory table is empty")

      val isInventoryEmpty = jdbc.queryForObject("""
         SELECT NOT EXISTS (SELECT id FROM inventory LIMIT 1)
      """.trimIndent(), emptyMap<String, Any>(), Boolean::class.java)

      if (isInventoryEmpty) {
         logger.debug("--> Inserting data for empty inventory table")
         jdbc.update("""
            INSERT INTO inventory (dataset, serial_number, lookup_key, lookup_key_type, barcode, alternate_id, brand, model_number, product_code, description, received_date, original_cost, actual_cost, model_category, times_rented, total_revenue, remaining_value, sell_price, assigned_value, idle_days, condition, invoice_number, inv_invoice_expensed_date, inv_purchase_order_number, returned_date, location, status, primary_location, location_type, status_id, model_id, store_id, received_location, invoice_id, inventory_changed_sw, changes_sent_to_current_state_sw)
            SELECT dataset, serial_number, lookup_key, lookup_key_type, barcode, COALESCE(alt_id, '123') AS alternate_id, COALESCE(brand, 'default_brand') AS brand, model_number, product_code, description, received_date, original_cost, actual_cost, model_category, times_rented, total_revenue, remaining_value, sell_price, assigned_value, idle_days, condition, invoice_number, inv_invoice_expensed_date, inv_purchase_order_number, returned_date, location, status, primary_location, location_type, 1 AS status_id, null AS model_id, 1 AS store_id, 1 AS received_location, null AS invoice_id, false AS inventory_changed_sw, false AS changes_sent_to_current_state_sw
            FROM fastinfo_prod_import.inventory_vw
         """, emptyMap<String, Any>())
      } else {
         logger.debug("--> Inventory table already has data")
      }
   }

   fun mapRow(rs: ResultSet): InventoryEntity {
      val company = companyRepository.mapRow(rs, columnPrefix = "comp_", addressPrefix = "comp_address_")

      return InventoryEntity(
         id = rs.getUuid("id"),
         serialNumber = rs.getString("serial_number"),
         lookupKey = rs.getString("lookup_key"),
         lookupKeyType = rs.getString("lookup_key_type"),
         barcode = rs.getString("barcode"),
         altId = rs.getString("alternate_id"),
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
         location = locationRepository.maybeMapRow(rs, "current_store_"),
         status = rs.getString("status"),
         primaryLocation = storeRepository.mapRow(rs, company, "primary_store_"),
         locationType = InventoryLocationType(
            id = rs.getInt("location_type_id"),
            value = rs.getString("location_type_value"),
            description = rs.getString("location_type_description"),
            localizationCode = rs.getString("location_type_localization_code")
         )
      )
   }

   fun mapRowWithException(rs: ResultSet, address: AddressEntity?): InventoryEntity {
      val company = companyRepository.mapRow(rs, columnPrefix = "comp_", addressPrefix = "comp_address_")
      val exceptionBy = auditExceptionRepository.mapEmployee(rs, address, "auditExceptionEmployee_")
      var auditException: AuditExceptionEntity? = null
      if (exceptionBy != null) {
         auditException = mapRowAuditException(rs, exceptionBy)
      }

      return InventoryEntity(
         id = rs.getUuid("id"),
         serialNumber = rs.getString("serial_number"),
         lookupKey = rs.getString("lookup_key"),
         lookupKeyType = rs.getString("lookup_key_type"),
         barcode = rs.getString("barcode"),
         altId = rs.getString("alternate_id"),
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
         location = locationRepository.maybeMapRow(rs, "current_store_"),
         status = rs.getString("status"),
         primaryLocation = storeRepository.mapRow(rs, company, "primary_store_"),
         locationType = InventoryLocationType(
            id = rs.getInt("location_type_id"),
            value = rs.getString("location_type_value"),
            description = rs.getString("location_type_description"),
            localizationCode = rs.getString("location_type_localization_code")
         ),
         exception = auditException
      )
   }

   fun mapRowWithException(rs: ResultSet, address: AddressEntity?): InventoryEntity {
      val company = companyRepository.mapRow(rs, columnPrefix = "comp_", addressPrefix = "comp_address_")
      val exceptionBy = auditExceptionRepository.mapEmployee(rs, address, "auditExceptionEmployee_")
      var auditException: AuditExceptionEntity? = null
      if (exceptionBy != null) {
         auditException = mapRowAuditException(rs, exceptionBy)
      }

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
         location = locationRepository.maybeMapRow(rs, "current_store_"),
         status = rs.getString("status"),
         primaryLocation = storeRepository.mapRow(rs, company, "primary_store_"),
         locationType = InventoryLocationType(
            id = rs.getInt("location_type_id"),
            value = rs.getString("location_type_value"),
            description = rs.getString("location_type_description"),
            localizationCode = rs.getString("location_type_localization_code")
         ),
         exception = auditException
      )
   }

   private fun buildStatusAndLocationTypeFilterString(statuses: List<String>, params: MutableMap<String, Any?>): String {
      //location_type filter is applied to status (N,R) only
      val str = StringBuilder(" ( ")
      var isFirstCondition = true
      if (statuses.contains("N")) {
         str.append(" (i.status = 'N' AND i.location = :location) ${if (params.containsKey("location_type")) "AND iltd.value = :location_type" else ""} ")
         isFirstCondition = false
      }
      if (statuses.contains("R")) {
         str.append(" ${if(!isFirstCondition) "OR" else ""} (i.status = 'R' AND i.location = :location) ${if (params.containsKey("location_type")) "AND iltd.value = :location_type" else ""} ")
         isFirstCondition = false
      }
      if (statuses.contains("D")) {
         str.append(" ${if(!isFirstCondition) "OR" else ""} i.status = 'D' ")
         isFirstCondition = false
      }
      if (statuses.contains("O")) {
         str.append(" ${if(!isFirstCondition) "OR" else ""} i.status = 'O' ")
      }
      str.append(" ) ")
      return str.toString()
   }

   fun mapInquiry(rs: ResultSet): InventoryInquiryDTO {
      val currentLoc = rs.getInt("current_location")
      val invoiceExpensedDate = rs.getLocalDateOrNull("invoice_expensed_date")
      val currentLocExpensed =
         if (invoiceExpensedDate != null) {
            "$currentLoc Expensed"
         } else currentLoc.toString()
      return InventoryInquiryDTO(
         modelNumber = rs.getString("model_number"),
         serialNumber = rs.getString("serial_number"),
         landedCost = rs.getBigDecimalOrNull("landed_cost"),
         status = rs.getString("status"),
         receivedDate = rs.getLocalDateOrNull("received_date"),
         poNbr = rs.getString("purchase_order_number"),
         invoiceNbr = rs.getString("invoice_number"),
         description = rs.getString("description"),
         currentLoc = currentLoc,
         invoiceExpensedDate = invoiceExpensedDate,
         altId = rs.getString("alternate_id"),
         currentLocExpensed = currentLocExpensed
      )
   }

   private fun mapRowAuditException(rs: ResultSet, scannedBy: EmployeeEntity): AuditExceptionEntity? =
      if (rs.getString("auditException_id") != null) {
         AuditExceptionEntity(
            audit = rs.getUuid("audit_id"),
            lookupKey = rs.getString("lookup_key"),
            scanArea = null,
            scannedBy = scannedBy,
            exceptionCode = rs.getString("auditException_exception_code")
         )
      } else {
         null
      }
}
