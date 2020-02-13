package com.cynergisuite.middleware.authentication.user.infrastructure

import com.cynergisuite.extensions.getOffsetDateTime
import com.cynergisuite.extensions.getUuid
import com.cynergisuite.middleware.authentication.user.AuthenticatedUser
import com.cynergisuite.middleware.authentication.user.User
import com.cynergisuite.middleware.company.Company
import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.department.Department
import com.cynergisuite.middleware.department.DepartmentEntity
import com.cynergisuite.middleware.employee.EmployeeEntity
import com.cynergisuite.middleware.location.Location
import com.cynergisuite.middleware.store.StoreEntity
import io.reactiverse.reactivex.pgclient.PgPool
import io.reactiverse.reactivex.pgclient.Row
import io.reactiverse.reactivex.pgclient.Tuple
import io.reactivex.Maybe
import org.intellij.lang.annotations.Language
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthenticationRepository @Inject constructor(
   private val postgresClient: PgPool
) {
   private val logger: Logger = LoggerFactory.getLogger(AuthenticationRepository::class.java)
   /**
    * This method returns a PG Reactive Single Employee that is really meant to be used only for authentication as it
    * unions together the cynergidb.employee table as well as the view referenced by the Foreign Data Wrapper that is
    * pointed at FastInfo to pull in Zortec data about an Employee
    */
   fun findUserByAuthentication(employeeNumber: Int, passCode: String, dataset: String, storeNumber: Int?): Maybe<User> {
      logger.trace("Checking authentication for {} {} {}", employeeNumber, dataset, storeNumber)

      @Language("PostgreSQL")
      val query = """
         SELECT * FROM (
            SELECT * FROM (
               SELECT
                  1 AS from_priority,
                  emp.id AS emp_id,
                  'sysz' AS emp_type,
                  emp.number AS emp_number,
                  emp.active AS emp_active,
                  comp.id AS comp_id,
                  comp.uu_row_id AS comp_uu_row_id,
                  comp.time_created AS comp_time_created,
                  comp.time_updated AS comp_time_updated,
                  comp.name AS comp_name,
                  comp.doing_business_as AS comp_doing_business_as,
                  comp.client_code AS comp_client_code,
                  comp.client_id AS comp_client_id,
                  comp.dataset_code AS comp_dataset,
                  comp.federal_id_number AS comp_federal_id_number,
                  dept.id AS dept_id,
                  dept.code AS dept_code,
                  dept.description AS dept_description,
                  dept.security_profile AS dept_security_profile,
                  dept.default_menu AS dept_default_menu,
                  loc.id AS loc_id,
                  loc.number AS loc_number,
                  loc.name AS loc_name,
                  defaultLoc.id AS defaultLoc_id,
                  defaultLoc.number AS defaultLoc_number,
                  defaultLoc.name AS defaultLoc_name
               FROM company comp
                  JOIN fastinfo_prod_import.employee_vw emp ON comp.dataset_code = emp.dataset
                  LEFT OUTER JOIN fastinfo_prod_import.department_vw dept ON comp.dataset_code = dept.dataset AND emp.department = dept.code
                  LEFT OUTER JOIN fastinfo_prod_import.store_vw loc ON comp.dataset_code = loc.dataset AND emp.store_number = loc.number
                  JOIN fastinfo_prod_import.store_vw defaultLoc ON comp.dataset_code = defaultLoc.dataset AND defaultLoc.number = (SELECT coalesce(max(store_number), 9000) FROM fastinfo_prod_import.employee_vw)
               UNION
               SELECT
                  2 AS from_priority,
                  emp.id AS emp_id,
                  'eli' AS emp_type,
                  emp.number AS emp_number,
                  emp.active AS emp_active,
                  comp.id AS comp_id,
                  comp.uu_row_id AS comp_uu_row_id,
                  comp.time_created AS comp_time_created,
                  comp.time_updated AS comp_time_updated,
                  comp.name AS comp_name,
                  comp.doing_business_as AS comp_doing_business_as,
                  comp.client_code AS comp_client_code,
                  comp.client_id AS comp_client_id,
                  comp.dataset_code AS comp_dataset,
                  comp.federal_id_number AS comp_federal_id_number,
                  dept.id AS dept_id,
                  dept.code AS dept_code,
                  dept.description AS dept_description,
                  dept.security_profile AS dept_security_profile,
                  dept.default_menu AS dept_default_menu,
                  loc.id AS loc_id,
                  loc.number AS loc_number,
                  loc.name AS loc_name,
                  defaultLoc.id AS defaultLoc_id,
                  defaultLoc.number AS defaultLoc_number,
                  defaultLoc.name AS defaultLoc_name
               FROM company comp
                  JOIN employee emp ON comp.id = emp.company_id
                  LEFT OUTER JOIN fastinfo_prod_import.department_vw dept ON comp.dataset_code = dept.dataset AND emp.department = dept.code
                  LEFT OUTER JOIN fastinfo_prod_import.store_vw loc ON comp.dataset_code = loc.dataset AND emp.store_number = loc.number
                  JOIN fastinfo_prod_import.store_vw defaultLoc ON comp.dataset_code = defaultLoc.dataset AND defaultLoc.number = (SELECT coalesce(max(store_number), 9000) FROM fastinfo_prod_import.employee_vw)
            ) AS inner_users
            WHERE emp_active = true
            ORDER BY from_priority
         ) AS users
         WHERE comp_dataset = $1
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

            val company = mapCompany(row, "comp_")
            val department = mapDepartment(row, company, "dept_")
            val defaultStore = mapLocation(row, company, "defaultLoc_")!!
            val store = mapLocation(row, company, "loc_")
            val employee = AuthenticatedUser(
               id = row.getLong("emp_id"),
               type = row.getString("emp_type"),
               number = row.getInteger("emp_number"),
               company = company,
               department = department,
               location = defaultStore,
               passCode = row.getString("emp_pass_code")
            )

            logger.trace("Processing results for employee {} with default store {}", employee, defaultStore)

            employee to defaultStore
         }
         .filter { (employee, _) ->
            if (employee.type == "eli") {
               logger.trace("Checking eli employee with hash password {}", employee)

               passwordEncoderService.matches(passCode, employee.passCode)
            } else {
               logger.trace("Checking sysz employee with plain text password {}", employee)

               employee.passCode == passCode // FIXME remove this when all users are loaded out of cynergidb and are encoded by BCrypt
            }
         }
         .filter { (employee, _) ->
            logger.trace("checking if employee store is null [Store {}] and if that employee is allowed to be auto assigned [allowAutoStoreAssign {}]", employee.store, employee.allowAutoStoreAssign)
            // FIXME this will probably need to be changed to doing some kind map that indicates to the caller that the user couldn't be logged int because they don't have a default store and aren't allow auto store assign
            employee.store != null || employee.allowAutoStoreAssign
         }
         .map { (employee, defaultStore) ->
            if (employee.store == null && employee.allowAutoStoreAssign) {
               logger.debug("Employee {} is allowed to auto store assign using {}", employeeNumber, defaultStore)

               employee.copy(store = defaultStore)
            } else {
               logger.debug("Employee {} is not allowed to auto store assign", employeeNumber)

               employee
            }
         }
   }

   private fun mapCompany(row: Row, columnPrefix: String): Company {
      return CompanyEntity(
         id = row.getLong("${columnPrefix}id"),
         uuRowId = row.getUUID("${columnPrefix}uu_row_id"),
         timeCreated = row.getOffsetDateTime("${columnPrefix}time_created"),
         timeUpdated = row.getOffsetDateTime("${columnPrefix}time_updated"),
         name = row.getString("${columnPrefix}name"),
         doingBusinessAs = row.getString("${columnPrefix}doing_business_as"),
         clientCode = row.getString("${columnPrefix}client_code"),
         clientId = row.getInteger("${columnPrefix}client_id"),
         datasetCode = row.getString("${columnPrefix}dataset_code"),
         federalTaxNumber = row.getString("${columnPrefix}federal_tax_number")
      )
   }

   private fun mapDepartment(row: Row, company: Company, columnPrefix: String): Department? {
      return if (row.getLong("${columnPrefix}id") != null) {
         DepartmentEntity(
            id = row.getLong("${columnPrefix}id"),
            code = row.getString("${columnPrefix}code"),
            description = row.getString("${columnPrefix}description"),
            securityProfile = row.getInteger("${columnPrefix}security_profile"),
            defaultMenu = row.getString("${columnPrefix}default_menu"),
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
