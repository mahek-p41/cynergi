package com.cynergisuite.middleware.audit.detail.infrastructure

import com.cynergisuite.domain.Identifiable
import com.cynergisuite.domain.PageRequest
import com.cynergisuite.domain.SimpleIdentifiableEntity
import com.cynergisuite.domain.infrastructure.RepositoryPage
import com.cynergisuite.extensions.findFirstOrNull
import com.cynergisuite.extensions.getOffsetDateTime
import com.cynergisuite.extensions.getUuid
import com.cynergisuite.extensions.insertReturning
import com.cynergisuite.extensions.query
import com.cynergisuite.extensions.queryForObject
import com.cynergisuite.extensions.updateReturning
import com.cynergisuite.middleware.audit.AuditEntity
import com.cynergisuite.middleware.audit.detail.AuditDetailEntity
import com.cynergisuite.middleware.audit.detail.scan.area.AuditScanAreaEntity
import com.cynergisuite.middleware.audit.detail.scan.area.infrastructure.AuditScanAreaRepository
import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.employee.EmployeeEntity
import com.cynergisuite.middleware.employee.infrastructure.EmployeeRepository
import com.cynergisuite.middleware.inventory.InventoryEntity
import io.micronaut.transaction.annotation.ReadOnly
import org.apache.commons.lang3.StringUtils.EMPTY
import org.jdbi.v3.core.Jdbi
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.sql.ResultSet
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import javax.transaction.Transactional

