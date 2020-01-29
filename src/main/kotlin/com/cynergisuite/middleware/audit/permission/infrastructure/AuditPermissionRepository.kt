package com.cynergisuite.middleware.audit.permission.infrastructure

import com.cynergisuite.extensions.findFirstOrNull
import com.cynergisuite.extensions.getOffsetDateTime
import com.cynergisuite.extensions.getUuid
import com.cynergisuite.middleware.audit.permission.AuditPermission
import com.cynergisuite.middleware.audit.permission.AuditPermissionType
import com.cynergisuite.middleware.company.infrastructure.CompanyRepository
import com.cynergisuite.middleware.department.infrastructure.DepartmentRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuditPermissionRepository @Inject constructor(
   private val companyRepository: CompanyRepository,
   private val departmentRepository: DepartmentRepository,
   private val jdbc: NamedParameterJdbcTemplate
) {
   private val logger: Logger = LoggerFactory.getLogger(AuditPermissionRepository::class.java)

   fun findOneByAsset(asset: String): AuditPermission? {
      logger.debug("Searching for AuditPermission with asset {}", asset)

      val found = jdbc.findFirstOrNull("""
         SELECT
            ap.id                   AS ap_id,
            ap.uu_row_id            AS ap_uu_row_id,
            ap.time_created         AS ap_time_created,
            ap.time_updated         AS ap_time_updated,
            aptd.id                 AS aptd_id,
            aptd.value              AS aptd_value,
            aptd.description        AS aptd_description,
            aptd.localization_code  AS aptd_localization_code,
            comp.id                 AS comp_id,
            comp.uu_row_id          AS comp_uu_row_id,
            comp.name               AS comp_name,
            comp.doing_business_as  AS comp_doing_business_as,
            comp.client_code        AS comp_client_code,
            comp.client_id          AS comp_client_id,
            comp.dataset_code       AS comp_dataset_code,
            comp.federal_tax_number AS comp_federal_tax_number,
            dept.id                 AS dept_id,
            dept.code               AS dept_code,
            dept.description        AS dept_description,
            dept.dataset            AS dept_dataset,
            dept.security_profile   AS dept_security_profile,
            dept.default_menu       AS dept_default_menu,
            dept.time_created       AS dept_time_created,
            dept.time_updated       AS dept_time_updated
         FROM audit_permission ap
            JOIN audit_permission_type_domain aptd ON ap.audit_permission_type_id = aptd.id
            JOIN company comp ON ap.company_id = comp.id
            JOIN fastinfo_prod_import.department_vw dept ON ap.department = dept.code
         WHERE aptd.value = :asset""",
         mapOf("asset" to asset),
         RowMapper { rs, _ ->
            AuditPermission(
               id = rs.getLong("ap_id"),
               uuRowId = rs.getUuid("ap_uu_row_id"),
               timeCreated = rs.getOffsetDateTime("ap_time_created"),
               timeUpdated = rs.getOffsetDateTime("ap_time_updated"),
               auditPermissionType = AuditPermissionType(
                  id = rs.getLong("aptd_id"),
                  value = rs.getString("aptd_value"),
                  description = rs.getString("aptd_description"),
                  localizationCode = rs.getString("aptd_localization_code")
               ),
               company = companyRepository.mapRow(rs, "comp_"),
               department = departmentRepository.mapRow(rs, "dept_")
            )
         }
      )

      logger.trace("Searching for AuditPermission with asset {} resulted in {}", asset, found)

      return found
   }
}
