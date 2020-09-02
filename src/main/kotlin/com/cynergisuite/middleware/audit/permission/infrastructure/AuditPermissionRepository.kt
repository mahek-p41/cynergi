package com.cynergisuite.middleware.audit.permission.infrastructure

import com.cynergisuite.domain.PageRequest
import com.cynergisuite.domain.infrastructure.RepositoryPage
import com.cynergisuite.extensions.deleteReturning
import com.cynergisuite.extensions.findFirstOrNull
import com.cynergisuite.extensions.insertReturning
import com.cynergisuite.extensions.queryPaged
import com.cynergisuite.extensions.updateReturning
import com.cynergisuite.middleware.audit.permission.AuditPermissionEntity
import com.cynergisuite.middleware.audit.permission.AuditPermissionType
import com.cynergisuite.middleware.company.Company
import com.cynergisuite.middleware.company.infrastructure.CompanyRepository
import com.cynergisuite.middleware.department.Department
import com.cynergisuite.middleware.department.infrastructure.DepartmentRepository
import org.eclipse.collections.impl.factory.Sets
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.sql.ResultSet
import javax.inject.Inject
import javax.inject.Singleton
import javax.transaction.Transactional

@Singleton
class AuditPermissionRepository @Inject constructor(
   private val companyRepository: CompanyRepository,
   private val departmentRepository: DepartmentRepository,
   private val jdbc: NamedParameterJdbcTemplate
) {
   private val logger: Logger = LoggerFactory.getLogger(AuditPermissionRepository::class.java)

   fun findById(id: Long, company: Company): AuditPermissionEntity? {
      logger.debug("Searching for AuditPermission with id {}/{}", id, company)

      val found = jdbc.findFirstOrNull(
         """
         WITH company AS (
            ${companyRepository.companyBaseQuery()}
         )
         SELECT
            ap.id                      AS ap_id,
            ap.uu_row_id               AS ap_uu_row_id,
            ap.time_created            AS ap_time_created,
            ap.time_updated            AS ap_time_updated,
            aptd.id                    AS aptd_id,
            aptd.value                 AS aptd_value,
            aptd.description           AS aptd_description,
            aptd.localization_code     AS aptd_localization_code,
            comp.id                    AS comp_id,
            comp.uu_row_id             AS comp_uu_row_id,
            comp.time_created          AS comp_time_created,
            comp.time_updated          AS comp_time_updated,
            comp.name                  AS comp_name,
            comp.doing_business_as     AS comp_doing_business_as,
            comp.client_code           AS comp_client_code,
            comp.client_id             AS comp_client_id,
            comp.dataset_code          AS comp_dataset_code,
            comp.federal_id_number     AS comp_federal_id_number,
            comp.address_id            AS address_id,
            comp.address_name          AS address_name,
            comp.address_address1      AS address_address1,
            comp.address_address2      AS address_address2,
            comp.address_city          AS address_city,
            comp.address_state         AS address_state,
            comp.address_postal_code   AS address_postal_code,
            comp.address_latitude      AS address_latitude,
            comp.address_longitude     AS address_longitude,
            comp.address_country       AS address_country,
            comp.address_county        AS address_county,
            comp.address_phone         AS address_phone,
            comp.address_fax           AS address_fax,
            dept.id                    AS dept_id,
            dept.code                  AS dept_code,
            dept.description           AS dept_description,
            dept.dataset               AS dept_dataset
         FROM audit_permission ap
              JOIN company comp ON ap.company_id = comp.id
              JOIN audit_permission_type_domain aptd ON ap.type_id = aptd.id
              JOIN fastinfo_prod_import.department_vw dept ON ap.department = dept.code AND comp.dataset_code = dept.dataset
         WHERE ap.id = :ap_id
               AND comp.id = :comp_id""",
         mapOf("ap_id" to id, "comp_id" to company.myId()),
         RowMapper { rs, _ ->
            processFindRow(rs, company)
         }
      )

      logger.trace("Searching for AuditPermission with id {} resulted in {}", id, found)

      return found
   }

   fun findAll(pageRequest: PageRequest, company: Company): RepositoryPage<AuditPermissionEntity, PageRequest> {
      logger.debug("Finding all Audit Permissions using {} and dataset: {}", pageRequest, company)

      return jdbc.queryPaged(
         """
         WITH company AS (
            ${companyRepository.companyBaseQuery()}
         )
         SELECT
            ap.id                         AS ap_id,
            ap.uu_row_id                  AS ap_uu_row_id,
            ap.time_created               AS ap_time_created,
            ap.time_updated               AS ap_time_updated,
            aptd.id                       AS aptd_id,
            aptd.value                    AS aptd_value,
            aptd.description              AS aptd_description,
            aptd.localization_code        AS aptd_localization_code,
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
            comp.address_id               AS comp_address_id,
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
            dept.id                       AS dept_id,
            dept.code                     AS dept_code,
            dept.description              AS dept_description,
            dept.dataset                  AS dept_dataset,
            count(*) OVER() as total_elements
         FROM audit_permission ap
              JOIN company comp ON ap.company_id = comp.id
              JOIN audit_permission_type_domain aptd ON ap.type_id = aptd.id
              JOIN fastinfo_prod_import.department_vw dept ON ap.department = dept.code AND comp.dataset_code = dept.dataset
         WHERE comp.id = :comp_id
         ORDER BY ap_${pageRequest.snakeSortBy()} ${pageRequest.sortDirection()}
         LIMIT :limit OFFSET :offset""",
         mapOf(
            "comp_id" to company.myId(),
            "limit" to pageRequest.size(),
            "offset" to pageRequest.offset()
         ),
         pageRequest
      ) { rs, elements ->
         processFindAllRows(elements, rs, company)
      }
   }

   fun findAllByType(pageRequest: PageRequest, company: Company, typeId: Long): RepositoryPage<AuditPermissionEntity, PageRequest> {
      logger.debug("Finding all Audit Permissions of a typeId {} using {} and company: {}", typeId, pageRequest, company)

      return jdbc.queryPaged(
         """
         WITH company AS (
            ${companyRepository.companyBaseQuery()}
         )
         SELECT
            ap.id                         AS ap_id,
            ap.uu_row_id                  AS ap_uu_row_id,
            ap.time_created               AS ap_time_created,
            ap.time_updated               AS ap_time_updated,
            aptd.id                       AS aptd_id,
            aptd.value                    AS aptd_value,
            aptd.description              AS aptd_description,
            aptd.localization_code        AS aptd_localization_code,
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
            comp.address_id               AS comp_address_id,
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
            dept.id                       AS dept_id,
            dept.code                     AS dept_code,
            dept.description              AS dept_description,
            dept.dataset                  AS dept_dataset,
            count(*) OVER() as total_elements
         FROM audit_permission ap
            JOIN company comp ON ap.company_id = comp.id
            JOIN audit_permission_type_domain aptd ON ap.type_id = aptd.id
            JOIN fastinfo_prod_import.department_vw dept ON ap.department = dept.code AND comp.dataset_code = dept.dataset
         WHERE comp.id = :comp_id AND ap.type_id = :typeId
         ORDER BY ap_${pageRequest.snakeSortBy()} ${pageRequest.sortDirection()}
         LIMIT :limit OFFSET :offset""",
         mapOf(
            "comp_id" to company.myId(),
            "typeId" to typeId,
            "limit" to pageRequest.size(),
            "offset" to pageRequest.offset()
         ),
         pageRequest
      ) { rs, elements ->
         processFindAllRows(elements, rs, company)
      }
   }

   private fun processFindAllRows(elements: MutableList<AuditPermissionEntity>, rs: ResultSet, company: Company) {
      do {
         elements.add(
            processFindRow(rs, company)
         )
      } while (rs.next())
   }

   fun permissionDepartmentByAsset(asset: String, company: Company): Set<Department> {
      logger.debug("Searching for AuditPermission with asset {}/{}", asset, company)
      val departments = Sets.mutable.empty<Department>()

      jdbc.query(
         """
         SELECT
            dept.id                 AS dept_id,
            dept.code               AS dept_code,
            dept.description        AS dept_description,
            dept.dataset            AS dept_dataset
         FROM audit_permission ap
              JOIN company comp ON ap.company_id = comp.id
              JOIN audit_permission_type_domain aptd ON ap.type_id = aptd.id
              JOIN fastinfo_prod_import.department_vw dept ON ap.department = dept.code AND comp.dataset_code = dept.dataset
         WHERE aptd.value = :asset
               AND comp.id = :comp_id""",
         mapOf("asset" to asset, "comp_id" to company.myId())
      ) { rs ->
         val dept = departmentRepository.mapRow(rs, company, "dept_")

         departments.add(dept)
      }

      logger.trace("Searching for AuditPermission with asset {} resulted in {}", asset, departments)

      return departments
   }

   private fun processFindRow(rs: ResultSet, company: Company): AuditPermissionEntity {
      return AuditPermissionEntity(
         id = rs.getLong("ap_id"),
         type = AuditPermissionType(
            id = rs.getLong("aptd_id"),
            value = rs.getString("aptd_value"),
            description = rs.getString("aptd_description"),
            localizationCode = rs.getString("aptd_localization_code")
         ),
         department = departmentRepository.mapRow(rs, company, "dept_")
      )
   }

   fun findAllPermissionTypes(pageRequest: PageRequest): RepositoryPage<AuditPermissionType, PageRequest> {
      logger.debug("Finding audit permissions using page {}", pageRequest)

      return jdbc.queryPaged(
         """
         SELECT
            id,
            value,
            description,
            localization_code,
            count(*) OVER() as total_elements
         FROM audit_permission_type_domain
         ORDER BY ${pageRequest.snakeSortBy()} ${pageRequest.sortDirection()}
         LIMIT :limit OFFSET :offset
         """,
         mapOf(
            "limit" to pageRequest.size(),
            "offset" to pageRequest.offset()
         ),
         pageRequest
      ) { rs, elements ->
         do {
            elements.add(
               AuditPermissionType(
                  id = rs.getLong("id"),
                  value = rs.getString("value"),
                  description = rs.getString("description"),
                  localizationCode = rs.getString("localization_code")
               )
            )
         } while (rs.next())
      }
   }

   @Transactional
   fun insert(auditPermission: AuditPermissionEntity): AuditPermissionEntity {
      logger.debug("Inserting AuditPermission {}", auditPermission)

      return jdbc.insertReturning(
         """
         INSERT INTO audit_permission(department, type_id, company_id)
         VALUES (:department, :type_id, :company_id)
         RETURNING
            *""",
         mapOf(
            "department" to auditPermission.department.code,
            "type_id" to auditPermission.type.id,
            "company_id" to auditPermission.department.company.myId()
         ),
         RowMapper { rs, _ ->
            AuditPermissionEntity(
               id = rs.getLong("id"),
               department = auditPermission.department.copy(),
               type = auditPermission.type.copy()
            )
         }
      )
   }

   @Transactional
   fun update(auditPermission: AuditPermissionEntity, company: Company): AuditPermissionEntity {
      logger.debug("Updating AuditPermission {}", auditPermission)

      return jdbc.updateReturning(
         """
         UPDATE audit_permission
         SET
            department = :department,
            type_id = :type_id,
            company_id = :company_id
         WHERE id = :id
         RETURNING
            *""",
         mapOf(
            "id" to auditPermission.id,
            "department" to auditPermission.department.code,
            "type_id" to auditPermission.type.id,
            "company_id" to company.myId()
         ),
         RowMapper { rs, _ ->
            AuditPermissionEntity(
               id = rs.getLong("id"),
               department = auditPermission.department.copy(),
               type = auditPermission.type.copy()
            )
         }
      )
   }

   @Transactional
   fun deleteById(id: Long, company: Company): AuditPermissionEntity? {
      logger.debug("Deleting AuditPermission using {}/{}", id, company)

      val existingPermission = findById(id, company)

      return if (existingPermission != null) {
         jdbc.deleteReturning(
            """
            DELETE FROM audit_permission
            WHERE id = :id
            RETURNING
               *""",
            mapOf("id" to id),
            RowMapper { rs, _ ->
               AuditPermissionEntity(
                  id = rs.getLong("id"),
                  department = existingPermission.department,
                  type = existingPermission.type
               )
            }
         )
      } else {
         null
      }
   }
}
