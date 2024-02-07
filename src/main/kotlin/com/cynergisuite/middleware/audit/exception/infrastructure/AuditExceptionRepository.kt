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
import com.cynergisuite.extensions.queryForObject
import com.cynergisuite.extensions.queryPaged
import com.cynergisuite.extensions.update
import com.cynergisuite.extensions.updateReturning
import com.cynergisuite.middleware.address.AddressEntity
import com.cynergisuite.middleware.address.AddressRepository
import com.cynergisuite.middleware.audit.AuditEntity
import com.cynergisuite.middleware.audit.detail.scan.area.AuditScanAreaEntity
import com.cynergisuite.middleware.audit.detail.scan.area.infrastructure.AuditScanAreaRepository
import com.cynergisuite.middleware.audit.exception.AuditExceptionEntity
import com.cynergisuite.middleware.audit.exception.note.AuditExceptionNote
import com.cynergisuite.middleware.audit.exception.note.infrastructure.AuditExceptionNoteRepository
import com.cynergisuite.middleware.authentication.user.User
import com.cynergisuite.middleware.authentication.user.infrastructure.SecurityGroupRepository
import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.company.infrastructure.CompanyRepository
import com.cynergisuite.middleware.department.DepartmentEntity
import com.cynergisuite.middleware.employee.EmployeeEntity
import com.cynergisuite.middleware.store.Store
import com.cynergisuite.middleware.store.StoreEntity
import io.micronaut.transaction.annotation.ReadOnly
import jakarta.inject.Inject
import jakarta.inject.Singleton
import org.apache.commons.lang3.StringUtils.EMPTY
import org.jdbi.v3.core.Jdbi
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.sql.ResultSet
import java.util.UUID
import javax.transaction.Transactional

