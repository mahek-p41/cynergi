package com.cynergisuite.middleware.audit.detail.infrastructure

import com.cynergisuite.domain.IdentifiableEntity
import com.cynergisuite.domain.PageRequest
import com.cynergisuite.domain.SimpleIdentifiableEntity
import com.cynergisuite.domain.infrastructure.Repository
import com.cynergisuite.domain.infrastructure.RepositoryPage
import com.cynergisuite.extensions.*
import com.cynergisuite.middleware.audit.Audit
import com.cynergisuite.middleware.audit.detail.AuditDetail
import com.cynergisuite.middleware.audit.detail.scan.area.AuditScanArea
import com.cynergisuite.middleware.audit.detail.scan.area.infrastructure.AuditScanAreaRepository
import com.cynergisuite.middleware.employee.Employee
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
) : Repository<AuditDetail> {
   private val logger: Logger = LoggerFactory.getLogger(AuditDetailRepository::class.java)
   private val selectBase = """
      WITH ad_employees AS (
         ${employeeRepository.selectBase}
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
         e.e_time_created AS e_time_created,
         e.e_time_updated AS e_time_updated,
         e.e_number AS e_number,
         e.e_last_name AS e_last_name,
         e.e_first_name_mi AS e_first_name_mi,
         e.e_pass_code AS  e_pass_code,
         e.e_active AS e_active,
         e.e_loc AS e_loc,
         e.s_id AS s_id,
         e.s_time_created AS s_time_created,
         e.s_time_updated AS s_time_updated,
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
   """.trimIndent()

   override fun findOne(id: Long): AuditDetail? {
      val found = jdbc.findFirstOrNull(
         "$selectBase\nWHERE ad.id = :id",
         mapOf("id" to id),
         RowMapper { rs, _ ->
            val scannedBy = employeeRepository.mapRow(rs, "e_")
            val auditScanArea = auditScanAreaRepository.mapPrefixedRow(rs, "asatd_")

            mapRow(rs, auditScanArea, scannedBy, SimpleIdentifiableEntity(rs.getLong("ad_audit_id")), "ad_")
         }
      )

      logger.trace("Searching for AuditDetail: {} resulted in {}", id, found)

      return found
   }

   fun findAll(audit: Audit, page: PageRequest): RepositoryPage<AuditDetail> {
      var totalElements: Long? = null
      val resultList: MutableList<AuditDetail> = mutableListOf()

      jdbc.query("""
         WITH paged AS (
            $selectBase
         )
         SELECT 
            p.*,
            count(*) OVER() as total_elements
         FROM paged AS p
         WHERE p.ad_audit_id = :audit_id
         ORDER by ad_${page.camelizeSortBy()} ${page.sortDirection}
         LIMIT ${page.size} OFFSET ${page.offset()}
      """.trimIndent(),
      mutableMapOf("audit_id" to audit.id)
      ) { rs ->
         val scannedBy = employeeRepository.mapRow(rs, "e_")
         val auditScanArea = auditScanAreaRepository.mapPrefixedRow(rs, "asatd_")

         resultList.add(mapRow(rs, auditScanArea, scannedBy, SimpleIdentifiableEntity(rs.getLong("ad_audit_id")), "ad_"))

         if (totalElements == null) {
            totalElements = rs.getLong("total_elements")
         }
      }

      return RepositoryPage(
         elements = resultList,
         totalElements = totalElements ?: 0
      )
   }

   override fun exists(id: Long): Boolean {
      val exists = jdbc.queryForObject("SELECT EXISTS(SELECT id FROM audit_detail WHERE id = :id)", mapOf("id" to id), Boolean::class.java)!!

      logger.trace("Checking if AuditDetail: {} exists resulted in {}", id, exists)

      return exists
   }

   @Transactional
   override fun insert(entity: AuditDetail): AuditDetail {
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
            "audit_id" to entity.audit.entityId()
         ),
         RowMapper { rs, _ ->
            mapRow(rs, entity.scanArea, entity.scannedBy, entity.audit)
         }
      )
   }

   @Transactional
   override fun update(entity: AuditDetail): AuditDetail {
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

   private fun mapRow(rs: ResultSet, scanArea: AuditScanArea, scannedBy: Employee, audit: IdentifiableEntity, columnPrefix: String = EMPTY): AuditDetail {
      return AuditDetail(
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
