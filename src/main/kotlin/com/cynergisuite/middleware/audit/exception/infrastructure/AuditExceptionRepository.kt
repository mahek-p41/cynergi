package com.cynergisuite.middleware.audit.exception.infrastructure

import com.cynergisuite.domain.Identifiable
import com.cynergisuite.domain.PageRequest
import com.cynergisuite.domain.SimpleIdentifiableEntity
import com.cynergisuite.domain.StandardPageRequest
import com.cynergisuite.domain.infrastructure.RepositoryPage
import com.cynergisuite.extensions.findFirstOrNull
import com.cynergisuite.extensions.getOffsetDateTime
import com.cynergisuite.extensions.insertReturning
import com.cynergisuite.extensions.queryPaged
import com.cynergisuite.extensions.updateReturning
import com.cynergisuite.middleware.audit.AuditEntity
import com.cynergisuite.middleware.audit.detail.scan.area.AuditScanArea
import com.cynergisuite.middleware.audit.detail.scan.area.infrastructure.AuditScanAreaRepository
import com.cynergisuite.middleware.audit.exception.AuditExceptionEntity
import com.cynergisuite.middleware.audit.exception.note.AuditExceptionNote
import com.cynergisuite.middleware.audit.exception.note.infrastructure.AuditExceptionNoteRepository
import com.cynergisuite.middleware.authentication.user.User
import com.cynergisuite.middleware.company.Company
import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.company.CompanyValueObject
import com.cynergisuite.middleware.department.DepartmentEntity
import com.cynergisuite.middleware.employee.EmployeeEntity
import com.cynergisuite.middleware.employee.infrastructure.EmployeeRepository
import com.cynergisuite.middleware.store.SimpleStore
import com.cynergisuite.middleware.store.Store
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

   private fun findOneQuery(): String = """
      WITH employees AS (
         ${employeeRepository.employeeBaseQuery()}
      )
      SELECT
         auditException.id                                          AS auditException_id,
         auditException.uu_row_id                                   AS auditException_uu_row_id,
         auditException.time_created                                AS auditException_time_created,
         auditException.time_updated                                AS auditException_time_updated,
         auditException.barcode                                     AS auditException_barcode,
         auditException.product_code                                AS auditException_product_code,
         auditException.alt_id                                      AS auditException_alt_id,
         auditException.serial_number                               AS auditException_serial_number,
         auditException.inventory_brand                             AS auditException_inventory_brand,
         auditException.inventory_model                             AS auditException_inventory_model,
         auditException.exception_code                              AS auditException_exception_code,
         auditException.audit_id                                    AS auditException_audit_id,
         auditException.approved                                    AS auditException_approved,
         auditException.approved_by                                 AS auditException_approved_by,
         auditException.lookup_key                                  AS auditException_lookup_key,
         auditScanArea.id                                           AS auditScanArea_id,
         auditScanArea.value                                        AS auditScanArea_value,
         auditScanArea.description                                  AS auditScanArea_description,
         auditScanArea.localization_code                            AS auditScanArea_localization_code,
         comp.id                                                    AS comp_id,
         comp.uu_row_id                                             AS comp_uu_row_id,
         comp.time_created                                          AS comp_time_created,
         comp.time_updated                                          AS comp_time_updated,
         comp.name                                                  AS comp_name,
         comp.doing_business_as                                     AS comp_doing_business_as,
         comp.client_code                                           AS comp_client_code,
         comp.client_id                                             AS comp_client_id,
         comp.dataset_code                                          AS comp_dataset_code,
         comp.federal_id_number                                     AS comp_federal_id_number,
         scannedBy.emp_id                                           AS scannedBy_id,
         scannedBy.emp_type                                         AS scannedBy_type,
         scannedBy.emp_number                                       AS scannedBy_number,
         scannedBy.emp_last_name                                    AS scannedBy_last_name,
         scannedBy.emp_first_name_mi                                AS scannedBy_first_name_mi,
         scannedBy.emp_pass_code                                    AS scannedBy_pass_code,
         scannedBy.emp_active                                       AS scannedBy_active,
         scannedBy.emp_cynergi_system_admin                         AS scannedBy_cynergi_system_admin,
         scannedBy.emp_alternative_store_indicator                  AS scannedBy_alternative_store_indicator,
         scannedBy.emp_alternative_area                             AS scannedBy_alternative_area,
         scannedBy.dept_id                                          AS scannedBy_dept_id,
         scannedBy.dept_code                                        AS scannedBy_dept_code,
         scannedBy.dept_description                                 AS scannedBy_dept_description,
         scannedBy.dept_security_profile                            AS scannedBy_dept_security_profile,
         scannedBy.dept_default_menu                                AS scannedBy_dept_default_menu,
         scannedBy.store_id                                         AS scannedBy_store_id,
         scannedBy.store_number                                     AS scannedBy_store_number,
         scannedBy.store_name                                       AS scannedBy_store_name,
         approvedBy.emp_id                                          AS approvedBy_id,
         approvedBy.emp_type                                        AS approvedBy_type,
         approvedBy.emp_number                                      AS approvedBy_number,
         approvedBy.emp_last_name                                   AS approvedBy_last_name,
         approvedBy.emp_first_name_mi                               AS approvedBy_first_name_mi,
         approvedBy.emp_pass_code                                   AS approvedBy_pass_code,
         approvedBy.emp_active                                      AS approvedBy_active,
         approvedBy.emp_cynergi_system_admin                        AS approvedBy_cynergi_system_admin,
         approvedBy.emp_alternative_store_indicator                 AS approvedBy_alternative_store_indicator,
         approvedBy.emp_alternative_area                            AS approvedBy_alternative_area,
         approvedBy.dept_id                                         AS approvedBy_dept_id,
         approvedBy.dept_code                                       AS approvedBy_dept_code,
         approvedBy.dept_description                                AS approvedBy_dept_description,
         approvedBy.dept_security_profile                           AS approvedBy_dept_security_profile,
         approvedBy.dept_default_menu                               AS approvedBy_dept_default_menu,
         approvedBy.store_id                                        AS approvedBy_store_id,
         approvedBy.store_number                                    AS approvedBy_store_number,
         approvedBy.store_name                                      AS approvedBy_store_name,
         auditExceptionNote.id                                      AS auditExceptionNote_id,
         auditExceptionNote.uu_row_id                               AS auditExceptionNote_uu_row_id,
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
         auditExceptionNoteEmployee.dept_security_profile           AS auditExceptionNoteEmployee_dept_security_profile,
         auditExceptionNoteEmployee.dept_default_menu               AS auditExceptionNoteEmployee_dept_default_menu,
         auditExceptionNoteEmployee.store_id                        AS auditExceptionNoteEmployee_store_id,
         auditExceptionNoteEmployee.store_number                    AS auditExceptionNoteEmployee_store_number,
         auditExceptionNoteEmployee.store_name                      AS auditExceptionNoteEmployee_store_name
      FROM audit_exception auditException
           JOIN audit_scan_area_type_domain AS auditScanArea ON auditException.scan_area_id = auditScanArea.id
           JOIN audit a ON auditException.audit_id = a.id
           JOIN company comp ON a.company_id = comp.id
           JOIN employees scannedBy ON auditException.scanned_by = scannedBy.emp_number AND comp.id = scannedBy.comp_id
           LEFT OUTER JOIN employees approvedBy ON auditException.approved_by = approvedBy.emp_number AND comp.id = approvedBy.comp_id
           LEFT OUTER JOIN audit_exception_note auditExceptionNote ON auditException.id = auditExceptionNote.audit_exception_id
           LEFT OUTER JOIN employees auditExceptionNoteEmployee ON auditExceptionNote.entered_by = auditExceptionNoteEmployee.emp_number AND comp.id = auditExceptionNoteEmployee.comp_id
      """.trimIndent()

   fun findOne(id: Long, company: Company): AuditExceptionEntity? {
      val params = mutableMapOf<String, Any?>("id" to id, "comp_id" to company.myId())
      val query = "${findOneQuery()}\nWHERE auditException.id = :id AND comp.id = :comp_id"

      logger.debug("Searching for AuditExceptions using {} {}", query, params)

      val found = jdbc.findFirstOrNull(query, params) { rs ->
         val scannedBy = mapEmployeeNotNull(rs, "scannedBy_")
         val approvedBy = mapEmployee(rs, "approvedBy_")
         val scanArea = auditScanAreaRepository.mapPrefixedRowOrNull(rs, "auditScanArea_")
         val exception = mapRow(rs, scanArea, scannedBy, approvedBy, SimpleIdentifiableEntity(rs.getLong("auditException_audit_id")), "auditException_")

         do {
            val enteredBy = mapEmployee(rs, "auditExceptionNoteEmployee_")

            if (enteredBy != null) {
               mapRowAuditExceptionNote(rs, enteredBy)?.also { exception.notes.add(it) }
            }
         } while(rs.next())

         exception
      }

      logger.trace("Searching for AuditException: {} resulted in {}", id, found)

      return found
   }

   fun isApproved(auditExceptionId: Long): Boolean {
      val approved = jdbc.queryForObject("SELECT EXISTS(SELECT id FROM audit_exception WHERE id = :id AND approved = TRUE)", mapOf("id" to auditExceptionId), Boolean::class.java)!!

      logger.trace("Checking if AuditException: {} has been approved resulted in {}", auditExceptionId, approved)

      return approved
   }

   fun findAll(audit: AuditEntity, company: Company, page: PageRequest): RepositoryPage<AuditExceptionEntity, PageRequest> {
      val comp_id = company.myId()
      val params = mutableMapOf<String, Any?>("audit_id" to audit.id, "comp_id" to comp_id, "limit" to page.size(), "offset" to page.offset())
      val sql = """
         WITH employees AS (
            ${employeeRepository.employeeBaseQuery()}
         ), paged AS (
            SELECT
               auditException.id                           AS auditException_id,
               auditException.uu_row_id                    AS auditException_uu_row_id,
               auditException.time_created                 AS auditException_time_created,
               auditException.time_updated                 AS auditException_time_updated,
               auditException.barcode                      AS auditException_barcode,
               auditException.product_code                 AS auditException_product_code,
               auditException.alt_id                       AS auditException_alt_id,
               auditException.serial_number                AS auditException_serial_number,
               auditException.inventory_brand              AS auditException_inventory_brand,
               auditException.inventory_model              AS auditException_inventory_model,
               auditException.exception_code               AS auditException_exception_code,
               auditException.audit_id                     AS auditException_audit_id,
               auditException.approved                   AS auditException_approved,
               auditException.approved_by                AS auditException_approved_by,
               auditException.lookup_key                   AS auditException_lookup_key,
               auditScanArea.id                            AS auditScanArea_id,
               auditScanArea.value                         AS auditScanArea_value,
               auditScanArea.description                   AS auditScanArea_description,
               auditScanArea.localization_code             AS auditScanArea_localization_code,
               comp.id                                     AS comp_id,
               comp.uu_row_id                              AS comp_uu_row_id,
               comp.time_created                           AS comp_time_created,
               comp.time_updated                           AS comp_time_updated,
               comp.name                                   AS comp_name,
               comp.doing_business_as                      AS comp_doing_business_as,
               comp.client_code                            AS comp_client_code,
               comp.client_id                              AS comp_client_id,
               comp.dataset_code                           AS comp_dataset_code,
               comp.federal_id_number                      AS comp_federal_id_number,
               scannedBy.emp_id                            AS scannedBy_id,
               scannedBy.emp_type                          AS scannedBy_type,
               scannedBy.emp_number                        AS scannedBy_number,
               scannedBy.emp_last_name                     AS scannedBy_last_name,
               scannedBy.emp_first_name_mi                 AS scannedBy_first_name_mi,
               scannedBy.emp_pass_code                     AS scannedBy_pass_code,
               scannedBy.emp_active                        AS scannedBy_active,
               scannedBy.emp_cynergi_system_admin          AS scannedBy_cynergi_system_admin,
               scannedBy.emp_alternative_store_indicator   AS scannedBy_alternative_store_indicator,
               scannedBy.emp_alternative_area              AS scannedBy_alternative_area,
               scannedBy.dept_id                           AS scannedBy_dept_id,
               scannedBy.dept_code                         AS scannedBy_dept_code,
               scannedBy.dept_description                  AS scannedBy_dept_description,
               scannedBy.dept_security_profile             AS scannedBy_dept_security_profile,
               scannedBy.dept_default_menu                 AS scannedBy_dept_default_menu,
               scannedBy.store_id                          AS scannedBy_store_id,
               scannedBy.store_number                      AS scannedBy_store_number,
               scannedBy.store_name                        AS scannedBy_store_name,
               approvedBy.emp_id                          AS approvedBy_id,
               approvedBy.emp_type                        AS approvedBy_type,
               approvedBy.emp_number                      AS approvedBy_number,
               approvedBy.emp_last_name                   AS approvedBy_last_name,
               approvedBy.emp_first_name_mi               AS approvedBy_first_name_mi,
               approvedBy.emp_pass_code                   AS approvedBy_pass_code,
               approvedBy.emp_active                      AS approvedBy_active,
               approvedBy.emp_cynergi_system_admin        AS approvedBy_cynergi_system_admin,
               approvedBy.emp_alternative_store_indicator AS approvedBy_alternative_store_indicator,
               approvedBy.emp_alternative_area            AS approvedBy_alternative_area,
               approvedBy.dept_id                         AS approvedBy_dept_id,
               approvedBy.dept_code                       AS approvedBy_dept_code,
               approvedBy.dept_description                AS approvedBy_dept_description,
               approvedBy.dept_security_profile           AS approvedBy_dept_security_profile,
               approvedBy.dept_default_menu               AS approvedBy_dept_default_menu,
               approvedBy.store_id                        AS approvedBy_store_id,
               approvedBy.store_number                    AS approvedBy_store_number,
               approvedBy.store_name                      AS approvedBy_store_name,
               count(*) OVER () AS total_elements
            FROM audit_exception auditException
               JOIN audit_scan_area_type_domain AS auditScanArea ON auditException.scan_area_id = auditScanArea.id
               JOIN audit a ON auditException.audit_id = a.id
               JOIN company comp ON a.company_id = comp.id
               JOIN employees scannedBy ON auditException.scanned_by = scannedBy.emp_number AND comp.id = scannedBy.comp_id
               LEFT OUTER JOIN employees approvedBy ON auditException.approved_by = approvedBy.emp_number AND comp.id = approvedBy.comp_id
            WHERE auditException.audit_id = :audit_id AND comp.id = :comp_id
            ORDER BY auditException_${page.snakeSortBy()} ${page.sortDirection()}
            LIMIT :limit OFFSET :offset
         )
         SELECT
            p.*,
            auditExceptionNote.id                                      AS auditExceptionNote_id,
            auditExceptionNote.uu_row_id                               AS auditExceptionNote_uu_row_id,
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
            auditExceptionNoteEmployee.dept_security_profile           AS auditExceptionNoteEmployee_dept_security_profile,
            auditExceptionNoteEmployee.dept_default_menu               AS auditExceptionNoteEmployee_dept_default_menu,
            auditExceptionNoteEmployee.store_id                        AS auditExceptionNoteEmployee_store_id,
            auditExceptionNoteEmployee.store_number                    AS auditExceptionNoteEmployee_store_number,
            auditExceptionNoteEmployee.store_name                      AS auditExceptionNoteEmployee_store_name
         FROM paged AS p
            LEFT OUTER JOIN audit_exception_note auditExceptionNote ON p.auditException_id = auditExceptionNote.audit_exception_id
            LEFT OUTER JOIN employees auditExceptionNoteEmployee ON auditExceptionNote.entered_by = auditExceptionNoteEmployee.emp_number AND p.comp_id = auditExceptionNoteEmployee.comp_id
         ORDER BY auditException_${page.snakeSortBy()}, auditExceptionNote.id ${page.sortDirection()}
      """

      logger.debug("find all audit exceptions {}/{}", sql, params)

      return jdbc.queryPaged(sql, params, page) { rs, elements ->
         var currentId = -1L
         var currentParentEntity: AuditExceptionEntity? = null

         do {
            val tempId = rs.getLong("auditException_id")
            val tempParentEntity: AuditExceptionEntity = if (tempId != currentId) {
               val scannedBy = mapEmployeeNotNull(rs, "scannedBy_")
               val approvedBy = mapEmployee(rs, "approvedBy_")
               val scanArea = auditScanAreaRepository.mapPrefixedRowOrNull(rs, "auditScanArea_")

               currentId = tempId
               currentParentEntity = mapRow(rs, scanArea, scannedBy, approvedBy, SimpleIdentifiableEntity(rs.getLong("auditException_audit_id")), "auditException_")
               elements.add(currentParentEntity)
               currentParentEntity
            } else {
               currentParentEntity!!
            }

            val enteredBy = mapEmployee(rs, "auditExceptionNoteEmployee_")

            if (enteredBy != null) {
               mapRowAuditExceptionNote(rs, enteredBy)?.also { tempParentEntity.notes.add(it) }
            }
         } while (rs.next())
      }
   }

   fun forEach(audit: AuditEntity, callback: (AuditExceptionEntity, even: Boolean) -> Unit) {
      val auditCompany = audit.store.myCompany()
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
         INSERT INTO audit_exception(scan_area_id, barcode, product_code, alt_id, serial_number, inventory_brand, inventory_model, scanned_by, exception_code, approved, approved_by, lookup_key, audit_id)
         VALUES (:scan_area_id, :barcode, :product_code, :alt_id, :serial_number, :inventory_brand, :inventory_model, :scanned_by, :exception_code, :approved, :approved_by, :lookup_key, :audit_id)
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
            "approved" to entity.approved,
            "approved_by" to entity.approvedBy?.number,
            "lookup_key" to entity.lookupKey,
            "audit_id" to entity.audit.myId()
         ),
         RowMapper { rs, _ ->
            mapRow(rs, entity.scanArea, entity.scannedBy, entity.approvedBy, entity.audit, EMPTY)
         }
      )
   }

   @Transactional
   fun approveAllExceptions(audit: AuditEntity, employee: User): Int {
      logger.debug("Updating audit_exception {}", audit)

      return jdbc.update("""
         UPDATE audit_exception
         SET approved = true,
             approved_by = :approved_by
         WHERE audit_id = :audit_id
         AND approved = false
         """,
         mapOf(
            "audit_id" to audit.myId(),
            "approved_by" to employee.myEmployeeNumber()
         )
      )
   }

   @Transactional
   fun update(entity: AuditExceptionEntity): AuditExceptionEntity {
      logger.debug("Updating audit_exception {}", entity)

      jdbc.updateReturning("""
         UPDATE audit_exception
         SET approved = :approved,
             approved_by = :approved_by
         WHERE id = :id
         RETURNING
            *
         """,
         mapOf(
            "approved" to entity.approved,
            "approved_by" to if (entity.approved) entity.approvedBy?.number else null,
            "id" to entity.id
         ),
         RowMapper { rs, _ ->
            mapRow(rs, entity.scanArea, entity.scannedBy, entity.approvedBy, entity.audit, EMPTY)
         }
      )

      val notes = entity.notes
         .asSequence()
         .map { auditExceptionNoteRepository.upsert(it) }
         .toMutableList()

      return entity.copy(notes = notes)
   }

   private fun mapRow(rs: ResultSet, scanArea: AuditScanArea?, scannedBy: EmployeeEntity, approvedBy: EmployeeEntity?, audit: Identifiable, columnPrefix: String): AuditExceptionEntity =
      AuditExceptionEntity(
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
         exceptionCode = rs.getString("${columnPrefix}exception_code"),
         approved = rs.getBoolean("${columnPrefix}approved"),
         approvedBy = approvedBy,
         lookupKey = rs.getString("${columnPrefix}lookup_key"),
         audit = audit
      )

   private fun mapCompany(rs: ResultSet): CompanyEntity {
      return CompanyEntity(
         id = rs.getLong("comp_id"),
         name = rs.getString("comp_name"),
         doingBusinessAs = rs.getString("comp_doing_business_as"),
         clientCode = rs.getString("comp_client_code"),
         clientId = rs.getInt("comp_client_id"),
         federalIdNumber = rs.getString("comp_federal_id_number"),
         datasetCode = rs.getString("comp_dataset_code")
      )
   }

   private fun mapEmployeeNotNull(rs: ResultSet, columnPrefix: String): EmployeeEntity {
      return EmployeeEntity(
         id = rs.getLong("${columnPrefix}id"),
         type = rs.getString("${columnPrefix}type"),
         number = rs.getInt("${columnPrefix}number"),
         company = mapCompany(rs),
         lastName = rs.getString("${columnPrefix}last_name"),
         firstNameMi = rs.getString("${columnPrefix}first_name_mi"),
         passCode = rs.getString("${columnPrefix}pass_code"),
         store = mapScannedByStore(rs, columnPrefix),
         active = rs.getBoolean("${columnPrefix}active"),
         department = mapScannedByDepartment(rs, columnPrefix),
         cynergiSystemAdmin = rs.getBoolean("${columnPrefix}cynergi_system_admin"),
         alternativeStoreIndicator = rs.getString("${columnPrefix}alternative_store_indicator"),
         alternativeArea = rs.getInt("${columnPrefix}alternative_area")
      )
   }

   private fun mapEmployee(rs: ResultSet, columnPrefix: String): EmployeeEntity? {
      return if (rs.getString("${columnPrefix}id") != null) {
         mapEmployeeNotNull(rs, columnPrefix)
      } else {
         null
      }
   }

   private fun mapScannedByStore(rs: ResultSet, columnPrefix: String): Store? {
      return if (rs.getString("${columnPrefix}store_id") != null) {
         SimpleStore(
            id = rs.getLong("${columnPrefix}store_id"),
            number = rs.getInt("${columnPrefix}store_number"),
            name = rs.getString("${columnPrefix}store_name"),
            company = CompanyValueObject.create(mapCompany(rs))!!
         )
      } else {
         null
      }
   }

   private fun mapScannedByDepartment(rs: ResultSet, columnPrefix: String): DepartmentEntity? {
      return if (rs.getString("${columnPrefix}dept_id") != null) {
         DepartmentEntity(
            id = rs.getLong("${columnPrefix}dept_id"),
            code = rs.getString("${columnPrefix}dept_code"),
            description = rs.getString("${columnPrefix}dept_description"),
            securityProfile = rs.getInt("${columnPrefix}dept_security_profile"),
            defaultMenu = rs.getString("${columnPrefix}dept_default_menu"),
            company = mapCompany(rs)
         )
      } else {
         null
      }
   }

   private fun mapRowAuditExceptionNote(rs: ResultSet, enteredBy: EmployeeEntity): AuditExceptionNote? =
      if (rs.getString("auditExceptionNote_id") != null) {
         AuditExceptionNote(
            id = rs.getLong("auditExceptionNote_id"),
            timeCreated = rs.getOffsetDateTime("auditExceptionNote_time_created"),
            timeUpdated = rs.getOffsetDateTime("auditExceptionNote_time_updated"),
            note = rs.getString("auditExceptionNote_note"),
            enteredBy = enteredBy,
            auditException = SimpleIdentifiableEntity(rs.getLong("auditException_id"))
         )
      } else {
         null
      }
}
