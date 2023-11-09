package com.cynergisuite.middleware.authentication.user.infrastructure

import com.cynergisuite.extensions.findFirstOrNull
import com.cynergisuite.extensions.getIntOrNull
import com.cynergisuite.extensions.query
import com.cynergisuite.middleware.authentication.user.AuthenticatedEmployee
import com.cynergisuite.middleware.authentication.user.User
import com.cynergisuite.middleware.authentication.user.UserSecurityLevels
import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.company.infrastructure.CompanyRepository
import com.cynergisuite.middleware.department.Department
import com.cynergisuite.middleware.employee.infrastructure.EmployeeRepository
import com.cynergisuite.middleware.location.infrastructure.LocationRepository
import io.micronaut.cache.annotation.Cacheable
import io.micronaut.data.annotation.Join
import io.micronaut.data.annotation.Query
import io.micronaut.data.annotation.repeatable.JoinSpecifications
import io.micronaut.data.jdbc.annotation.JdbcRepository
import io.micronaut.data.repository.CrudRepository
import io.micronaut.transaction.annotation.ReadOnly
import jakarta.inject.Inject
import org.jdbi.v3.core.Jdbi
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.sql.ResultSet
import java.util.UUID

@JdbcRepository
abstract class AuthenticationRepository @Inject constructor(
   private val companyRepository: CompanyRepository,
   private val employeeRepository: EmployeeRepository,
   private val locationRepository: LocationRepository,
   private val securityGroupRepository: SecurityGroupRepository,
   private val jdbi: Jdbi,
) : CrudRepository<AuthenticatedEmployee, Long> {
   private val logger: Logger = LoggerFactory.getLogger(AuthenticationRepository::class.java)

   @Query(
      """
      SELECT
         au.id                          AS id,
         au.type                        AS type,
         au.number                      AS number,
         au.pass_code                   AS pass_code,
         au.alternative_store_indicator AS alternative_store_indicator,
         au.alternative_area            AS alternative_area,
         au.store_number                AS store_number,
         comp.id                        AS company_id,
         comp.name                      AS company_name,
         comp.doing_business_as         AS company_doing_business_as,
         comp.client_code               AS company_client_code,
         comp.client_id                 AS company_client_id,
         comp.dataset_code              AS company_dataset_code,
         comp.federal_id_number         AS company_federal_id_number,
         compAddr.id                    AS company_address_id,
         compAddr.number                AS company_address_number,
         compAddr.name                  AS company_address_name,
         compAddr.address1              AS company_address_address1,
         compAddr.address2              AS company_address_address2,
         compAddr.city                  AS company_address_city,
         compAddr.state                 AS company_address_state,
         compAddr.postal_code           AS company_address_postal_code,
         compAddr.latitude              AS company_address_latitude,
         compAddr.longitude             AS company_address_longitude,
         compAddr.country               AS company_address_country,
         compAddr.county                AS company_address_county,
         compAddr.phone                 AS company_address_phone,
         compAddr.fax                   AS company_address_fax,
         dept.id                        AS department_id,
         dept.code                      AS department_code,
         dept.description               AS department_description,
         comp.id                        AS department_company_id,
         comp.name                      AS department_company_name,
         comp.doing_business_as         AS department_company_doing_business_as,
         comp.client_code               AS department_company_client_code,
         comp.client_id                 AS department_company_client_id,
         comp.dataset_code              AS department_company_dataset_code,
         comp.federal_id_number         AS department_company_federal_id_number,
         compAddr.id                    AS department_company_address_id,
         compAddr.number                AS department_company_address_number,
         compAddr.name                  AS department_company_address_name,
         compAddr.address1              AS department_company_address_address1,
         compAddr.address2              AS department_company_address_address2,
         compAddr.city                  AS department_company_address_city,
         compAddr.state                 AS department_company_address_state,
         compAddr.postal_code           AS department_company_address_postal_code,
         compAddr.latitude              AS department_company_address_latitude,
         compAddr.longitude             AS department_company_address_longitude,
         compAddr.country               AS department_company_address_country,
         compAddr.county                AS department_company_address_county,
         compAddr.phone                 AS department_company_address_phone,
         compAddr.fax                   AS department_company_address_fax,
         assignedLoc.id                 AS assigned_location_id,
         assignedLoc.number             AS assigned_location_number,
         assignedLoc.name               AS assigned_location_name,
         comp.id                        AS assigned_location_company_id,
         comp.name                      AS assigned_location_company_name,
         comp.doing_business_as         AS assigned_location_company_doing_business_as,
         comp.client_code               AS assigned_location_company_client_code,
         comp.client_id                 AS assigned_location_company_client_id,
         comp.dataset_code              AS assigned_location_company_dataset_code,
         comp.federal_id_number         AS assigned_location_company_federal_id_number,
         compAddr.id                    AS assigned_location_company_address_id,
         compAddr.number                AS assigned_location_company_address_number,
         compAddr.name                  AS assigned_location_company_address_name,
         compAddr.address1              AS assigned_location_company_address_address1,
         compAddr.address2              AS assigned_location_company_address_address2,
         compAddr.city                  AS assigned_location_company_address_city,
         compAddr.state                 AS assigned_location_company_address_state,
         compAddr.postal_code           AS assigned_location_company_address_postal_code,
         compAddr.latitude              AS assigned_location_company_address_latitude,
         compAddr.longitude             AS assigned_location_company_address_longitude,
         compAddr.country               AS assigned_location_company_address_country,
         compAddr.county                AS assigned_location_company_address_county,
         compAddr.phone                 AS assigned_location_company_address_phone,
         compAddr.fax                   AS assigned_location_company_address_fax,
         chosenLoc.id                   AS chosen_location_id,
         chosenLoc.number               AS chosen_location_number,
         chosenLoc.name                 AS chosen_location_name,
         comp.id                        AS chosen_location_company_id,
         comp.name                      AS chosen_location_company_name,
         comp.doing_business_as         AS chosen_location_company_doing_business_as,
         comp.client_code               AS chosen_location_company_client_code,
         comp.client_id                 AS chosen_location_company_client_id,
         comp.dataset_code              AS chosen_location_company_dataset_code,
         comp.federal_id_number         AS chosen_location_company_federal_id_number,
         compAddr.id                    AS chosen_location_company_address_id,
         compAddr.number                AS chosen_location_company_address_number,
         compAddr.name                  AS chosen_location_company_address_name,
         compAddr.address1              AS chosen_location_company_address_address1,
         compAddr.address2              AS chosen_location_company_address_address2,
         compAddr.city                  AS chosen_location_company_address_city,
         compAddr.state                 AS chosen_location_company_address_state,
         compAddr.postal_code           AS chosen_location_company_address_postal_code,
         compAddr.latitude              AS chosen_location_company_address_latitude,
         compAddr.longitude             AS chosen_location_company_address_longitude,
         compAddr.country               AS chosen_location_company_address_country,
         compAddr.county                AS chosen_location_company_address_county,
         compAddr.phone                 AS chosen_location_company_address_phone,
         compAddr.fax                   AS chosen_location_company_address_fax,
         fallbackLoc.id                 AS fallback_location_id,
         fallbackLoc.number             AS fallback_location_number,
         fallbackLoc.name               AS fallback_location_name,
         secgrp.id                      AS security_groups_id,
         secgrp.value                   AS security_groups_value,
         secgrp.description             AS security_groups_description,
         sapd.id                        AS security_groups_types_id,
         sapd.value                     AS security_groups_types_value,
         sapd.description               AS security_groups_types_description,
         sapd.localization_code         AS security_groups_types_localization_code,
         sapd.area_id                   AS security_groups_types_area_id,
         comp.id                        AS security_groups_company_id,
         comp.name                      AS security_groups_company_name,
         comp.doing_business_as         AS security_groups_company_doing_business_as,
         comp.client_code               AS security_groups_company_client_code,
         comp.client_id                 AS security_groups_company_client_id,
         comp.dataset_code              AS security_groups_company_dataset_code,
         comp.federal_id_number         AS security_groups_company_federal_id_number,
         compAddr.id                    AS security_groups_company_address_id,
         compAddr.number                AS security_groups_company_address_number,
         compAddr.name                  AS security_groups_company_address_name,
         compAddr.address1              AS security_groups_company_address_address1,
         compAddr.address2              AS security_groups_company_address_address2,
         compAddr.city                  AS security_groups_company_address_city,
         compAddr.state                 AS security_groups_company_address_state,
         compAddr.postal_code           AS security_groups_company_address_postal_code,
         compAddr.latitude              AS security_groups_company_address_latitude,
         compAddr.longitude             AS security_groups_company_address_longitude,
         compAddr.country               AS security_groups_company_address_country,
         compAddr.county                AS security_groups_company_address_county,
         compAddr.phone                 AS security_groups_company_address_phone,
         compAddr.fax                   AS security_groups_company_address_fax
      FROM authenticated_user_vw au
           JOIN company comp ON comp.id = au.company_id AND comp.deleted = FALSE
           LEFT JOIN address compAddr on comp.address_id = compAddr.id AND compAddr.deleted = FALSE
           LEFT OUTER JOIN fastinfo_prod_import.department_vw dept ON comp.dataset_code = dept.dataset AND au.department = dept.code
           LEFT OUTER JOIN system_stores_fimvw assignedLoc ON comp.dataset_code = assignedLoc.dataset AND au.store_number = assignedLoc.number
           LEFT OUTER JOIN system_stores_fimvw chosenLoc ON comp.dataset_code = chosenLoc.dataset AND chosenLoc.number  = :storeNumber
           JOIN system_stores_fimvw fallbackLoc ON comp.dataset_code = fallbackLoc.dataset AND fallbackLoc.number = (SELECT coalesce(max(store_number), 9000) FROM fastinfo_prod_import.employee_vw WHERE dataset = comp.dataset_code)
           LEFT OUTER JOIN (
             select esg.employee_id_sfk, sg.id as security_group_id,
             sg.value as security_group_value,
             sg.description as security_group_description
           from employee_to_security_group esg
           join security_group sg on esg.security_group_id = sg.id AND esg.emp_number = :employeeNumber
           join company c on sg.company_id = c.id
           where c.dataset_code = :dataset
           ) filteredSecurityGroups on au.id = filteredSecurityGroups.employee_id_sfk
           LEFT OUTER JOIN security_group secgrp on filteredSecurityGroups.security_group_id = secgrp.id and secgrp.deleted = FALSE
           LEFT JOIN security_group_to_security_access_point sgsap ON sgsap.security_group_id = secgrp.id
		     LEFT JOIN security_access_point_type_domain sapd ON sgsap.security_access_point_id = sapd.id
      WHERE comp.dataset_code = :dataset
            AND au.number = :employeeNumber
            AND au.pass_code = convert_passcode(au.type, :passCode, au.pass_code)
   """,
      nativeQuery = true
   )
   @JoinSpecifications(
      Join("company"), // when defining these Join's the pathing should match the Java/Kotlin camelCase property access path not the '_' snake_case access path used in the associated SQL.  Micronaut Data will handle translating this
      Join("company.address"),
      Join("department"),
      Join("department.company"),
      Join("department.company.address"),
      Join("assignedLocation"),
      Join("assignedLocation.company"),
      Join("assignedLocation.company.address"),
      Join("chosenLocation"),
      Join("chosenLocation.company"),
      Join("chosenLocation.company.address"),
      Join("fallbackLocation"),
      Join("fallbackLocation.company"),
      Join("fallbackLocation.company.address"),
      Join("securityGroups"),
      Join("securityGroups.types"),
      Join("securityGroups.company")
   )
   abstract fun findUserByAuthenticationWithStore(employeeNumber: Int, passCode: String, dataset: String, storeNumber: Int): AuthenticatedEmployee?

   @Query(
      """
      SELECT
         au.id                          AS id,
         au.type                        AS type,
         au.number                      AS number,
         au.pass_code                   AS pass_code,
         au.alternative_store_indicator AS alternative_store_indicator,
         au.alternative_area            AS alternative_area,
         au.store_number                AS store_number,
         comp.id                        AS company_id,
         comp.name                      AS company_name,
         comp.doing_business_as         AS company_doing_business_as,
         comp.client_code               AS company_client_code,
         comp.client_id                 AS company_client_id,
         comp.dataset_code              AS company_dataset_code,
         comp.federal_id_number         AS company_federal_id_number,
         compAddr.id                    AS company_address_id,
         compAddr.number                AS company_address_number,
         compAddr.name                  AS company_address_name,
         compAddr.address1              AS company_address_address1,
         compAddr.address2              AS company_address_address2,
         compAddr.city                  AS company_address_city,
         compAddr.state                 AS company_address_state,
         compAddr.postal_code           AS company_address_postal_code,
         compAddr.latitude              AS company_address_latitude,
         compAddr.longitude             AS company_address_longitude,
         compAddr.country               AS company_address_country,
         compAddr.county                AS company_address_county,
         compAddr.phone                 AS company_address_phone,
         compAddr.fax                   AS company_address_fax,
         dept.id                        AS department_id,
         dept.code                      AS department_code,
         dept.description               AS department_description,
         comp.id                        AS department_company_id,
         comp.name                      AS department_company_name,
         comp.doing_business_as         AS department_company_doing_business_as,
         comp.client_code               AS department_company_client_code,
         comp.client_id                 AS department_company_client_id,
         comp.dataset_code              AS department_company_dataset_code,
         comp.federal_id_number         AS department_company_federal_id_number,
         compAddr.id                    AS department_company_address_id,
         compAddr.number                AS department_company_address_number,
         compAddr.name                  AS department_company_address_name,
         compAddr.address1              AS department_company_address_address1,
         compAddr.address2              AS department_company_address_address2,
         compAddr.city                  AS department_company_address_city,
         compAddr.state                 AS department_company_address_state,
         compAddr.postal_code           AS department_company_address_postal_code,
         compAddr.latitude              AS department_company_address_latitude,
         compAddr.longitude             AS department_company_address_longitude,
         compAddr.country               AS department_company_address_country,
         compAddr.county                AS department_company_address_county,
         compAddr.phone                 AS department_company_address_phone,
         compAddr.fax                   AS department_company_address_fax,
         assignedLoc.id                 AS assigned_location_id,
         assignedLoc.number             AS assigned_location_number,
         assignedLoc.name               AS assigned_location_name,
         comp.id                        AS assigned_location_company_id,
         comp.name                      AS assigned_location_company_name,
         comp.doing_business_as         AS assigned_location_company_doing_business_as,
         comp.client_code               AS assigned_location_company_client_code,
         comp.client_id                 AS assigned_location_company_client_id,
         comp.dataset_code              AS assigned_location_company_dataset_code,
         comp.federal_id_number         AS assigned_location_company_federal_id_number,
         compAddr.id                    AS assigned_location_company_address_id,
         compAddr.number                AS assigned_location_company_address_number,
         compAddr.name                  AS assigned_location_company_address_name,
         compAddr.address1              AS assigned_location_company_address_address1,
         compAddr.address2              AS assigned_location_company_address_address2,
         compAddr.city                  AS assigned_location_company_address_city,
         compAddr.state                 AS assigned_location_company_address_state,
         compAddr.postal_code           AS assigned_location_company_address_postal_code,
         compAddr.latitude              AS assigned_location_company_address_latitude,
         compAddr.longitude             AS assigned_location_company_address_longitude,
         compAddr.country               AS assigned_location_company_address_country,
         compAddr.county                AS assigned_location_company_address_county,
         compAddr.phone                 AS assigned_location_company_address_phone,
         compAddr.fax                   AS assigned_location_company_address_fax,
         chosenLoc.id                   AS chosen_location_id,
         chosenLoc.number               AS chosen_location_number,
         chosenLoc.name                 AS chosen_location_name,
         comp.id                        AS chosen_location_company_id,
         comp.name                      AS chosen_location_company_name,
         comp.doing_business_as         AS chosen_location_company_doing_business_as,
         comp.client_code               AS chosen_location_company_client_code,
         comp.client_id                 AS chosen_location_company_client_id,
         comp.dataset_code              AS chosen_location_company_dataset_code,
         comp.federal_id_number         AS chosen_location_company_federal_id_number,
         compAddr.id                    AS chosen_location_company_address_id,
         compAddr.number                AS chosen_location_company_address_number,
         compAddr.name                  AS chosen_location_company_address_name,
         compAddr.address1              AS chosen_location_company_address_address1,
         compAddr.address2              AS chosen_location_company_address_address2,
         compAddr.city                  AS chosen_location_company_address_city,
         compAddr.state                 AS chosen_location_company_address_state,
         compAddr.postal_code           AS chosen_location_company_address_postal_code,
         compAddr.latitude              AS chosen_location_company_address_latitude,
         compAddr.longitude             AS chosen_location_company_address_longitude,
         compAddr.country               AS chosen_location_company_address_country,
         compAddr.county                AS chosen_location_company_address_county,
         compAddr.phone                 AS chosen_location_company_address_phone,
         compAddr.fax                   AS chosen_location_company_address_fax,
         fallbackLoc.id                 AS fallback_location_id,
         fallbackLoc.number             AS fallback_location_number,
         fallbackLoc.name               AS fallback_location_name,
         comp.id                        AS fallback_location_company_id,
         comp.name                      AS fallback_location_company_name,
         comp.doing_business_as         AS fallback_location_company_doing_business_as,
         comp.client_code               AS fallback_location_company_client_code,
         comp.client_id                 AS fallback_location_company_client_id,
         comp.dataset_code              AS fallback_location_company_dataset_code,
         comp.federal_id_number         AS fallback_location_company_federal_id_number,
         compAddr.id                    AS fallback_location_company_address_id,
         compAddr.number                AS fallback_location_company_address_number,
         compAddr.name                  AS fallback_location_company_address_name,
         compAddr.address1              AS fallback_location_company_address_address1,
         compAddr.address2              AS fallback_location_company_address_address2,
         compAddr.city                  AS fallback_location_company_address_city,
         compAddr.state                 AS fallback_location_company_address_state,
         compAddr.postal_code           AS fallback_location_company_address_postal_code,
         compAddr.latitude              AS fallback_location_company_address_latitude,
         compAddr.longitude             AS fallback_location_company_address_longitude,
         compAddr.country               AS fallback_location_company_address_country,
         compAddr.county                AS fallback_location_company_address_county,
         compAddr.phone                 AS fallback_location_company_address_phone,
         compAddr.fax                   AS fallback_location_company_address_fax,
         secgrp.id                      AS security_groups_id,
         secgrp.value                   AS security_groups_value,
         secgrp.description             AS security_groups_description,
         sapd.id                        AS security_groups_types_id,
         sapd.value                     AS security_groups_types_value,
         sapd.description               AS security_groups_types_description,
         sapd.localization_code         AS security_groups_types_localization_code,
         sapd.area_id                   AS security_groups_types_area_id,
         comp.id                        AS security_groups_company_id,
         comp.name                      AS security_groups_company_name,
         comp.doing_business_as         AS security_groups_company_doing_business_as,
         comp.client_code               AS security_groups_company_client_code,
         comp.client_id                 AS security_groups_company_client_id,
         comp.dataset_code              AS security_groups_company_dataset_code,
         comp.federal_id_number         AS security_groups_company_federal_id_number,
         compAddr.id                    AS security_groups_company_address_id,
         compAddr.number                AS security_groups_company_address_number,
         compAddr.name                  AS security_groups_company_address_name,
         compAddr.address1              AS security_groups_company_address_address1,
         compAddr.address2              AS security_groups_company_address_address2,
         compAddr.city                  AS security_groups_company_address_city,
         compAddr.state                 AS security_groups_company_address_state,
         compAddr.postal_code           AS security_groups_company_address_postal_code,
         compAddr.latitude              AS security_groups_company_address_latitude,
         compAddr.longitude             AS security_groups_company_address_longitude,
         compAddr.country               AS security_groups_company_address_country,
         compAddr.county                AS security_groups_company_address_county,
         compAddr.phone                 AS security_groups_company_address_phone,
         compAddr.fax                   AS security_groups_company_address_fax
      FROM authenticated_user_vw au
           JOIN company comp ON comp.id = au.company_id AND comp.deleted = FALSE
           LEFT JOIN address compAddr on comp.address_id = compAddr.id AND compAddr.deleted = FALSE
           LEFT OUTER JOIN fastinfo_prod_import.department_vw dept ON comp.dataset_code = dept.dataset AND au.department = dept.code
           LEFT OUTER JOIN system_stores_fimvw assignedLoc ON comp.dataset_code = assignedLoc.dataset AND au.store_number = assignedLoc.number
           LEFT OUTER JOIN system_stores_fimvw chosenLoc ON comp.dataset_code = chosenLoc.dataset AND chosenLoc.number IS NULL
           JOIN system_stores_fimvw fallbackLoc ON comp.dataset_code = fallbackLoc.dataset AND fallbackLoc.number = (SELECT coalesce(max(store_number), 9000) FROM fastinfo_prod_import.employee_vw WHERE dataset = comp.dataset_code)
           LEFT OUTER JOIN (
             select esg.employee_id_sfk, sg.id as security_group_id,
             sg.value as security_group_value,
             sg.description as security_group_description
           from employee_to_security_group esg
           join security_group sg on esg.security_group_id = sg.id AND esg.emp_number = :employeeNumber
           join company c on sg.company_id = c.id
           where c.dataset_code = :dataset
           ) filteredSecurityGroups on au.id = filteredSecurityGroups.employee_id_sfk
           LEFT OUTER JOIN security_group secgrp on filteredSecurityGroups.security_group_id = secgrp.id and secgrp.deleted = FALSE
           LEFT JOIN security_group_to_security_access_point sgsap ON sgsap.security_group_id = secgrp.id
		     LEFT JOIN security_access_point_type_domain sapd ON sgsap.security_access_point_id = sapd.id
      WHERE comp.dataset_code = :dataset
            AND au.number = :employeeNumber
            AND au.pass_code = convert_passcode(au.type, :passCode, au.pass_code)
   """,
      nativeQuery = true
   )
   @JoinSpecifications(
      Join("company"), // when defining these Join's the pathing should match the Java/Kotlin camelCase property access path not the '_' snake_case access path used in the associated SQL.  Micronaut Data will handle translating this
      Join("company.address"),
      Join("department"),
      Join("department.company"),
      Join("department.company.address"),
      Join("assignedLocation"),
      Join("assignedLocation.company"),
      Join("assignedLocation.company.address"),
      Join("chosenLocation"),
      Join("chosenLocation.company"),
      Join("chosenLocation.company.address"),
      Join("fallbackLocation"),
      Join("fallbackLocation.company"),
      Join("fallbackLocation.company.address"),
      Join("securityGroups"),
      Join("securityGroups.types"),
      Join("securityGroups.company")
   )
   abstract fun findUserByAuthentication(employeeNumber: Int, passCode: String, dataset: String): AuthenticatedEmployee?

   @ReadOnly
   @Cacheable("creds-cache")
   fun findUser(employeeId: Long, employeeType: String, employeeNumber: Int, companyId: UUID, storeNumber: Int): AuthenticatedEmployee {
      val company = companyRepository.findOne(companyId) ?: throw Exception("Unable to find company")
      val employee = employeeRepository.findOne(employeeId, employeeType, company) ?: throw Exception("Unable to find employee")
      val location = locationRepository.findOne(storeNumber, company) ?: throw Exception("Unable to find store from authentication")
      val securityGroups = securityGroupRepository.findAll(employeeId, company.id!!)
      val department = employee.department

      return AuthenticatedEmployee(
         id = employeeId,
         type = employeeType,
         number = employeeNumber,
         company = company,
         department = department,
         assignedLocation = location,
         alternativeStoreIndicator = employee.alternativeStoreIndicator,
         alternativeArea = employee.alternativeArea,
      //   cynergiSystemAdmin = employee.cynergiSystemAdmin,
         chosenLocation = location,
         fallbackLocation = location,
         passCode = employee.passCode,
         securityGroups = securityGroups
      )
   }

   @ReadOnly
   fun findPermissions(department: Department): Set<String> {
      val params = mapOf("dept_code" to department.myCode(), "comp_id" to department.myCompany().id)
      val sql =
         """
         SELECT aptd.value AS value
         FROM audit_permission_type_domain aptd
              JOIN audit_permission ap ON ap.type_id = aptd.id AND ap.deleted = FALSE
         WHERE ap.department = :dept_code AND ap.company_id = :comp_id
         UNION
         SELECT aptd.value AS value
         FROM audit_permission_type_domain aptd
         WHERE aptd.id NOT IN (SELECT DISTINCT type_id FROM audit_permission WHERE deleted = FALSE)
         """.trimIndent()

      return processPermissionValues(sql, params)
   }

   @ReadOnly
   fun findAllPermissions(): Set<String> { // TODO look into solving cynergi_system_admin privileges some other way
      return processPermissionValues(
         """
         SELECT value from audit_permission_type_domain
         UNION
         SELECT 'cynergi-system-admin' AS value
         """.trimIndent(),
         emptyMap()
      )
   }

   @ReadOnly
   fun findUserSecurityLevels(user: User, company: CompanyEntity): UserSecurityLevels? {
      logger.debug("Get user security levels")

      val params = mutableMapOf<String, Any?>("emp_number" to user.myEmployeeNumber(), "dataset_code" to company.datasetCode)
      val sql =
         """
         SELECT
            opr.account_payable_security        AS emp_account_payable_security,
            opr.purchase_order_security         AS emp_purchase_order_security,
            opr.general_ledger_security         AS emp_general_ledger_security,
            opr.system_administration_security  AS emp_system_administration_security,
            opr.file_maintenance_security       AS emp_file_maintenance_security,
            opr.bank_reconciliation_security    AS emp_bank_reconciliation_security
         FROM
            fastinfo_prod_import.operator_vw opr
         WHERE
            opr.number = :emp_number AND opr.dataset = :dataset_code
         """.trimIndent()

      return jdbi.findFirstOrNull(
         sql,
         params
      ) { rs, _ ->
         mapUserSecurityLevels(rs)
      } ?: UserSecurityLevels(user.isCynergiAdmin())
   }

   private fun processPermissionValues(sql: String, params: Map<String, Any?>): Set<String> {
      logger.debug("Get permission {}\n{}", params, sql)

      val resultSet = mutableSetOf<String>()

      jdbi.query(sql, params) {
         rs, _ ->
         resultSet.add(rs.getString("value"))
      }

      return resultSet
   }

   private fun mapUserSecurityLevels(rs: ResultSet, columnPrefix: String? = "emp_"): UserSecurityLevels =
      UserSecurityLevels(
         accountPayableLevel = rs.getIntOrNull("${columnPrefix}account_payable_security"),
         purchaseOrderLevel = rs.getIntOrNull("${columnPrefix}purchase_order_security"),
         generalLedgerLevel = rs.getIntOrNull("${columnPrefix}general_ledger_security"),
         systemAdministrationLevel = rs.getIntOrNull("${columnPrefix}system_administration_security"),
         fileMaintenanceLevel = rs.getIntOrNull("${columnPrefix}file_maintenance_security"),
         bankReconciliationLevel = rs.getIntOrNull("${columnPrefix}bank_reconciliation_security")
      )
}
