package com.cynergisuite.middleware.authentication.user.infrastructure

import com.cynergisuite.middleware.authentication.PasswordEncoderService
import com.cynergisuite.middleware.authentication.user.AuthenticatedEmployee
import com.cynergisuite.middleware.authentication.user.AuthenticatedUser
import com.cynergisuite.middleware.company.Company
import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.company.infrastructure.CompanyRepository
import com.cynergisuite.middleware.department.Department
import com.cynergisuite.middleware.department.DepartmentEntity
import com.cynergisuite.middleware.department.infrastructure.DepartmentRepository
import com.cynergisuite.middleware.employee.infrastructure.EmployeeRepository
import com.cynergisuite.middleware.store.StoreEntity
import com.cynergisuite.middleware.store.infrastructure.StoreRepository
import io.micronaut.cache.annotation.Cacheable
import io.reactiverse.reactivex.pgclient.PgPool
import io.reactiverse.reactivex.pgclient.Row
import io.reactiverse.reactivex.pgclient.Tuple
import io.reactivex.Maybe
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthenticationRepository @Inject constructor(
   private val companyRepository: CompanyRepository,
   private val departmentRepository: DepartmentRepository,
   private val employeeRepository: EmployeeRepository,
   private val storeRepository: StoreRepository,
   private val passwordEncoderService: PasswordEncoderService,
   private val postgresClient: PgPool
) {
   private val logger: Logger = LoggerFactory.getLogger(AuthenticationRepository::class.java)
   /**
    * This method returns a PG Reactive Single Employee that is really meant to be used only for authentication as it
    * unions together the cynergidb.employee table as well as the view referenced by the Foreign Data Wrapper that is
    * pointed at FastInfo to pull in Zortec data about an Employee
    */
   fun findUserByAuthentication(employeeNumber: Int, passCode: String, dataset: String, storeNumber: Int?): Maybe<AuthenticatedEmployee> {
      logger.trace("Checking authentication for {} {} {}", employeeNumber, dataset, storeNumber)

      val query = """
         SELECT * FROM (
            SELECT * FROM (
               SELECT
                  1                      AS from_priority,
                  emp.id                 AS emp_id,
                  'sysz'                 AS emp_type,
                  emp.number             AS emp_number,
                  emp.active             AS emp_active,
                  false                  AS emp_cynergi_system_admin,
                  emp.pass_code          AS emp_pass_code,
                  emp.alternative_store_indicator AS emp_alternative_store_indicator,
                  emp.alternative_area   AS emp_alternative_area,
                  comp.id                AS comp_id,
                  comp.uu_row_id         AS comp_uu_row_id,
                  comp.time_created      AS comp_time_created,
                  comp.time_updated      AS comp_time_updated,
                  comp.name              AS comp_name,
                  comp.doing_business_as AS comp_doing_business_as,
                  comp.client_code       AS comp_client_code,
                  comp.client_id         AS comp_client_id,
                  comp.dataset_code      AS comp_dataset_code,
                  comp.federal_id_number AS comp_federal_id_number,
                  dept.id                AS dept_id,
                  dept.code              AS dept_code,
                  dept.description       AS dept_description,
                  dept.security_profile  AS dept_security_profile,
                  dept.default_menu      AS dept_default_menu,
                  loc.id                 AS loc_id,
                  loc.number             AS loc_number,
                  loc.name               AS loc_name,
                  fallbackLoc.id         AS fallbackLoc_id,
                  fallbackLoc.number     AS fallbackLoc_number,
                  fallbackLoc.name       AS fallbackLoc_name
               FROM company comp
                  JOIN fastinfo_prod_import.employee_vw emp ON comp.dataset_code = emp.dataset
                  LEFT OUTER JOIN fastinfo_prod_import.department_vw dept ON comp.dataset_code = dept.dataset AND emp.department = dept.code
                  LEFT OUTER JOIN fastinfo_prod_import.store_vw loc ON comp.dataset_code = loc.dataset AND emp.store_number = loc.number
                  JOIN fastinfo_prod_import.store_vw fallbackLoc ON comp.dataset_code = fallbackLoc.dataset AND fallbackLoc.number = (SELECT coalesce(max(store_number), 9000) FROM fastinfo_prod_import.employee_vw)
               UNION
               SELECT
                  2                           AS from_priority,
                  emp.id                      AS emp_id,
                  'eli'                       AS emp_type,
                  emp.number                  AS emp_number,
                  emp.active                  AS emp_active,
                  emp.cynergi_system_admin    AS emp_cynergi_system_admin,
                  emp.pass_code               AS emp_pass_code,
                  emp.alternative_store_indicator AS emp_alternative_store_indicator,
                  emp.alternative_area        AS emp_alternative_area,
                  comp.id                     AS comp_id,
                  comp.uu_row_id              AS comp_uu_row_id,
                  comp.time_created           AS comp_time_created,
                  comp.time_updated           AS comp_time_updated,
                  comp.name                   AS comp_name,
                  comp.doing_business_as      AS comp_doing_business_as,
                  comp.client_code            AS comp_client_code,
                  comp.client_id              AS comp_client_id,
                  comp.dataset_code           AS comp_dataset_code,
                  comp.federal_id_number      AS comp_federal_id_number,
                  dept.id                     AS dept_id,
                  dept.code                   AS dept_code,
                  dept.description            AS dept_description,
                  dept.security_profile       AS dept_security_profile,
                  dept.default_menu           AS dept_default_menu,
                  loc.id                      AS loc_id,
                  loc.number                  AS loc_number,
                  loc.name                    AS loc_name,
                  fallbackLoc.id               AS fallbackLoc_id,
                  fallbackLoc.number           AS fallbackLoc_number,
                  fallbackLoc.name             AS fallbackLoc_name
               FROM company comp
                  JOIN employee emp ON comp.id = emp.company_id
                  LEFT OUTER JOIN fastinfo_prod_import.department_vw dept ON comp.dataset_code = dept.dataset AND emp.department = dept.code
                  LEFT OUTER JOIN fastinfo_prod_import.store_vw loc ON comp.dataset_code = loc.dataset AND emp.store_number = loc.number
                  JOIN fastinfo_prod_import.store_vw fallbackLoc ON comp.dataset_code = fallbackLoc.dataset AND fallbackLoc.number = (SELECT coalesce(max(store_number), 9000) FROM fastinfo_prod_import.employee_vw)
            ) AS inner_users
            WHERE emp_active = true
            ORDER BY from_priority
         ) AS users
         WHERE comp_dataset_code = $1
               AND emp_number = $2
               ${if (storeNumber != null) "AND loc_number = $3" else ""}
      """.trimIndent()
      val params = if (storeNumber != null) {
         Tuple.of(dataset, employeeNumber, storeNumber)
      } else {
         Tuple.of(dataset, employeeNumber)
      }

      logger.trace("Checking authentication for {} {} {} using {}", employeeNumber, dataset, storeNumber, params)

      return postgresClient.rxPreparedQuery(query, params)
         .filter { rs -> rs.size() > 0 }
         .map { rs ->
            val iterator = rs.iterator()
            val row = iterator.next()

            val company = mapCompany(row)
            val department = mapDepartment(row, company)
            val fallbackLocation = mapLocation(row, company, "fallbackloc_")!!
            val employeeLocation = mapLocation(row, company, "loc_")

            AuthenticatedEmployee(
               id = row.getLong("emp_id"),
               type = row.getString("emp_type"),
               number = row.getInteger("emp_number"),
               company = company,
               department = department,
               location = employeeLocation,
               fallbackLocation = fallbackLocation,
               passCode = row.getString("emp_pass_code"),
               cynergiSystemAdmin = row.getBoolean("emp_cynergi_system_admin"),
               alternativeStoreIndicator = row.getString("emp_alternative_store_indicator"),
               alternativeArea = row.getInteger("emp_alternative_area")
            )
         }
         .filter { employee ->
            if (employee.type == "eli") {
               logger.trace("Checking eli employee with hash password {}", employee)

               passwordEncoderService.matches(passCode, employee.passCode)
            } else {
               logger.trace("Checking sysz employee with plain text password {}", employee)

               employee.passCode == passCode // FIXME remove this when all users are loaded out of cynergidb and are encoded by BCrypt
            }
         }
   }

   @Cacheable("creds-cache")
   fun findUser(employeeId: Long, employeeType: String, employeeNumber: Int, companyId: Long, storeNumber: Int): AuthenticatedUser {
      val company = companyRepository.findOne(companyId) ?: throw Exception("Unable to find company")
      val employee = employeeRepository.findOne(employeeId, employeeType, company) ?: throw Exception("Unable to find employee")
      val location = storeRepository.findOne(storeNumber, company) ?: throw Exception("Unable to find store from authentication")
      val department = employee.department

      return AuthenticatedUser(
         id = employeeId,
         type = employeeType,
         number = employeeNumber,
         company = company,
         department = department,
         location = location,
         alternativeStoreIndicator = employee.alternativeStoreIndicator,
         alternativeArea = employee.alternativeArea
      )
   }

   private fun mapCompany(row: Row): Company {
      return CompanyEntity(
         id = row.getLong("comp_id"),
         uuRowId = row.getUUID("comp_uu_row_id"),
         timeCreated = row.getOffsetDateTime("comp_time_created"),
         timeUpdated = row.getOffsetDateTime("comp_time_updated"),
         name = row.getString("comp_name"),
         doingBusinessAs = row.getString("comp_doing_business_as"),
         clientCode = row.getString("comp_client_code"),
         clientId = row.getInteger("comp_client_id"),
         datasetCode = row.getString("comp_dataset_code"),
         federalIdNumber = row.getString("comp_federal_tax_number")
      )
   }

   private fun mapDepartment(row: Row, company: Company): Department? {
      return if (row.getLong("dept_id") != null) {
         DepartmentEntity(
            id = row.getLong("dept_id"),
            code = row.getString("dept_code"),
            description = row.getString("dept_description"),
            securityProfile = row.getInteger("dept_security_profile"),
            defaultMenu = row.getString("dept_default_menu"),
            company = company
         )
      } else {
         null
      }
   }

   private fun mapLocation(row: Row, company: Company, columnPrefix: String): StoreEntity? {
      return if (row.getLong("${columnPrefix}id") != null) {
         StoreEntity(
            id = row.getLong("${columnPrefix}id"),
            number = row.getInteger("${columnPrefix}number"),
            name = row.getString("${columnPrefix}name"),
            company = company
         )
      } else {
         null
      }
   }
}
