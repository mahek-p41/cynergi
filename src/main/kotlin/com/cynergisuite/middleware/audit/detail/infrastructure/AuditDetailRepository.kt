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

   private fun selectBaseQuery(params: MutableMap<String, Any?>, dataset: String): String {
      return """
         WITH ad_employees AS (
            ${employeeRepository.selectBaseQuery(params, dataset)}
         )
         SELECT
            ad.id AS ad_id,
            ad.uu_row_id AS ad_uu_row_id,
            ad.time_created AS ad_time_created,
            ad.time_updated AS ad_time_updated,
            ad.barcode AS ad_barcode,
            ad.product_code AS ad_product_code,
            ad.alt_id AS ad_alt_id,
            ad.serial_number AS ad_serial_number,
            ad.inventory_brand AS ad_inventory_brand,
            ad.inventory_model AS ad_inventory_model,
            ad.audit_id AS ad_audit_id,
            e.e_id AS e_id,
            e.e_number AS e_number,
            e.e_dataset AS e_dataset,
            e.e_last_name AS e_last_name,
            e.e_first_name_mi AS e_first_name_mi,
            e.e_pass_code AS  e_pass_code,
            e.e_department AS e_department,
            e.e_active AS e_active,
            e.e_employee_type AS e_employee_type,
            e.e_allow_auto_store_assign AS e_allow_auto_store_assign,
            e.s_id AS s_id,
            e.s_number AS s_number,
            e.s_name AS s_name,
            e.s_dataset AS s_dataset,
            asatd.id AS asatd_id,
            asatd.value AS asatd_value,
            asatd.description AS asatd_description,
            asatd.localization_code AS asatd_localization_code
         FROM audit_detail ad
              JOIN ad_employees e
                ON ad.scanned_by = e.e_number
              JOIN audit_scan_area_type_domain asatd
                ON ad.scan_area_id = asatd.id
      """
   }

   fun findOne(id: Long, dataset: String): AuditDetailEntity? {
      val params = mutableMapOf<String, Any?>("id" to id)
      val query = "${selectBaseQuery(params, dataset)} WHERE ad.id = :id"
      val found = jdbc.findFirstOrNull(query, params, RowMapper { rs, _ ->
            val scannedBy = employeeRepository.mapRow(rs, "e_")
            val auditScanArea = auditScanAreaRepository.mapPrefixedRow(rs, "asatd_")

            mapRow(rs, auditScanArea, scannedBy, SimpleIdentifiableEntity(rs.getLong("ad_audit_id")), "ad_")
         }
      )

      logger.trace("Searching for AuditDetail: {} resulted in {}", id, found)

      return found
   }

   fun findAll(audit: AuditEntity, dataset: String, page: PageRequest): RepositoryPage<AuditDetailEntity, PageRequest> {
      val params = mutableMapOf<String, Any?>("audit_id" to audit.id)
      val query = """
         WITH paged AS (
            ${selectBaseQuery(params, dataset)}
         )
         SELECT
            p.*,
            count(*) OVER() as total_elements
         FROM paged AS p
         WHERE p.ad_audit_id = :audit_id
         ORDER by ad_${page.snakeSortBy()} ${page.sortDirection()}
         LIMIT ${page.size()} OFFSET ${page.offset()}
      """
      var totalElements: Long? = null
      val resultList: MutableList<AuditDetailEntity> = mutableListOf()

      jdbc.query(query, params) { rs ->
         val scannedBy = employeeRepository.mapRow(rs, "e_")
         val auditScanArea = auditScanAreaRepository.mapPrefixedRow(rs, "asatd_")

         resultList.add(mapRow(rs, auditScanArea, scannedBy, SimpleIdentifiableEntity(rs.getLong("ad_audit_id")), "ad_"))

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

   fun exists(id: Long): Boolean {
      val exists = jdbc.queryForObject("SELECT EXISTS(SELECT id FROM audit_detail WHERE id = :id)", mapOf("id" to id), Boolean::class.java)!!

      logger.trace("Checking if AuditDetail: {} exists resulted in {}", id, exists)

      return exists
   }

   fun doesNotExist(id: Long): Boolean = !exists(id)

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
         uuRowId = rs.getUuid("${columnPrefix}uu_row_id"),
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
