package com.cynergisuite.middleware.authentication.user.infrastructure

import com.cynergisuite.extensions.findFirstOrNull
import com.cynergisuite.extensions.getIntOrNull
import com.cynergisuite.middleware.authentication.PasswordEncoderService
import com.cynergisuite.middleware.authentication.user.AuthenticatedEmployee
import com.cynergisuite.middleware.authentication.user.AuthenticatedUser
import com.cynergisuite.middleware.authentication.user.User
import com.cynergisuite.middleware.authentication.user.UserSecurityLevels
import com.cynergisuite.middleware.company.Company
import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.company.infrastructure.CompanyRepository
import com.cynergisuite.middleware.department.Department
import com.cynergisuite.middleware.department.DepartmentEntity
import com.cynergisuite.middleware.employee.infrastructure.EmployeeRepository
import com.cynergisuite.middleware.location.infrastructure.LocationRepository
import com.cynergisuite.middleware.store.Store
import com.cynergisuite.middleware.store.StoreEntity
import io.micronaut.cache.annotation.Cacheable
import io.reactivex.Maybe
import io.vertx.reactivex.pgclient.PgPool
import io.vertx.reactivex.sqlclient.Row
import io.vertx.reactivex.sqlclient.Tuple
import org.apache.commons.lang3.StringUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.sql.ResultSet
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthenticationRepository @Inject constructor(
   private val companyRepository: CompanyRepository,
   private val employeeRepository: EmployeeRepository,
   private val locationRepository: LocationRepository,
   private val passwordEncoderService: PasswordEncoderService,
   private val postgresClient: PgPool,
   private val jdbc: NamedParameterJdbcTemplate
) {
   private val logger: Logger = LoggerFactory.getLogger(AuthenticationRepository::class.java)

   /**
    * This method returns a PG Reactive Single Employee that is really meant to be used only for authentication as it
    * unions together the cynergidb.employee table as well as the view referenced by the Foreign Data Wrapper that is
    * pointed at FastInfo to pull in Zortec data about an Employee
    */
   fun findUserByAuthentication(employeeNumber: Int, passCode: String, dataset: String, storeNumber: Int?): Maybe<AuthenticatedEmployee> {
      logger.trace("Checking authentication for {} {} {}", employeeNumber, dataset, storeNumber)

      val query =
         """
         SELECT * FROM (
            SELECT * FROM (
               SELECT
                  1                               AS from_priority,
                  emp.id                          AS emp_id,
                  'sysz'                          AS emp_type,
                  emp.number                      AS emp_number,
                  emp.active                      AS emp_active,
                  false                           AS emp_cynergi_system_admin,
                  emp.pass_code                   AS emp_pass_code,
                  emp.alternative_store_indicator AS emp_alternative_store_indicator,
                  emp.alternative_area            AS emp_alternative_area,
                  comp.id                         AS comp_id,
                  comp.time_created               AS comp_time_created,
                  comp.time_updated               AS comp_time_updated,
                  comp.name                       AS comp_name,
                  comp.doing_business_as          AS comp_doing_business_as,
                  comp.client_code                AS comp_client_code,
                  comp.client_id                  AS comp_client_id,
                  comp.dataset_code               AS comp_dataset_code,
                  comp.federal_id_number          AS comp_federal_id_number,
                  address.id                      AS address_id,
                  address.name                    AS address_name,
                  address.address1                AS address_address1,
                  address.address2                AS address_address2,
                  address.city                    AS address_city,
                  address.state                   AS address_state,
                  address.postal_code             AS address_postal_code,
                  address.latitude                AS address_latitude,
                  address.longitude               AS address_longitude,
                  address.country                 AS address_country,
                  address.county                  AS address_county,
                  address.phone                   AS address_phone,
                  address.fax                     AS address_fax,
                  dept.id                         AS dept_id,
                  dept.code                       AS dept_code,
                  dept.description                AS dept_description,
                  assignedLoc.id                  AS assignedLoc_id,
                  assignedLoc.number              AS assignedLoc_number,
                  assignedLoc.name                AS assignedLoc_name,
                  chosenLoc.id                    AS chosenLoc_id,
                  chosenLoc.number                AS chosenLoc_number,
                  chosenLoc.name                  AS chosenLoc_name,
                  fallbackLoc.id                  AS fallbackLoc_id,
                  fallbackLoc.number              AS fallbackLoc_number,
                  fallbackLoc.name                AS fallbackLoc_name
               FROM company comp
                  JOIN fastinfo_prod_import.employee_vw emp ON comp.dataset_code = emp.dataset
                  LEFT JOIN address ON comp.address_id = address.id
                  LEFT OUTER JOIN fastinfo_prod_import.department_vw dept ON comp.dataset_code = dept.dataset AND emp.department = dept.code
                  LEFT OUTER JOIN fastinfo_prod_import.store_vw assignedLoc ON comp.dataset_code = assignedLoc.dataset AND emp.store_number = assignedLoc.number
                  LEFT OUTER JOIN fastinfo_prod_import.store_vw chosenLoc ON comp.dataset_code = chosenLoc.dataset AND chosenLoc.number ${if (storeNumber != null) " = $3" else "IS NULL"}
                  JOIN fastinfo_prod_import.store_vw fallbackLoc ON comp.dataset_code = fallbackLoc.dataset AND fallbackLoc.number = (SELECT coalesce(max(store_number), 9000) FROM fastinfo_prod_import.employee_vw WHERE dataset = comp.dataset_code)
               UNION
               SELECT
                  2                               AS from_priority,
                  emp.id                          AS emp_id,
                  'eli'                           AS emp_type,
                  emp.number                      AS emp_number,
                  emp.active                      AS emp_active,
                  emp.cynergi_system_admin        AS emp_cynergi_system_admin,
                  emp.pass_code                   AS emp_pass_code,
                  emp.alternative_store_indicator AS emp_alternative_store_indicator,
                  emp.alternative_area            AS emp_alternative_area,
                  comp.id                         AS comp_id,
                  comp.time_created               AS comp_time_created,
                  comp.time_updated               AS comp_time_updated,
                  comp.name                       AS comp_name,
                  comp.doing_business_as          AS comp_doing_business_as,
                  comp.client_code                AS comp_client_code,
                  comp.client_id                  AS comp_client_id,
                  comp.dataset_code               AS comp_dataset_code,
                  comp.federal_id_number          AS comp_federal_id_number,
                  address.id                      AS address_id,
                  address.name                    AS address_name,
                  address.address1                AS address_address1,
                  address.address2                AS address_address2,
                  address.city                    AS address_city,
                  address.state                   AS address_state,
                  address.postal_code             AS address_postal_code,
                  address.latitude                AS address_latitude,
                  address.longitude               AS address_longitude,
                  address.country                 AS address_country,
                  address.county                  AS address_county,
                  address.phone                   AS address_phone,
                  address.fax                     AS address_fax,
                  dept.id                         AS dept_id,
                  dept.code                       AS dept_code,
                  dept.description                AS dept_description,
                  assignedLoc.id                  AS assignedLoc_id,
                  assignedLoc.number              AS assignedLoc_number,
                  assignedLoc.name                AS assignedLoc_name,
                  chosenLoc.id                    AS chosenLoc_id,
                  chosenLoc.number                AS chosenLoc_number,
                  chosenLoc.name                  AS chosenLoc_name,
                  fallbackLoc.id                  AS fallbackLoc_id,
                  fallbackLoc.number              AS fallbackLoc_number,
                  fallbackLoc.name                AS fallbackLoc_name
               FROM company comp
                  JOIN employee emp ON comp.id = emp.company_id
                  LEFT JOIN address ON comp.address_id = address.id
                  LEFT OUTER JOIN fastinfo_prod_import.department_vw dept ON comp.dataset_code = dept.dataset AND emp.department = dept.code
                  LEFT OUTER JOIN fastinfo_prod_import.store_vw assignedLoc ON comp.dataset_code = assignedLoc.dataset AND emp.store_number = assignedLoc.number
                  LEFT OUTER JOIN fastinfo_prod_import.store_vw chosenLoc ON comp.dataset_code = chosenLoc.dataset AND chosenLoc.number ${if (storeNumber != null) " = $3" else "IS NULL"}
                  JOIN fastinfo_prod_import.store_vw fallbackLoc ON comp.dataset_code = fallbackLoc.dataset AND fallbackLoc.number = (SELECT coalesce(max(store_number), 9000) FROM fastinfo_prod_import.employee_vw WHERE dataset = comp.dataset_code)
            ) AS inner_users
            WHERE emp_active = true
            ORDER BY from_priority
         ) AS users
      WHERE comp_dataset_code = $1
         AND emp_number = $2
         """.trimIndent()
      val params = if (storeNumber != null) {
         Tuple.of(dataset, employeeNumber, storeNumber)
      } else {
         Tuple.of(dataset, employeeNumber)
      }

      logger.trace("Checking authentication for {} {} {} using {}", employeeNumber, dataset, storeNumber, params)

      return postgresClient.preparedQuery(query)
         .rxExecute(params)
         .filter { rs -> rs.size() > 0 }
         .map { rs ->
            val iterator = rs.iterator()
            val row = iterator.next()

            val company = mapCompany(row)
            val department = mapDepartment(row, company)
            val fallbackLocation = mapLocation(row, company, "fallbackloc_")!! // make sure fallbackLoc is all lower case, the reactive pg row isn't as smart as the JDBC driver
            val employeeLocation = mapLocation(row, company, "assignedloc_") // make sure assignedloc is all lower case, the reactive pg row isn't as smart as the JDBC driver
            val chosenLocation = mapLocation(row, company, "chosenloc_") // make sure chosenloc is all lower case, the reactive pg row isn't as smart as the JDBC driver

            AuthenticatedEmployee(
               id = row.getLong("emp_id"),
               type = row.getString("emp_type"),
               number = row.getInteger("emp_number"),
               company = company,
               department = department,
               location = employeeLocation,
               chosenLocation = chosenLocation,
               fallbackLocation = fallbackLocation,
               passCode = row.getString("emp_pass_code"),
               cynergiSystemAdmin = row.getBoolean("emp_cynergi_system_admin"),
               alternativeStoreIndicator = row.getString("emp_alternative_store_indicator"),
               alternativeArea = row.getLong("emp_alternative_area")
            )
         }
         .filter { employee ->
            if (employee.type == "eli") {
               logger.trace("Checking eli employee with hash password {}", employee)

               passwordEncoderService.matches(passCode, employee.passCode)
            } else {
               logger.trace("Checking sysz employee with plain text password {}", employee)

               // existing cynergi login process lets the user type in as many characters as they want and only compares the first 6 characters to the stored password
               employee.passCode == StringUtils.substring(passCode, 0, 6) // FIXME remove this when all users are loaded out of cynergidb and are encoded by BCrypt
            }
         }
   }

   @Cacheable("creds-cache")
   fun findUser(employeeId: Long, employeeType: String, employeeNumber: Int, companyId: UUID, storeNumber: Int): AuthenticatedUser {
      val company = companyRepository.findOne(companyId) ?: throw Exception("Unable to find company")
      val employee = employeeRepository.findOne(employeeId, employeeType, company) ?: throw Exception("Unable to find employee")
      val location = locationRepository.findOne(storeNumber, company) ?: throw Exception("Unable to find store from authentication")
      val department = employee.department

      return AuthenticatedUser(
         id = employeeId,
         type = employeeType,
         number = employeeNumber,
         company = company,
         department = department,
         location = location,
         alternativeStoreIndicator = employee.alternativeStoreIndicator,
         alternativeArea = employee.alternativeArea,
         cynergiSystemAdmin = employee.cynergiSystemAdmin
      )
   }

   private fun mapCompany(row: Row): Company {
      return CompanyEntity(
         id = row.getUUID("comp_id"),
         name = row.getString("comp_name"),
         doingBusinessAs = row.getString("comp_doing_business_as"),
         clientCode = row.getString("comp_client_code"),
         clientId = row.getInteger("comp_client_id"),
         datasetCode = row.getString("comp_dataset_code"),
         federalIdNumber = row.getString("comp_federal_id_number")
      )
   }

   fun findPermissions(department: Department): Set<String> {
      val params = mapOf("dept_code" to department.myCode(), "comp_id" to department.myCompany().myId())
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

   fun findUserSecurityLevels(user: User, company: Company): UserSecurityLevels? {
      logger.debug("Get user security levels")

      val params = mutableMapOf<String, Any?>("emp_number" to user.myEmployeeNumber(), "dataset_code" to company.myDataset())
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

      return jdbc.findFirstOrNull(
         sql,
         params
      ) { rs, _ ->
         mapUserSecurityLevels(rs)
      } ?: UserSecurityLevels(user.isCynergiAdmin())
   }

   private fun processPermissionValues(sql: String, params: Map<String, Any?>): Set<String> {
      logger.debug("Get permission {}\n{}", params, sql)

      val resultSet = mutableSetOf<String>()

      jdbc.query(sql, params) {
         rs ->
         resultSet.add(rs.getString("value"))
      }

      return resultSet
   }

   private fun mapDepartment(row: Row, company: Company): Department? {
      return if (row.getLong("dept_id") != null) {
         DepartmentEntity(
            id = row.getLong("dept_id"),
            code = row.getString("dept_code"),
            description = row.getString("dept_description"),
            company = company
         )
      } else {
         null
      }
   }

   private fun mapLocation(row: Row, company: Company, columnPrefix: String): Store? {
      return if (row.getLong("${columnPrefix}id") != null) {
         StoreEntity(
            id = row.getLong("${columnPrefix}id"),
            number = row.getInteger("${columnPrefix}number"),
            name = row.getString("${columnPrefix}name"),
            company = company,
         )
      } else {
         null
      }
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
