package com.cynergisuite.middleware.audit.exception.infrastructure

import com.cynergisuite.domain.Identifiable
import com.cynergisuite.domain.PageRequest
import com.cynergisuite.domain.SimpleIdentifiableEntity
import com.cynergisuite.domain.infrastructure.RepositoryPage
import com.cynergisuite.extensions.*
import com.cynergisuite.middleware.audit.AuditEntity
import com.cynergisuite.middleware.audit.detail.scan.area.AuditScanArea
import com.cynergisuite.middleware.audit.detail.scan.area.infrastructure.AuditScanAreaRepository
import com.cynergisuite.middleware.audit.exception.AuditExceptionEntity
import com.cynergisuite.middleware.audit.exception.note.infrastructure.AuditExceptionNoteRepository
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
class AuditExceptionRepository @Inject constructor(
   private val auditScanAreaRepository: AuditScanAreaRepository,
   private val auditExceptionNoteRepository: AuditExceptionNoteRepository,
   private val employeeRepository: EmployeeRepository,
   private val jdbc: NamedParameterJdbcTemplate
) {
   private val logger: Logger = LoggerFactory.getLogger(AuditExceptionRepository::class.java)

   fun findOne(id: Long): AuditExceptionEntity? {
      val found = jdbc.findFirstOrNullWithCrossJoin("""
         WITH ae_employees AS (
            ${employeeRepository.selectBase}
         )
         SELECT
            ae.id AS ae_id,
            ae.uu_row_id AS ae_uu_row_id,
            ae.time_created AS ae_time_created,
            ae.time_updated AS ae_time_updated,
            ae.barcode AS ae_barcode,
            ae.product_code AS ae_product_code,
            ae.alt_id AS ae_alt_id,
            ae.serial_number AS ae_serial_number,
            ae.inventory_brand AS ae_inventory_brand,
            ae.inventory_model AS ae_inventory_model,
            ae.exception_code AS ae_exception_code,
            ae.audit_id AS ae_audit_id,
            ae.signed_off AS ae_signed_off,
            ae.lookup_key AS ae_lookup_key,
            e.e_id AS e_id,
            e.e_time_created AS e_time_created,
            e.e_time_updated AS e_time_updated,
            e.e_number AS e_number,
            e.e_last_name AS e_last_name,
            e.e_first_name_mi AS e_first_name_mi,
            e.e_pass_code AS  e_pass_code,
            e.e_active AS e_active,
            e.e_department AS e_department,
            e.e_employee_type AS e_employee_type,
            e.e_allow_auto_store_assign AS e_allow_auto_store_assign,
            e.s_id AS s_id,
            e.s_time_created AS s_time_created,
            e.s_time_updated AS s_time_updated,
            e.s_number AS s_number,
            e.s_name AS s_name,
            e.s_dataset AS s_dataset,
            asatd.id AS asatd_id,
            asatd.value AS asatd_value,
            asatd.description AS asatd_description,
            asatd.localization_code AS asatd_localization_code,
            aen.id AS aen_id,
            aen.uu_row_id AS aen_uu_row_id,
            aen.time_created AS aen_time_created,
            aen.time_updated AS aen_time_updated,
            aen.note AS aen_note,
            aen.entered_by AS aen_entered_by,
            aen.audit_exception_id AS aen_audit_exception_id,
            noteEmployee.e_id AS noteEmployee_id,
            noteEmployee.e_time_created AS noteEmployee_time_created,
            noteEmployee.e_time_updated AS noteEmployee_time_updated,
            noteEmployee.e_number AS noteEmployee_number,
            noteEmployee.e_last_name AS noteEmployee_last_name,
            noteEmployee.e_first_name_mi AS noteEmployee_first_name_mi,
            noteEmployee.e_pass_code AS noteEmployee_pass_code,
            noteEmployee.e_active AS noteEmployee_active,
            noteEmployee.e_department AS noteEmployee_department,
            noteEmployee.e_employee_type AS noteEmployee_employee_type,
            noteEmployee.e_allow_auto_store_assign AS noteEmployee_allow_auto_store_assign,
            noteEmployee.s_id AS noteEmployee_store_id,
            noteEmployee.s_time_created AS noteEmployee_store_time_created,
            noteEmployee.s_time_updated AS noteEmployee_store_time_updated,
            noteEmployee.s_number AS noteEmployee_store_number,
            noteEmployee.s_name AS noteEmployee_store_name,
            noteEmployee.s_dataset AS noteEmployee_store_dataset
         FROM audit_exception ae
              JOIN ae_employees e
                ON ae.scanned_by = e.e_number
              LEFT OUTER JOIN audit_scan_area_type_domain asatd
                ON ae.scan_area_id = asatd.id
              LEFT OUTER JOIN audit_exception_note aen
                ON ae.id = aen.audit_exception_id
              LEFT OUTER JOIN ae_employees noteEmployee
                ON aen.entered_by = noteEmployee.e_number
         WHERE ae.id = :id""".trimIndent(), mapOf("id" to id),
         RowMapper { rs, _ ->
            val scannedBy = employeeRepository.mapRow(rs, "e_", "s_")
            val scanArea = auditScanAreaRepository.mapPrefixedRowOrNull(rs, "asatd_")

            mapRow(rs, scanArea, scannedBy, SimpleIdentifiableEntity(rs.getLong("ae_audit_id")), "ae_")
         }
      ) { auditException, rs ->
         val enteredBy = employeeRepository.maybeMapRow(rs, "noteEmployee_", "noteEmployee_store_")

         if (enteredBy != null) {
            auditExceptionNoteRepository.mapRow(rs, enteredBy, "aen_")?.also { auditException.notes.add(it) }
         }
      }

      logger.trace("Searching for AuditException: {} resulted in {}", id, found)

      return found
   }

   fun findAll(audit: AuditEntity, page: PageRequest): RepositoryPage<AuditExceptionEntity> {
      var totalElements: Long? = null
      val sql = """
         WITH paged AS (
            WITH ae_employees AS (
               ${employeeRepository.selectBase}
            ),
            audit_exceptions AS (
               SELECT ae.id AS ae_id,
                  ae.uu_row_id AS ae_uu_row_id,
                  ae.time_created AS ae_time_created,
                  ae.time_updated AS ae_time_updated,
                  ae.barcode AS ae_barcode,
                  ae.product_code AS ae_product_code,
                  ae.alt_id AS ae_alt_id,
                  ae.serial_number AS ae_serial_number,
                  ae.inventory_brand AS ae_inventory_brand,
                  ae.inventory_model AS ae_inventory_model,
                  ae.exception_code AS ae_exception_code,
                  ae.audit_id AS ae_audit_id,
                  ae.signed_off AS ae_signed_off,
                  ae.lookup_key AS ae_lookup_key,
                  e.e_id AS e_id,
                  e.e_time_created AS e_time_created,
                  e.e_time_updated AS e_time_updated,
                  e.e_number AS e_number,
                  e.e_last_name AS e_last_name,
                  e.e_first_name_mi AS e_first_name_mi,
                  e.e_pass_code AS e_pass_code,
                  e.e_active AS e_active,
                  e.e_department AS e_department,
                  e.e_employee_type AS e_employee_type,
                  e.e_allow_auto_store_assign AS e_allow_auto_store_assign,
                  e.s_id AS s_id,
                  e.s_time_created AS s_time_created,
                  e.s_time_updated AS s_time_updated,
                  e.s_number AS s_number,
                  e.s_name AS s_name,
                  e.s_dataset AS s_dataset,
                  asatd.id AS asatd_id,
                  asatd.value AS asatd_value,
                  asatd.description AS asatd_description,
                  asatd.localization_code AS asatd_localization_code,
                  count(*) OVER() as total_elements
               FROM audit_exception ae
                    JOIN ae_employees e
                      ON ae.scanned_by = e.e_number
                    LEFT OUTER JOIN audit_scan_area_type_domain asatd
                      ON ae.scan_area_id = asatd.id
               WHERE ae.audit_id = :audit_id
               ORDER by ae_${page.snakeSortBy()} ${page.sortDirection}
               LIMIT ${page.size} OFFSET ${page.offset()}
            )
            SELECT
               ae.*,
               aen.id AS aen_id,
               aen.uu_row_id AS aen_uu_row_id,
               aen.time_created AS aen_time_created,
               aen.time_updated AS aen_time_updated,
               aen.note AS aen_note,
               aen.audit_exception_id AS aen_audit_exception_id,
               noteEmployee.e_id AS noteEmployee_id,
               noteEmployee.e_time_created AS noteEmployee_time_created,
               noteEmployee.e_time_updated AS noteEmployee_time_updated,
               noteEmployee.e_number AS noteEmployee_number,
               noteEmployee.e_last_name AS noteEmployee_last_name,
               noteEmployee.e_first_name_mi AS noteEmployee_first_name_mi,
               noteEmployee.e_pass_code AS noteEmployee_pass_code,
               noteEmployee.e_active AS noteEmployee_active,
               noteEmployee.e_department AS noteEmployee_department,
               noteEmployee.e_employee_type AS noteEmployee_employee_type,
               noteEmployee.e_allow_auto_store_assign AS noteEmployee_allow_auto_store_assign,
               noteEmployee.s_id AS noteEmployee_store_id,
               noteEmployee.s_time_created AS noteEmployee_store_time_created,
               noteEmployee.s_time_updated AS noteEmployee_store_time_updated,
               noteEmployee.s_number AS noteEmployee_store_number,
               noteEmployee.s_name AS noteEmployee_store_name,
               noteEmployee.s_dataset AS noteEmployee_store_dataset
            FROM audit_exceptions ae
                 LEFT OUTER JOIN audit_exception_note aen
                   ON ae.ae_id = aen.audit_exception_id
                 LEFT OUTER JOIN ae_employees noteEmployee
                   ON aen.entered_by = noteEmployee.e_number
            ORDER BY ae.ae_id, aen.id ASC
         )
         SELECT p.* FROM paged AS p
         """.trimIndent()

      logger.debug("find all audit exceptions {}", sql)

      val resultList = jdbc.findAllWithCrossJoin(sql, mutableMapOf("audit_id" to audit.id), "ae_id", RowMapper { rs, _ ->
            val scannedBy = employeeRepository.mapRow(rs, "e_")
            val scanArea = auditScanAreaRepository.mapPrefixedRowOrNull(rs, "asatd_")

            if (totalElements == null) {
               totalElements = rs.getLong("total_elements")
            }

            mapRow(rs, scanArea, scannedBy, SimpleIdentifiableEntity(rs.getLong("ae_audit_id")), "ae_")
         }
      ) { auditException, rs ->
         val enteredBy = employeeRepository.maybeMapRow(rs, "noteEmployee_", "noteEmployee_store_")

         if (enteredBy != null) {
            auditExceptionNoteRepository.mapRow(rs, enteredBy, "aen_")?.also { auditException.notes.add(it) }
         }
      }

      return RepositoryPage(
         requested = page,
         elements = resultList,
         totalElements = totalElements ?: 0
      )
   }

   fun forEach(audit: AuditEntity, callback: (AuditExceptionEntity, even: Boolean) -> Unit) {
      var result = findAll(audit, PageRequest(page = 1, size = 100, sortBy = "id", sortDirection = "ASC"))
      var index = 0

      while(result.elements.isNotEmpty()){
         result.elements.forEach { auditException ->
            callback(auditException, index % 2 == 0)
            index++
         }

         result = findAll(audit, result.requested.nextPage())
      }
   }

   fun exists(id: Long): Boolean {
      val exists = jdbc.queryForObject("SELECT EXISTS(SELECT id FROM audit_exception WHERE id = :id)", mapOf("id" to id), Boolean::class.java)!!

      logger.trace("Checking if AuditException: {} exists resulted in {}", id, exists)

      return exists
   }

   fun doesNotExist(id: Long): Boolean = !exists(id)

   @Transactional
   fun insert(entity: AuditExceptionEntity): AuditExceptionEntity {
      logger.debug("Inserting audit_exception {}", entity)

      return jdbc.insertReturning("""
         INSERT INTO audit_exception(scan_area_id, barcode, product_code, alt_id, serial_number, inventory_brand, inventory_model, scanned_by, exception_code, signed_off, lookup_key, audit_id)
         VALUES (:scan_area_id, :barcode, :product_code, :alt_id, :serial_number, :inventory_brand, :inventory_model, :scanned_by, :exception_code, :signed_off, :lookup_key, :audit_id)
         RETURNING
            *
         """.trimIndent(),
         mapOf(
            "scan_area_id" to entity.scanArea?.id,
            "barcode" to entity.barcode,
            "product_code" to entity.productCode,
            "alt_id" to entity.altId,
            "serial_number" to entity.serialNumber,
            "inventory_brand" to entity.inventoryBrand,
            "inventory_model" to entity.inventoryModel,
            "scanned_by" to entity.scannedBy.number,
            "exception_code" to entity.exceptionCode,
            "signed_off" to entity.signedOff,
            "lookup_key" to entity.lookupKey,
            "audit_id" to entity.audit.myId()
         ),
         RowMapper { rs, rowNum ->
            mapRow(rs, entity.scanArea, entity.scannedBy, entity.audit)
         }
      )
   }

   @Transactional
   fun signOffAllExceptions(audit: AuditEntity) {
      logger.debug("Updating audit_exception {}", audit)

      jdbc.update("""
         UPDATE audit_exception
         SET signed_off = true
         WHERE audit_id = :audit_id
         AND signed_off = false
         """.trimIndent(),
         mapOf(
            "audit_id" to audit.myId()
         )
      )
   }

   @Transactional
   fun update(entity: AuditExceptionEntity): AuditExceptionEntity {
      logger.debug("Updating audit_exception {}", entity)

      val notes = entity.notes
         .asSequence()
         .map { auditExceptionNoteRepository.upsert(it) }
         .toMutableList()

      return entity.copy(notes = notes)
   }

   private fun mapRow(rs: ResultSet, scanArea: AuditScanArea?, scannedBy: EmployeeEntity, audit: Identifiable, columnPrefix: String = EMPTY): AuditExceptionEntity =
      AuditExceptionEntity(
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
         exceptionCode = rs.getString("${columnPrefix}exception_code"),
         signedOff = rs.getBoolean("${columnPrefix}signed_off"),
         lookupKey = rs.getString("${columnPrefix}lookup_key"),
         audit = audit
      )
}