@Singleton
class AuditExceptionRepository @Inject constructor(
   private val addressRepository: AddressRepository,
   private val auditScanAreaRepository: AuditScanAreaRepository,
   private val auditExceptionNoteRepository: AuditExceptionNoteRepository,
   private val companyRepository: CompanyRepository,
   private val securityGroupRepository: SecurityGroupRepository,
   private val jdbc: Jdbi
) {
   private val logger: Logger = LoggerFactory.getLogger(AuditExceptionRepository::class.java)

   private fun findOneQuery(): String =
      """
      SELECT
         auditException.id                                          AS auditException_id,
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
         auditScanArea.name                                         AS auditScanArea_name,
         store.id                                                   AS store_id,
         store.number                                               AS store_number,
         store.name                                                 AS store_name,
         comp.id                                                    AS comp_id,
         comp.time_created                                          AS comp_time_created,
         comp.time_updated                                          AS comp_time_updated,
         comp.name                                                  AS comp_name,
         comp.doing_business_as                                     AS comp_doing_business_as,
         comp.client_code                                           AS comp_client_code,
         comp.client_id                                             AS comp_client_id,
         comp.dataset_code                                          AS comp_dataset_code,
         comp.federal_id_number                                     AS comp_federal_id_number,
         comp.include_demo_inventory                                AS comp_include_demo_inventory,
         comp.address_id                                            AS address_id,
         comp.address_name                                          AS address_name,
         comp.address_address1                                      AS address_address1,
         comp.address_address2                                      AS address_address2,
         comp.address_city                                          AS address_city,
         comp.address_state                                         AS address_state,
         comp.address_postal_code                                   AS address_postal_code,
         comp.address_latitude                                      AS address_latitude,
         comp.address_longitude                                     AS address_longitude,
         comp.address_country                                       AS address_country,
         comp.address_county                                        AS address_county,
         comp.address_phone                                         AS address_phone,
         comp.address_fax                                           AS address_fax,
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
         scannedBy.store_id                                         AS scannedBy_store_id,
         scannedBy.store_number                                     AS scannedBy_store_number,
         scannedBy.store_name                                       AS scannedBy_store_name,
		   secGrp.id													           AS secGrp_id,
		   secGrp.value											              AS secGrp_value,
         secGrp.description											        AS secGrp_description,
         secGrp.company_id											           AS secGrp_company,
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
         approvedBy.store_id                                        AS approvedBy_store_id,
         approvedBy.store_number                                    AS approvedBy_store_number,
         approvedBy.store_name                                      AS approvedBy_store_name,
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
      FROM audit_exception auditException
           JOIN audit_scan_area AS auditScanArea ON auditException.scan_area_id = auditScanArea.id
           JOIN audit a ON auditException.audit_id = a.id
           JOIN (${companyRepository.companyBaseQuery()}) comp ON a.company_id = comp.id AND comp.deleted = FALSE
           JOIN system_employees_fimvw scannedBy ON auditException.scanned_by = scannedBy.emp_number AND comp.id = scannedBy.comp_id
           LEFT JOIN employee_to_security_group empSecGrp on scannedBy.emp_number = empSecGrp.employee_id_sfk and empSecGrp.deleted = FALSE
		     LEFT JOIN security_group secGrp on empSecGrp.security_group_id = secGrp.id
           JOIN system_stores_fimvw store ON comp.dataset_code = store.dataset AND auditScanArea.store_number_sfk = store.number
           LEFT OUTER JOIN system_employees_fimvw approvedBy ON auditException.approved_by = approvedBy.emp_number AND comp.id = approvedBy.comp_id
           LEFT OUTER JOIN audit_exception_note auditExceptionNote ON auditException.id = auditExceptionNote.audit_exception_id
           LEFT OUTER JOIN system_employees_fimvw auditExceptionNoteEmployee ON auditExceptionNote.entered_by = auditExceptionNoteEmployee.emp_number AND comp.id = auditExceptionNoteEmployee.comp_id
      """.trimIndent()

   @ReadOnly
   fun findOne(id: UUID, company: CompanyEntity): AuditExceptionEntity? {
      val params = mutableMapOf<String, Any?>("id" to id, "comp_id" to company.id)
      val query = "${findOneQuery()}\nWHERE auditException.id = :id AND comp.id = :comp_id"

      logger.trace("Searching for AuditExceptions using {} {}", query, params)

      val found = jdbc.findFirstOrNull(query, params) { rs, _ ->
         val address = addressRepository.mapAddressOrNull(rs, "address_")
         val scannedBy = mapEmployeeNotNull(rs, address, "scannedBy_")
         val approvedBy = mapEmployee(rs, address, "approvedBy_")
         val scanArea = auditScanAreaRepository.mapRow(rs, company, "auditScanArea_")
         val exception = mapRow(rs, scanArea, scannedBy, approvedBy, SimpleIdentifiableEntity(rs.getUuid("auditException_audit_id")), "auditException_")

         do {
            val enteredBy = mapEmployee(rs, address, "auditExceptionNoteEmployee_")

            if (enteredBy != null) {
               mapRowAuditExceptionNote(rs, enteredBy)?.also { exception.notes.add(it) }
            }
         } while (rs.next())

         exception
      }

      logger.trace("Searching for AuditException: {} resulted in {}", id, found)

      return found
   }

   @ReadOnly
   fun isApproved(auditExceptionId: UUID): Boolean {
      val approved = jdbc.queryForObject("SELECT EXISTS(SELECT id FROM audit_exception WHERE id = :id AND approved = TRUE)", mapOf("id" to auditExceptionId), Boolean::class.java)

      logger.trace("Checking if AuditException: {} has been approved resulted in {}", auditExceptionId, approved)

      return approved
   }

   /**
    * This method used for validation does not check against the ID column, so if it is set in the provided entity
    * it will be ignored.
    */
   @ReadOnly
   fun existsForAudit(auditExceptionEntity: AuditExceptionEntity): Boolean {
      logger.trace("Checking if audit exception exists for {}", auditExceptionEntity)

      return jdbc.queryForObject(
         """
         SELECT EXISTS(
             SELECT id
             FROM audit_exception
             WHERE exception_code = :exception_code
                   AND audit_id = :audit_id
                   AND lookup_key = :lookup_key
         )
         """.trimIndent(),
         mapOf<String, Any?>(
            "barcode" to auditExceptionEntity.barcode,
            "serial_number" to auditExceptionEntity.serialNumber,
            "inventory_brand" to auditExceptionEntity.inventoryBrand,
            "inventory_model" to auditExceptionEntity.inventoryModel,
            "exception_code" to auditExceptionEntity.exceptionCode,
            "audit_id" to auditExceptionEntity.audit.myId(),
            "product_code" to auditExceptionEntity.productCode,
            "alt_id" to auditExceptionEntity.altId,
            "lookup_key" to auditExceptionEntity.lookupKey,
            "scan_area_id" to auditExceptionEntity.scanArea?.myId()
         ),
         Boolean::class.java
      )
   }

   @ReadOnly
   fun findAll(audit: AuditEntity, company: CompanyEntity, page: PageRequest): RepositoryPage<AuditExceptionEntity, PageRequest> {
      val compId = company.id
      val params = mutableMapOf("audit_id" to audit.id, "comp_id" to compId, "limit" to page.size(), "offset" to page.offset())
      val sql =
         """
         WITH paged AS (
            SELECT
               auditException.id                           AS auditException_id,
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
               auditException.approved                     AS auditException_approved,
               auditException.approved_by                  AS auditException_approved_by,
               auditException.lookup_key                   AS auditException_lookup_key,
               auditScanArea.id                            AS auditScanArea_id,
               auditScanArea.name                          AS auditScanArea_name,
               store.id                                    AS store_id,
               store.number                                AS store_number,
               store.name                                  AS store_name,
               comp.id                                     AS comp_id,
               comp.time_created                           AS comp_time_created,
               comp.time_updated                           AS comp_time_updated,
               comp.name                                   AS comp_name,
               comp.doing_business_as                      AS comp_doing_business_as,
               comp.client_code                            AS comp_client_code,
               comp.client_id                              AS comp_client_id,
               comp.dataset_code                           AS comp_dataset_code,
               comp.federal_id_number                      AS comp_federal_id_number,
               comp.include_demo_inventory                 AS comp_include_demo_inventory,
               comp.address_id                             AS address_id,
               comp.address_name                           AS address_name,
               comp.address_address1                       AS address_address1,
               comp.address_address2                       AS address_address2,
               comp.address_city                           AS address_city,
               comp.address_state                          AS address_state,
               comp.address_postal_code                    AS address_postal_code,
               comp.address_latitude                       AS address_latitude,
               comp.address_longitude                      AS address_longitude,
               comp.address_country                        AS address_country,
               comp.address_county                         AS address_county,
               comp.address_phone                          AS address_phone,
               comp.address_fax                            AS address_fax,
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
               scannedBy.store_id                          AS scannedBy_store_id,
               scannedBy.store_number                      AS scannedBy_store_number,
               scannedBy.store_name                        AS scannedBy_store_name,
               approvedBy.emp_id                           AS approvedBy_id,
               approvedBy.emp_type                         AS approvedBy_type,
               approvedBy.emp_number                       AS approvedBy_number,
               approvedBy.emp_last_name                    AS approvedBy_last_name,
               approvedBy.emp_first_name_mi                AS approvedBy_first_name_mi,
               approvedBy.emp_pass_code                    AS approvedBy_pass_code,
               approvedBy.emp_active                       AS approvedBy_active,
               approvedBy.emp_cynergi_system_admin         AS approvedBy_cynergi_system_admin,
               approvedBy.emp_alternative_store_indicator  AS approvedBy_alternative_store_indicator,
               approvedBy.emp_alternative_area             AS approvedBy_alternative_area,
               approvedBy.dept_id                          AS approvedBy_dept_id,
               approvedBy.dept_code                        AS approvedBy_dept_code,
               approvedBy.dept_description                 AS approvedBy_dept_description,
               approvedBy.store_id                         AS approvedBy_store_id,
               approvedBy.store_number                     AS approvedBy_store_number,
               approvedBy.store_name                       AS approvedBy_store_name,
               count(*) OVER () AS total_elements
            FROM audit_exception auditException
               JOIN audit_scan_area AS auditScanArea ON auditException.scan_area_id = auditScanArea.id
               JOIN audit a ON auditException.audit_id = a.id
               JOIN (${companyRepository.companyBaseQuery()}) comp ON a.company_id = comp.id AND comp.deleted = FALSE
               JOIN system_employees_fimvw scannedBy ON auditException.scanned_by = scannedBy.emp_number AND comp.id = scannedBy.comp_id
               JOIN system_stores_fimvw store ON comp.dataset_code = store.dataset AND auditScanArea.store_number_sfk = store.number
               LEFT OUTER JOIN system_employees_fimvw approvedBy ON auditException.approved_by = approvedBy.emp_number AND comp.id = approvedBy.comp_id
            WHERE auditException.audit_id = :audit_id AND comp.id = :comp_id
            ORDER BY auditException_${page.snakeSortBy()} ${page.sortDirection()}
            LIMIT :limit OFFSET :offset
         )
         SELECT
            p.*,
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
            LEFT OUTER JOIN audit_exception_note auditExceptionNote ON p.auditException_id = auditExceptionNote.audit_exception_id
            LEFT OUTER JOIN system_employees_fimvw auditExceptionNoteEmployee ON auditExceptionNote.entered_by = auditExceptionNoteEmployee.emp_number AND p.comp_id = auditExceptionNoteEmployee.comp_id
         ORDER BY auditException_${page.snakeSortBy()}, auditExceptionNote.id ${page.sortDirection()}
      """

      logger.trace("find all audit exceptions {}/{}", sql, params)

      return jdbc.queryPaged(sql, params, page) { rs, elements ->
         val address = addressRepository.mapAddressOrNull(rs, "address_")
         var currentId: UUID? = null
         var currentParentEntity: AuditExceptionEntity? = null

         do {
            val tempId = rs.getUuid("auditException_id")
            val tempParentEntity: AuditExceptionEntity = if (tempId != currentId) {
               val scannedBy = mapEmployeeNotNull(rs, address, "scannedBy_")
               val approvedBy = mapEmployee(rs, address, "approvedBy_")
               val scanArea = auditScanAreaRepository.mapRow(rs, company, "auditScanArea_")

               currentId = tempId
               currentParentEntity = mapRow(rs, scanArea, scannedBy, approvedBy, SimpleIdentifiableEntity(rs.getUuid("auditException_audit_id")), "auditException_")
               elements.add(currentParentEntity)
               currentParentEntity
            } else {
               currentParentEntity!!
            }

            val enteredBy = mapEmployee(rs, address, "auditExceptionNoteEmployee_")

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

      while (result.elements.isNotEmpty()) {
         result.elements.forEach { auditException: AuditExceptionEntity ->
            callback(auditException, index % 2 == 0)
            index++
         }

         result = findAll(audit, auditCompany, result.requested.nextPage())
      }
   }

   @ReadOnly
   fun exists(id: UUID): Boolean {
      val exists = jdbc.queryForObject("SELECT EXISTS(SELECT id FROM audit_exception WHERE id = :id)", mapOf("id" to id), Boolean::class.java)

      logger.trace("Checking if AuditException: {} exists resulted in {}", id, exists)

      return exists
   }

   fun doesNotExist(id: UUID): Boolean = !exists(id)

   @Transactional
   fun insert(entity: AuditExceptionEntity): AuditExceptionEntity {
      logger.trace("Inserting audit_exception {}", entity)

      return jdbc.insertReturning(
         """
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
         )
      ) { rs, _ ->
         mapRow(rs, entity.scanArea, entity.scannedBy, entity.approvedBy, entity.audit, EMPTY)
      }
   }

   @Transactional
   fun approveAllExceptions(audit: AuditEntity, employee: User): Int {
      logger.trace("Updating audit_exception {}/{}", audit, employee)

      return jdbc.update(
         """
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
      logger.trace("Updating audit_exception {}", entity)

      jdbc.updateReturning(
         """
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
         )
      ) { rs, _ ->
         mapRow(rs, entity.scanArea, entity.scannedBy, entity.approvedBy, entity.audit, EMPTY)
      }

      val notes = entity.notes
         .asSequence()
         .map { auditExceptionNoteRepository.upsert(it) }
         .toMutableList()

      return entity.copy(notes = notes)
   }

   private fun mapRow(rs: ResultSet, scanArea: AuditScanAreaEntity?, scannedBy: EmployeeEntity, approvedBy: EmployeeEntity?, audit: Identifiable, columnPrefix: String): AuditExceptionEntity =
      AuditExceptionEntity(
         id = rs.getUuid("${columnPrefix}id"),
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

   private fun mapCompany(rs: ResultSet, address: AddressEntity?): CompanyEntity {
      return CompanyEntity(
         id = rs.getUuid("comp_id"),
         name = rs.getString("comp_name"),
         doingBusinessAs = rs.getString("comp_doing_business_as"),
         clientCode = rs.getString("comp_client_code"),
         clientId = rs.getInt("comp_client_id"),
         federalIdNumber = rs.getString("comp_federal_id_number"),
         includeDemoInventory = rs.getBoolean("comp_include_demo_inventory"),
         address = address,
         datasetCode = rs.getString("comp_dataset_code")
      )
   }

   private fun mapEmployeeNotNull(rs: ResultSet, address: AddressEntity?, columnPrefix: String): EmployeeEntity {
      val employeeId = rs.getLong("${columnPrefix}id")
      val employeeNumber = rs.getInt("${columnPrefix}number")
      val company = mapCompany(rs, address)
      val securityGroups = securityGroupRepository.findByEmployee(employeeId, employeeNumber, company.id!!)
      return EmployeeEntity(
         id = rs.getLong("${columnPrefix}id"),
         type = rs.getString("${columnPrefix}type"),
         number = rs.getInt("${columnPrefix}number"),
         company = mapCompany(rs, address),
         lastName = rs.getString("${columnPrefix}last_name"),
         firstNameMi = rs.getString("${columnPrefix}first_name_mi"),
         passCode = rs.getString("${columnPrefix}pass_code"),
         store = mapScannedByStore(rs, address, columnPrefix),
         active = rs.getBoolean("${columnPrefix}active"),
         department = mapScannedByDepartment(rs, address, columnPrefix),
         //cynergiSystemAdmin = rs.getBoolean("${columnPrefix}cynergi_system_admin"),
         alternativeStoreIndicator = rs.getString("${columnPrefix}alternative_store_indicator"),
         alternativeArea = rs.getLong("${columnPrefix}alternative_area"),
         securityGroups = securityGroups
      )
   }

   private fun mapEmployee(rs: ResultSet, address: AddressEntity?, columnPrefix: String): EmployeeEntity? {
      return if (rs.getString("${columnPrefix}id") != null) {
         mapEmployeeNotNull(rs, address, columnPrefix)
      } else {
         null
      }
   }

   private fun mapScannedByStore(rs: ResultSet, address: AddressEntity?, columnPrefix: String): Store? {
      return if (rs.getString("${columnPrefix}store_id") != null) {
         val storeEntity = StoreEntity(
            id = rs.getLong("${columnPrefix}store_id"),
            number = rs.getInt("${columnPrefix}store_number"),
            name = rs.getString("${columnPrefix}store_name"),
            company = mapCompany(rs, address),
         )
         storeEntity
      } else {
         null
      }
   }

   private fun mapScannedByDepartment(rs: ResultSet, address: AddressEntity?, columnPrefix: String): DepartmentEntity? {
      return if (rs.getString("${columnPrefix}dept_id") != null) {
         DepartmentEntity(
            id = rs.getLong("${columnPrefix}dept_id"),
            code = rs.getString("${columnPrefix}dept_code"),
            description = rs.getString("${columnPrefix}dept_description"),
            company = mapCompany(rs, address)
         )
      } else {
         null
      }
   }

   private fun mapRowAuditExceptionNote(rs: ResultSet, enteredBy: EmployeeEntity): AuditExceptionNote? =
      if (rs.getString("auditExceptionNote_id") != null) {
         AuditExceptionNote(
            id = rs.getUuid("auditExceptionNote_id"),
            timeCreated = rs.getOffsetDateTime("auditExceptionNote_time_created"),
            timeUpdated = rs.getOffsetDateTime("auditExceptionNote_time_updated"),
            note = rs.getString("auditExceptionNote_note"),
            enteredBy = enteredBy,
            auditException = SimpleIdentifiableEntity(rs.getUuid("auditException_id"))
         )
      } else {
         null
      }
}
