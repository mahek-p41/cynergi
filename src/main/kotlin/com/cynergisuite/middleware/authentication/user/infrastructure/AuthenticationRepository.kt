package com.cynergisuite.middleware.authentication.user.infrastructure

import com.cynergisuite.extensions.query
import com.cynergisuite.middleware.authentication.user.AuthenticatedEmployee
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
import java.util.UUID

@JdbcRepository
abstract class AuthenticationRepository @Inject constructor(
   private val companyRepository: CompanyRepository,
   private val employeeRepository: EmployeeRepository,
   private val locationRepository: LocationRepository,
   private val jdbi: Jdbi,
) : CrudRepository<AuthenticatedEmployee, Long> {
   private val logger: Logger = LoggerFactory.getLogger(AuthenticationRepository::class.java)

   @Query(
      """
      SELECT
         au.id                          AS id,
         au.type                        AS type,
         au.number                      AS number,
         au.cynergi_system_admin        AS cynergi_system_admin,
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
         fallbackLoc.id                 AS fallback_location_id,
         fallbackLoc.number             AS fallback_location_number,
         fallbackLoc.name               AS fallback_location_name
      FROM authenticated_user_vw au
           JOIN company comp ON comp.id = au.company_id
           LEFT OUTER JOIN fastinfo_prod_import.department_vw dept ON comp.dataset_code = dept.dataset AND au.department = dept.code
           LEFT OUTER JOIN system_stores_fimvw assignedLoc ON comp.dataset_code = assignedLoc.dataset AND au.store_number = assignedLoc.number
           LEFT OUTER JOIN system_stores_fimvw chosenLoc ON comp.dataset_code = chosenLoc.dataset AND chosenLoc.number  = :storeNumber
           JOIN system_stores_fimvw fallbackLoc ON comp.dataset_code = fallbackLoc.dataset AND fallbackLoc.number = (SELECT coalesce(max(store_number), 9000) FROM system_employees_fimvw WHERE dataset = comp.dataset_code)
      WHERE comp.dataset_code = :dataset
            AND au.number = :employeeNumber
            AND au.pass_code = convert_passcode(au.type, :passCode, au.pass_code)
   """,
      nativeQuery = true
   )
   @JoinSpecifications(
      Join("company"), // when defining these Join's the pathing should match the Java/Kotlin camelCase property access path not the '_' snake_case access path used in the associated SQL.  Micronaut Data will handle translating this
      Join("department"),
      Join("department.company"),
      Join("assignedLocation"),
      Join("assignedLocation.company"),
      Join("chosenLocation"),
      Join("chosenLocation.company"),
      Join("fallbackLocation"),
      Join("fallbackLocation.company"),
   )
   abstract fun findUserByAuthenticationWithStore(employeeNumber: Int, passCode: String, dataset: String, storeNumber: Int): AuthenticatedEmployee?

   @Query(
      """
      SELECT
         au.id                          AS id,
         au.type                        AS type,
         au.number                      AS number,
         au.cynergi_system_admin        AS cynergi_system_admin,
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
         fallbackLoc.id                 AS fallback_location_id,
         fallbackLoc.number             AS fallback_location_number,
         fallbackLoc.name               AS fallback_location_name,
         comp.id                        AS fallback_location_company_id,
         comp.name                      AS fallback_location_company_name,
         comp.doing_business_as         AS fallback_location_company_doing_business_as,
         comp.client_code               AS fallback_location_company_client_code,
         comp.client_id                 AS fallback_location_company_client_id,
         comp.dataset_code              AS fallback_location_company_dataset_code,
         comp.federal_id_number         AS fallback_location_company_federal_id_number
      FROM authenticated_user_vw au
           JOIN company comp ON comp.id = au.company_id
           LEFT OUTER JOIN fastinfo_prod_import.department_vw dept ON comp.dataset_code = dept.dataset AND au.department = dept.code
           LEFT OUTER JOIN system_stores_fimvw assignedLoc ON comp.dataset_code = assignedLoc.dataset AND au.store_number = assignedLoc.number
           LEFT OUTER JOIN system_stores_fimvw chosenLoc ON comp.dataset_code = chosenLoc.dataset AND chosenLoc.number IS NULL
           JOIN system_stores_fimvw fallbackLoc ON comp.dataset_code = fallbackLoc.dataset AND fallbackLoc.number = (SELECT coalesce(max(store_number), 9000) FROM system_employees_fimvw WHERE dataset = comp.dataset_code)
      WHERE comp.dataset_code = :dataset
            AND au.number = :employeeNumber
            AND au.pass_code = convert_passcode(au.type, :passCode, au.pass_code)
   """,
      nativeQuery = true
   )
   @JoinSpecifications(
      Join("company"), // when defining these Join's the pathing should match the Java/Kotlin camelCase property access path not the '_' snake_case access path used in the associated SQL.  Micronaut Data will handle translating this
      Join("department"),
      Join("department.company"),
      Join("assignedLocation"),
      Join("assignedLocation.company"),
      Join("chosenLocation"),
      Join("chosenLocation.company"),
      Join("fallbackLocation"),
      Join("fallbackLocation.company"),
   )
   abstract fun findUserByAuthentication(employeeNumber: Int, passCode: String, dataset: String): AuthenticatedEmployee?

   @ReadOnly
   @Cacheable("creds-cache")
   fun findUser(employeeId: Long, employeeType: String, employeeNumber: Int, companyId: UUID, storeNumber: Int): AuthenticatedEmployee {
      val company = companyRepository.findOne(companyId) ?: throw Exception("Unable to find company")
      val employee = employeeRepository.findOne(employeeId, employeeType, company) ?: throw Exception("Unable to find employee")
      val location = locationRepository.findOne(storeNumber, company) ?: throw Exception("Unable to find store from authentication")
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
         cynergiSystemAdmin = employee.cynergiSystemAdmin,
         chosenLocation = location,
         fallbackLocation = location,
         passCode = employee.passCode
      )
   }

   @ReadOnly
   fun findPermissions(department: Department): Set<String> {
      val params = mapOf("dept_code" to department.myCode(), "comp_id" to department.myCompany().id)
      val sql =
         """
         SELECT aptd.value AS value
         FROM audit_permission_type_domain aptd
              JOIN audit_permission ap ON ap.type_id = aptd.id
         WHERE ap.department = :dept_code AND ap.company_id = :comp_id
         UNION
         SELECT aptd.value AS value
         FROM audit_permission_type_domain aptd
         WHERE aptd.id NOT IN (SELECT DISTINCT type_id FROM  audit_permission)
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

   private fun processPermissionValues(sql: String, params: Map<String, Any?>): Set<String> {
      logger.debug("Get permission {}\n{}", params, sql)

      val resultSet = mutableSetOf<String>()

      jdbi.query(sql, params) {
         rs, _ ->
         resultSet.add(rs.getString("value"))
      }

      return resultSet
   }
}