@Singleton
class AuditDetailRepository @Inject constructor(
   private val auditScanAreaRepository: AuditScanAreaRepository,
   private val employeeRepository: EmployeeRepository,
   private val jdbc: Jdbi
) {
   private val logger: Logger = LoggerFactory.getLogger(AuditDetailRepository::class.java)

   private fun selectBaseQuery(): String {
      return """
         WITH employees AS (
            ${employeeRepository.employeeBaseQuery()}
         )
         SELECT
            auditDetail.id AS auditDetail_id,
            auditDetail.time_created                  AS auditDetail_time_created,
            auditDetail.time_updated                  AS auditDetail_time_updated,
            auditDetail.lookup_key                    AS auditDetail_lookup_key,
            auditDetail.barcode                       AS auditDetail_barcode,
            auditDetail.product_code                  AS auditDetail_product_code,
            auditDetail.alt_id                        AS auditDetail_alt_id,
            auditDetail.serial_number                 AS auditDetail_serial_number,
            auditDetail.inventory_brand               AS auditDetail_inventory_brand,
            auditDetail.inventory_model               AS auditDetail_inventory_model,
            auditDetail.audit_id                      AS auditDetail_audit_id,
            scannedBy.emp_id                          AS scannedBy_id,
            scannedBy.emp_number                      AS scannedBy_number,
            scannedBy.emp_last_name                   AS scannedBy_last_name,
            scannedBy.emp_first_name_mi               AS scannedBy_first_name_mi,
            scannedBy.emp_type                        AS scannedBy_type,
            scannedBy.emp_pass_code                   AS scannedBy_pass_code,
            scannedBy.emp_active                      AS scannedBy_active,
            scannedBy.emp_cynergi_system_admin        AS scannedBy_cynergi_system_admin,
            scannedBy.emp_alternative_store_indicator AS scannedBy_alternative_store_indicator,
            scannedBy.emp_alternative_area            AS scannedBy_alternative_area,
            scannedBy.store_id                        AS store_id,
            scannedBy.store_number                    AS store_number,
            scannedBy.store_name                      AS store_name,
            scannedBy.comp_id                         AS comp_id,
            scannedBy.comp_time_created               AS comp_time_created,
            scannedBy.comp_time_updated               AS comp_time_updated,
            scannedBy.comp_name                       AS comp_name,
            scannedBy.comp_doing_business_as          AS comp_doing_business_as,
            scannedBy.comp_client_code                AS comp_client_code,
            scannedBy.comp_client_id                  AS comp_client_id,
            scannedBy.comp_dataset_code               AS comp_dataset_code,
            scannedBy.comp_federal_id_number          AS comp_federal_id_number,
            scannedBy.dept_id                         AS dept_id,
            scannedBy.dept_code                       AS dept_code,
            scannedBy.dept_description                AS dept_description,
            scanArea.id                               AS scanArea_id,
            scanArea.name                             AS scanArea_name,
            store.id                                  AS scanAreaStore_id,
            store.number                              AS scanAreaStore_number,
            store.name                                AS scanAreaStore_name
         FROM audit_detail auditDetail
              JOIN audit a ON auditDetail.audit_id = a.id
              JOIN company comp ON a.company_id = comp.id
              JOIN employees scannedBy ON auditDetail.scanned_by = scannedBy.emp_number AND scannedBy.comp_id = comp.id
              JOIN audit_scan_area scanArea ON auditDetail.scan_area_id = scanArea.id
              JOIN fastinfo_prod_import.store_vw store ON comp.dataset_code = store.dataset AND scanArea.store_number_sfk = store.number
      """
   }

   @ReadOnly
   fun exists(auditId: UUID, inventory: InventoryEntity): Boolean {
      val exists = jdbc.queryForObject(
         """
               SELECT EXISTS (
                  SELECT id
                  FROM audit_detail
                  WHERE audit_id = :audit_id
                     AND lookup_key = :lookup_key
               )
            """,
         mapOf(
            "audit_id" to auditId,
            "lookup_key" to inventory.lookupKey
         ),
         Boolean::class.java
      )

      logger.info("Checking if Scan Area with the same name, company, store exists resulted in {}", exists)

      return exists
   }

   @ReadOnly
   fun findOne(id: UUID, company: CompanyEntity): AuditDetailEntity? {
      val sql = "${selectBaseQuery()} WHERE auditDetail.id = :id AND comp.id = :comp_id"
      val params = mutableMapOf<String, Any?>("id" to id, "comp_id" to company.id)

      val found = queryAndExtractFindOneResults(sql, params, company)

      logger.trace("Searching for AuditDetail: {} resulted in {}", id, found)

      return found
   }

   @ReadOnly
   fun findOne(auditId: UUID, inventory: InventoryEntity, company: CompanyEntity): AuditDetailEntity? {
      val sql = "${selectBaseQuery()} WHERE audit_id = :audit_id AND auditDetail.alt_id = :alt_id AND auditDetail.serial_number = :serial_number"
      val params = mapOf("audit_id" to auditId, "alt_id" to inventory.altId, "serial_number" to inventory.serialNumber)

      val found = queryAndExtractFindOneResults(sql, params, company)

      logger.trace(
         "Searching for audit detail by audit ID {} and inventory(lookup_key) {}, resulted in ",
         auditId,
         inventory,
         found
      )

      return found
   }

   private fun queryAndExtractFindOneResults(sql: String, params: Map<String, Any?>, company: CompanyEntity): AuditDetailEntity? {
      logger.trace("Querying for single AuditDetail using {}/{}", sql, params)

      return jdbc.findFirstOrNull(sql, params) { rs, _ ->
         val scannedBy = employeeRepository.mapRow(rs, "scannedBy_")
         val auditScanArea = auditScanAreaRepository.mapRow(rs, company, "scanArea_", "scanAreaStore_")

         mapRow(
            rs,
            auditScanArea,
            scannedBy,
            SimpleIdentifiableEntity(rs.getUuid("auditDetail_audit_id")),
            "auditDetail_"
         )
      }
   }

   @ReadOnly
   fun findAll(
      audit: AuditEntity,
      company: CompanyEntity,
      page: PageRequest
   ): RepositoryPage<AuditDetailEntity, PageRequest> {
      val params = mutableMapOf<String, Any?>("audit_id" to audit.id, "comp_id" to company.id)
      val query =
         """
      WITH paged AS (
         ${selectBaseQuery()}
         WHERE scannedBy.comp_id = :comp_id
      )
      SELECT
         p.*,
         count(*) OVER() as total_elements
      FROM paged AS p
      WHERE p.auditDetail_audit_id = :audit_id
      ORDER by auditDetail_${page.snakeSortBy()} ${page.sortDirection()}
      LIMIT ${page.size()} OFFSET ${page.offset()}
      """
      var totalElements: Long? = null
      val resultList: MutableList<AuditDetailEntity> = mutableListOf()

      jdbc.query(query, params) { rs, _ ->
         val scannedBy = employeeRepository.mapRow(rs, "scannedBy_")
         val auditScanArea = auditScanAreaRepository.mapRow(rs, company, "scanArea_", "scanAreaStore_")

         resultList.add(
            mapRow(
               rs,
               auditScanArea,
               scannedBy,
               SimpleIdentifiableEntity(rs.getUuid("auditDetail_audit_id")),
               "auditDetail_"
            )
         )

         if (totalElements == null) {
            totalElements = rs.getLong("total_elements")
         }
      }

      return RepositoryPage(
         requested = page,
         elements = resultList,
         totalElements = totalElements ?: 0
      )
   }

   @Transactional
   fun insert(entity: AuditDetailEntity): AuditDetailEntity {
      logger.debug("Inserting audit_detail {}", entity)

      return jdbc.insertReturning(
         """
         INSERT INTO audit_detail(scan_area_id, lookup_key, barcode, product_code, alt_id, serial_number, inventory_brand, inventory_model, scanned_by, audit_id)
         VALUES (:scan_area_id, :lookup_key, :barcode, :product_code, :alt_id, :serial_number, :inventory_brand, :inventory_model, :scanned_by, :audit_id)
         RETURNING
            *
         """.trimIndent(),
         mapOf(
            "scan_area_id" to entity.scanArea.id,
            "lookup_key" to entity.lookupKey,
            "barcode" to entity.barcode,
            "product_code" to entity.productCode,
            "alt_id" to entity.altId,
            "serial_number" to entity.serialNumber,
            "inventory_brand" to entity.inventoryBrand,
            "inventory_model" to entity.inventoryModel,
            "scanned_by" to entity.scannedBy.number,
            "audit_id" to entity.audit.myId()
         )
      ) { rs, _ ->
         mapRow(rs, entity.scanArea, entity.scannedBy, entity.audit)
      }
   }

   @Transactional
   fun update(entity: AuditDetailEntity): AuditDetailEntity {
      logger.debug("Updating audit_detail {}", entity)

      return jdbc.updateReturning(
         """
         UPDATE audit_detail
         SET
            scan_area_id = :scan_area_id,
            lookup_key = :lookup_key,
            barcode = :barcode,
            product_code = :product_code,
            alt_id = :alt_id,
            serial_number = :serial_number,
            inventory_brand = :inventory_brand,
            inventory_model = :inventory_model,
            scanned_by = :scanned_by
         WHERE id = :id
         RETURNING
            *
         """.trimIndent(),
         mapOf(
            "id" to entity.id,
            "scan_area_id" to entity.scanArea.id,
            "lookup_key" to entity.lookupKey,
            "barcode" to entity.barcode,
            "product_code" to entity.productCode,
            "alt_id" to entity.altId,
            "serial_number" to entity.serialNumber,
            "inventory_brand" to entity.inventoryBrand,
            "inventory_model" to entity.inventoryModel,
            "scanned_by" to entity.scannedBy.number
         )
      ) { rs, _ ->
         mapRow(rs, entity.scanArea, entity.scannedBy, entity.audit)
      }
   }

   private fun mapRow(
      rs: ResultSet,
      scanArea: AuditScanAreaEntity,
      scannedBy: EmployeeEntity,
      audit: Identifiable,
      columnPrefix: String = EMPTY
   ): AuditDetailEntity {
      return AuditDetailEntity(
         id = rs.getUuid("${columnPrefix}id"),
         timeCreated = rs.getOffsetDateTime("${columnPrefix}time_created"),
         timeUpdated = rs.getOffsetDateTime("${columnPrefix}time_updated"),
         scanArea = scanArea,
         lookupKey = rs.getString("${columnPrefix}lookup_key"),
         barcode = rs.getString("${columnPrefix}barcode"),
         productCode = rs.getString("${columnPrefix}product_code"),
         altId = rs.getString("${columnPrefix}alt_id"),
         serialNumber = rs.getString("${columnPrefix}serial_number"),
         inventoryBrand = rs.getString("${columnPrefix}inventory_brand"),
         inventoryModel = rs.getString("${columnPrefix}inventory_model"),
         scannedBy = scannedBy,
         audit = audit
      )
   }
}
