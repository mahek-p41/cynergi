package com.cynergisuite.middleware.audit.detail.infrastructure

import com.cynergisuite.domain.Identifiable
import com.cynergisuite.domain.PageRequest
import com.cynergisuite.domain.SimpleIdentifiableEntity
import com.cynergisuite.domain.infrastructure.RepositoryPage
import com.cynergisuite.extensions.findFirstOrNull
import com.cynergisuite.extensions.getOffsetDateTime
import com.cynergisuite.extensions.getUuid
import com.cynergisuite.extensions.insertReturning
import com.cynergisuite.extensions.updateReturning
import com.cynergisuite.middleware.audit.AuditEntity
import com.cynergisuite.middleware.audit.detail.AuditDetailEntity
import com.cynergisuite.middleware.audit.detail.scan.area.AuditScanArea
import com.cynergisuite.middleware.audit.detail.scan.area.infrastructure.AuditScanAreaRepository
import com.cynergisuite.middleware.company.Company
import com.cynergisuite.middleware.employee.EmployeeEntity
import com.cynergisuite.middleware.employee.infrastructure.EmployeeRepository
import io.micronaut.spring.tx.annotation.Transactional
import org.apache.commons.lang3.StringUtils.EMPTY
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.sql.ResultSet
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuditDetailRepository @Inject constructor(
   private val auditScanAreaRepository: AuditScanAreaRepository,
   private val employeeRepository: EmployeeRepository,
   private val jdbc: NamedParameterJdbcTemplate
) {
   private val logger: Logger = LoggerFactory.getLogger(AuditDetailRepository::class.java)

   private fun selectBaseQuery(): String {
      return """
         WITH employees AS (
            ${employeeRepository.employeeBaseQuery()}
         )
         SELECT
            auditDetail.id AS auditDetail_id,
            auditDetail.uu_row_id                     AS auditDetail_uu_row_id,
            auditDetail.time_created                  AS auditDetail_time_created,
            auditDetail.time_updated                  AS auditDetail_time_updated,
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
            scannedBy.comp_uu_row_id                  AS comp_uu_row_id,
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
            asatd.id                                  AS asatd_id,
            asatd.value                               AS asatd_value,
            asatd.description                         AS asatd_description,
            asatd.localization_code                   AS asatd_localization_code
         FROM audit_detail auditDetail
              JOIN audit a ON auditDetail.audit_id = a.id
              JOIN company comp ON a.company_id = comp.id
              JOIN employees scannedBy ON auditDetail.scanned_by = scannedBy.emp_number AND scannedBy.comp_id = comp.id
              JOIN audit_scan_area_type_domain asatd ON auditDetail.scan_area_id = asatd.id
      """
   }

   fun findOne(id: Long, company: Company): AuditDetailEntity? {
      val params = mutableMapOf<String, Any?>("id" to id, "comp_id" to company.myId())
      val query = "${selectBaseQuery()} WHERE auditDetail.id = :id AND comp.id = :comp_id"
      val found = jdbc.findFirstOrNull(query, params, RowMapper { rs, _ ->
            val scannedBy = employeeRepository.mapRow(rs, "scannedBy_")
            val auditScanArea = auditScanAreaRepository.mapPrefixedRow(rs, "asatd_")

            mapRow(rs, auditScanArea, scannedBy, SimpleIdentifiableEntity(rs.getLong("auditDetail_audit_id")), "auditDetail_")
         }
      )

      logger.trace("Searching for AuditDetail: {} resulted in {}", id, found)

      return found
   }

   fun findAll(audit: AuditEntity, company: Company, page: PageRequest): RepositoryPage<AuditDetailEntity, PageRequest> {
      val params = mutableMapOf<String, Any?>("audit_id" to audit.id, "comp_id" to company.myId())
      val query = """
         WITH paged AS (
            ${selectBaseQuery()}
            WHERE comp.id = :comp_id
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

      jdbc.query(query, params) { rs ->
         val scannedBy = employeeRepository.mapRow(rs, "scannedBy_")
         val auditScanArea = auditScanAreaRepository.mapPrefixedRow(rs, "asatd_")

         resultList.add(mapRow(rs, auditScanArea, scannedBy, SimpleIdentifiableEntity(rs.getLong("auditDetail_audit_id")), "auditDetail_"))

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

      return jdbc.insertReturning("""
         INSERT INTO audit_detail(scan_area_id, barcode, product_code, alt_id, serial_number, inventory_brand, inventory_model, scanned_by, audit_id)
         VALUES (:scan_area_id, :barcode, :product_code, :alt_id, :serial_number, :inventory_brand, :inventory_model, :scanned_by, :audit_id)
         RETURNING
            *
         """.trimIndent(),
         mapOf(
            "scan_area_id" to entity.scanArea.id,
            "barcode" to entity.barcode,
            "product_code" to entity.productCode,
            "alt_id" to entity.altId,
            "serial_number" to entity.serialNumber,
            "inventory_brand" to entity.inventoryBrand,
            "inventory_model" to entity.inventoryModel,
            "scanned_by" to entity.scannedBy.number,
            "audit_id" to entity.audit.myId()
         ),
         RowMapper { rs, _ ->
            mapRow(rs, entity.scanArea, entity.scannedBy, entity.audit)
         }
      )
   }

   @Transactional
   fun update(entity: AuditDetailEntity): AuditDetailEntity {
      logger.debug("Updating audit_detail {}", entity)

      return jdbc.updateReturning("""
         UPDATE audit_detail
         SET
            scan_area_id = :scan_area_id,
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
            "barcode" to entity.barcode,
            "product_code" to entity.productCode,
            "alt_id" to entity.altId,
            "serial_number" to entity.serialNumber,
            "inventory_brand" to entity.inventoryBrand,
            "inventory_model" to entity.inventoryModel,
            "scanned_by" to entity.scannedBy.number
         ),
         RowMapper { rs, _ ->
            mapRow(rs, entity.scanArea, entity.scannedBy, entity.audit)
         }
      )
   }

   private fun mapRow(rs: ResultSet, scanArea: AuditScanArea, scannedBy: EmployeeEntity, audit: Identifiable, columnPrefix: String = EMPTY): AuditDetailEntity {
      return AuditDetailEntity(
         id = rs.getLong("${columnPrefix}id"),
         timeCreated = rs.getOffsetDateTime("${columnPrefix}time_created"),
         timeUpdated = rs.getOffsetDateTime("${columnPrefix}time_updated"),
         scanArea = scanArea,
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
