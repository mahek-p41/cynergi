package com.cynergisuite.middleware.audit.exception.infrastructure

import com.cynergisuite.domain.Identifiable
import com.cynergisuite.domain.PageRequest
import com.cynergisuite.domain.SimpleIdentifiableEntity
import com.cynergisuite.domain.StandardPageRequest
import com.cynergisuite.domain.infrastructure.RepositoryPage
import com.cynergisuite.extensions.findFirstOrNull
import com.cynergisuite.extensions.getOffsetDateTime
import com.cynergisuite.extensions.getUuid
import com.cynergisuite.extensions.insertReturning
import com.cynergisuite.extensions.queryPaged
import com.cynergisuite.extensions.updateReturning
import com.cynergisuite.middleware.audit.AuditEntity
import com.cynergisuite.middleware.audit.detail.scan.area.AuditScanArea
import com.cynergisuite.middleware.audit.detail.scan.area.infrastructure.AuditScanAreaRepository
import com.cynergisuite.middleware.audit.exception.AuditExceptionEntity
import com.cynergisuite.middleware.audit.exception.note.infrastructure.AuditExceptionNoteRepository
import com.cynergisuite.middleware.authentication.user.IdentifiableUser
import com.cynergisuite.middleware.authentication.user.User
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
class AuditExceptionRepository @Inject constructor(
   private val auditScanAreaRepository: AuditScanAreaRepository,
   private val auditExceptionNoteRepository: AuditExceptionNoteRepository,
   private val employeeRepository: EmployeeRepository,
   private val jdbc: NamedParameterJdbcTemplate
) {
   private val logger: Logger = LoggerFactory.getLogger(AuditExceptionRepository::class.java)

   fun findOne(id: Long, company: Company): AuditExceptionEntity? {
      val comp_id = company.myId()
      val params = mutableMapOf<String, Any?>("id" to id, "comp_id" to comp_id)
      val query = """
         WITH ae_employees AS (
            ${employeeRepository.employeeBaseQuery()}
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
            ae.signed_off_by AS ae_signed_off_by,
            ae.lookup_key AS ae_lookup_key,
            e.emp_id AS e_id,
            e.emp_number AS e_number,
            e.emp_last_name AS e_last_name,
            e.emp_first_name_mi AS e_first_name_mi,
            e.emp_pass_code AS  e_pass_code,
            e.emp_active AS e_active,
            e.emp_department AS e_department,
            e.emp_type AS e_employee_type,
            e2.emp_id AS e2_id,
            e2.emp_number AS e2_number,
            e2.emp_last_name AS e2_last_name,
            e2.emp_first_name_mi AS e2_first_name_mi,
            e2.emp_pass_code AS  e2_pass_code,
            e2.emp_active AS e2_active,
            e2.emp_department AS e2_department,
            e2.emp_type AS e2_employee_type,
            e.fpis_id AS s_id,
            e.fpis_number AS s_number,
            e.fpis_name AS s_name,
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
            noteEmployee.emp_id AS noteEmployee_id,
            noteEmployee.emp_number AS noteEmployee_number,
            noteEmployee.emp_last_name AS noteEmployee_last_name,
            noteEmployee.emp_first_name_mi AS noteEmployee_first_name_mi,
            noteEmployee.emp_pass_code AS noteEmployee_pass_code,
            noteEmployee.emp_active AS noteEmployee_active,
            noteEmployee.emp_department AS noteEmployee_department,
            noteEmployee.emp_type AS noteEmployee_employee_type,
            noteEmployee.fpis_id AS noteEmployee_store_id,
            noteEmployee.fpis_number AS noteEmployee_store_number,
            noteEmployee.fpis_name AS noteEmployee_store_name
         FROM audit_exception ae
              JOIN ae_employees e
                ON ae.scanned_by = e.emp_number AND e.comp_id = :comp_id
              LEFT OUTER JOIN ae_employees e2
                ON ae.signed_off_by = e2.emp_number AND e2.comp_id = :comp_id
              LEFT OUTER JOIN audit_scan_area_type_domain asatd
                ON ae.scan_area_id = asatd.id
              LEFT OUTER JOIN audit_exception_note aen
                ON ae.id = aen.audit_exception_id
              LEFT OUTER JOIN ae_employees noteEmployee
                ON aen.entered_by = noteEmployee.emp_number AND noteEmployee.comp_id = :comp_id
         WHERE ae.id = :id"""

      val found = jdbc.findFirstOrNull(query, params) { rs ->
         val scannedBy = employeeRepository.mapRow(rs, "e_", "s_")
         val scanArea = auditScanAreaRepository.mapPrefixedRowOrNull(rs, "asatd_")
         val signedOffBy = employeeRepository.mapRowOrNull(rs, "e2_")
         val exception = mapRow(rs, scanArea, scannedBy, signedOffBy, SimpleIdentifiableEntity(rs.getLong("ae_audit_id")), "ae_")

         do {
            val enteredBy = employeeRepository.mapRowOrNull(rs, "noteEmployee_", "noteEmployee_store_")

            if (enteredBy != null) {
               auditExceptionNoteRepository.mapRow(rs, enteredBy, "aen_")?.also { exception.notes.add(it) }
            }
         } while(rs.next())

         exception
      }

      logger.trace("Searching for AuditException: {} resulted in {}", id, found)

      return found
   }

   fun findAll(audit: AuditEntity, company: Company, page: PageRequest): RepositoryPage<AuditExceptionEntity, PageRequest> {
      val comp_id = company.myId()
      val params = mutableMapOf<String, Any?>("audit_id" to audit.id, "comp_id" to comp_id)
      val sql = """
      WITH paged AS (
         WITH ae_employees AS (
            ${employeeRepository.employeeBaseQuery()}
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
               ae.signed_off_by AS ae_signed_off_by,
               ae.lookup_key AS ae_lookup_key,
               ae.scanned_by as ae_scanned_by,
               ae.scan_area_id as ae_scan_area_id,
               e.comp_id AS comp_id,
               e.comp_uu_row_id AS comp_uu_row_id,
               e.comp_time_created AS comp_time_created,
               e.comp_time_updated AS comp_time_updated,
               e.comp_name AS comp_name,
               e.comp_doing_business_as AS comp_doing_business_as,
               e.comp_client_code AS comp_client_code,
               e.comp_client_id AS comp_client_id,
               e.comp_dataset_code AS comp_dataset_code,
               e.comp_federal_id_number AS comp_federal_id_number,
               e.dept_id AS dept_id,
               e.dept_code AS dept_code,
               e.dept_description AS dept_description,
               e.dept_security_profile AS dept_security_profile,
               e.dept_default_menu AS dept_default_menu,
               e.emp_id AS e_id,
               e.emp_number AS e_number,
               e.emp_last_name AS e_last_name,
               e.emp_first_name_mi AS e_first_name_mi,
               e.emp_pass_code AS e_pass_code,
               e.emp_active AS e_active,
               e.emp_department AS e_department,
               e.emp_type AS e_type,
               e2.emp_id AS e2_id,
               e2.emp_number AS e2_number,
               e2.emp_last_name AS e2_last_name,
               e2.emp_first_name_mi AS e2_first_name_mi,
               e2.emp_pass_code AS  e2_pass_code,
               e2.emp_active AS e2_active,
               e2.emp_department AS e2_department,
               e2.emp_type AS e2_type,
               e.fpis_id AS fpis_id,
               e.fpis_number AS s_number,
               e.fpis_name AS s_name,
               asatd.id AS asatd_id,
               asatd.value AS asatd_value,
               asatd.description AS asatd_description,
               asatd.localization_code AS asatd_localization_code,
               count(*) OVER() as total_elements
            FROM audit_exception ae
                 JOIN ae_employees e
                   ON ae.scanned_by = e.emp_number
                   AND e.comp_id = :comp_id
                 LEFT OUTER JOIN ae_employees e2
                           ON ae.signed_off_by = e2.emp_number
                           AND e2.comp_id = :comp_id
                 LEFT OUTER JOIN audit_scan_area_type_domain asatd
                   ON ae.scan_area_id = asatd.id
            WHERE ae.audit_id = :audit_id
            ORDER by ae_${page.snakeSortBy()} ${page.sortDirection()}
            LIMIT ${page.size()} OFFSET ${page.offset()}
         )
         SELECT
            ae.*,
            aen.id AS aen_id,
            aen.uu_row_id AS aen_uu_row_id,
            aen.time_created AS aen_time_created,
            aen.time_updated AS aen_time_updated,
            aen.note AS aen_note,
            aen.audit_exception_id AS aen_audit_exception_id,
            noteEmployee.emp_id AS noteEmployee_id,
            noteEmployee.emp_number AS noteEmployee_number,
            noteEmployee.emp_last_name AS noteEmployee_last_name,
            noteEmployee.emp_first_name_mi AS noteEmployee_first_name_mi,
            noteEmployee.emp_pass_code AS noteEmployee_pass_code,
            noteEmployee.emp_active AS noteEmployee_active,
            noteEmployee.emp_department AS noteEmployee_department,
            noteEmployee.emp_type AS noteEmployee_employee_type,
            noteEmployee.fpis_id AS noteEmployee_store_id,
            noteEmployee.fpis_number AS noteEmployee_store_number,
            noteEmployee.fpis_name AS noteEmployee_store_name
         FROM audit_exceptions ae
              LEFT OUTER JOIN audit_exception_note aen
                ON ae.ae_id = aen.audit_exception_id
              LEFT OUTER JOIN ae_employees noteEmployee
                ON aen.entered_by = noteEmployee.emp_number
                AND ae.comp_id = noteEmployee.comp_id
         ORDER BY ae.ae_id, aen.id ASC
      )
      SELECT p.* FROM paged AS p
      """.trimIndent()

      logger.debug("find all audit exceptions {}", sql)

      return jdbc.queryPaged(sql, params, page) { rs, elements ->
         var currentId = -1L
         var currentParentEntity: AuditExceptionEntity? = null

         do {
            val tempId = rs.getLong("ae_id")
            val tempParentEntity: AuditExceptionEntity = if (tempId != currentId) {
               val scannedBy = employeeRepository.mapRow(rs, "e_")
               val scanArea = auditScanAreaRepository.mapPrefixedRowOrNull(rs, "asatd_")
               val signedOffBy = employeeRepository.mapRowOrNull(rs, "e2_")

               currentId = tempId
               currentParentEntity = mapRow(rs, scanArea, scannedBy, signedOffBy, SimpleIdentifiableEntity(rs.getLong("ae_audit_id")), "ae_")
               elements.add(currentParentEntity)
               currentParentEntity
            } else {
               currentParentEntity!!
            }

            val enteredBy = employeeRepository.mapRowOrNull(rs, "noteEmployee_", "noteEmployee_store_")

            if (enteredBy != null) {
               auditExceptionNoteRepository.mapRow(rs, enteredBy, "aen_")?.also { tempParentEntity.notes.add(it) }
            }
         } while (rs.next())
      }
   }

   fun forEach(audit: AuditEntity, callback: (AuditExceptionEntity, even: Boolean) -> Unit) {
      val auditCompany = audit.store.company
      var result = findAll(audit, auditCompany, StandardPageRequest(page = 1, size = 100, sortBy = "id", sortDirection = "ASC"))
      var index = 0

      while(result.elements.isNotEmpty()) {
         result.elements.forEach { auditException: AuditExceptionEntity ->
            callback(auditException, index % 2 == 0)
            index++
         }

         result = findAll(audit, auditCompany, result.requested.nextPage())
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
         INSERT INTO audit_exception(scan_area_id, barcode, product_code, alt_id, serial_number, inventory_brand, inventory_model, scanned_by, exception_code, signed_off, signed_off_by, lookup_key, audit_id)
         VALUES (:scan_area_id, :barcode, :product_code, :alt_id, :serial_number, :inventory_brand, :inventory_model, :scanned_by, :exception_code, :signed_off, :signed_off_by, :lookup_key, :audit_id)
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
            "scanned_by" to entity.scannedBy.myEmployeeNumber(),
            "exception_code" to entity.exceptionCode,
            "signed_off" to entity.signedOff,
            "signed_off_by" to entity.signedOffBy?.myEmployeeNumber(),
            "lookup_key" to entity.lookupKey,
            "audit_id" to entity.audit.myId()
         ),
         RowMapper { rs, _ ->
            mapRow(rs, entity.scanArea, entity.scannedBy, entity.signedOffBy, entity.audit)
         }
      )
   }

   @Transactional
   fun signOffAllExceptions(audit: AuditEntity, employee: User): Int {
      logger.debug("Updating audit_exception {}", audit)

      return jdbc.update("""
         UPDATE audit_exception
         SET signed_off = true,
             signed_off_by = :employee
         WHERE audit_id = :audit_id
         AND signed_off = false
         """,
         mapOf(
            "audit_id" to audit.myId(),
            "employee" to employee.myEmployeeNumber()
         )
      )
   }

   @Transactional
   fun update(entity: AuditExceptionEntity): AuditExceptionEntity {
      logger.debug("Updating audit_exception {}", entity)

      jdbc.updateReturning("""
         UPDATE audit_exception
         SET signed_off = :signed_off,
             signed_off_by = :employee
         WHERE id = :id
         RETURNING
            *
         """,
         mapOf(
            "signed_off" to entity.signedOff,
            "employee" to if (entity.signedOff) entity.signedOffBy?.myEmployeeNumber() else null,
            "id" to entity.id
         ),
         RowMapper { rs, _ ->
            mapRow(rs, entity.scanArea, entity.scannedBy, entity.signedOffBy, entity.audit)
         }
      )

      val notes = entity.notes
         .asSequence()
         .map { auditExceptionNoteRepository.upsert(it) }
         .toMutableList()

      return entity.copy(notes = notes)
   }

   private fun mapRow(rs: ResultSet, scanArea: AuditScanArea?, scannedBy: EmployeeEntity, signedOffBy: EmployeeEntity?, audit: Identifiable, columnPrefix: String = EMPTY): AuditExceptionEntity =
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
         signedOffBy = signedOffBy,
         lookupKey = rs.getString("${columnPrefix}lookup_key"),
         audit = audit
      )
}
