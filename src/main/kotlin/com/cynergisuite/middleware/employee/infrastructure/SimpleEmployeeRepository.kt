package com.cynergisuite.middleware.employee.infrastructure

import com.cynergisuite.domain.PageRequest
import com.cynergisuite.domain.infrastructure.RepositoryPage
import com.cynergisuite.extensions.findFirstOrNull
import com.cynergisuite.extensions.insertReturning
import com.cynergisuite.extensions.trimToNull
import com.cynergisuite.middleware.authentication.PasswordEncoderService
import com.cynergisuite.middleware.authentication.user.AuthenticatedUser
import com.cynergisuite.middleware.authentication.user.User
import com.cynergisuite.middleware.company.Company
import com.cynergisuite.middleware.company.infrastructure.CompanyRepository
import com.cynergisuite.middleware.department.Department
import com.cynergisuite.middleware.department.infrastructure.DepartmentRepository
import com.cynergisuite.middleware.employee.EmployeeEntity
import com.cynergisuite.middleware.employee.EmployeePageRequest
import com.cynergisuite.middleware.store.Store
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.sql.ResultSet
import java.sql.SQLException
import javax.inject.Inject
import javax.inject.Singleton
import javax.transaction.Transactional

/**
 * The reason to have this is to fic the circular dependency issue
 * Store -> Region -> Division -> Employee -> Store
 */
@Singleton
class SimpleEmployeeRepository @Inject constructor(
   private val companyRepository: CompanyRepository,
   private val departmentRepository: DepartmentRepository,
   private val jdbc: NamedParameterJdbcTemplate,
   private val passwordEncoderService: PasswordEncoderService
) {
   private val logger: Logger = LoggerFactory.getLogger(SimpleEmployeeRepository::class.java)

   fun employeeBaseQuery() =
      """
      SELECT * FROM (
         SELECT * FROM (
            SELECT
               1                               AS from_priority,
               emp.id                          AS emp_id,
               'sysz'                          AS emp_type,
               emp.number                      AS emp_number,
               emp.last_name                   AS emp_last_name,
               emp.first_name_mi               AS emp_first_name_mi,
               emp.pass_code                   AS emp_pass_code,
               emp.active                      AS emp_active,
               emp.department                  AS emp_department,
               false                           AS emp_cynergi_system_admin,
               emp.alternative_store_indicator AS emp_alternative_store_indicator,
               emp.alternative_area            AS emp_alternative_area,
               comp.id                         AS comp_id,
               comp.uu_row_id                  AS comp_uu_row_id,
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
               store.id                        AS store_id,
               store.number                    AS store_number,
               store.name                      AS store_name
            FROM fastinfo_prod_import.employee_vw emp
               JOIN company comp ON emp.dataset = comp.dataset_code
               LEFT JOIN address ON comp.address_id = address.id
               LEFT OUTER JOIN fastinfo_prod_import.department_vw dept ON comp.dataset_code = dept.dataset AND emp.department = dept.code
               LEFT OUTER JOIN fastinfo_prod_import.store_vw store ON comp.dataset_code = store.dataset AND emp.store_number = store.number
            UNION
            SELECT
               2                               AS from_priority,
               emp.id                          AS emp_id,
               'eli'                           AS emp_type,
               emp.number                      AS emp_number,
               emp.last_name                   AS emp_last_name,
               emp.first_name_mi               AS emp_first_name_mi,
               emp.pass_code                   AS emp_pass_code,
               emp.active                      AS emp_active,
               emp.department                  AS emp_department,
               emp.cynergi_system_admin        AS emp_cynergi_system_admin,
               emp.alternative_store_indicator AS emp_alternative_store_indicator,
               emp.alternative_area            AS emp_alternative_area,
               comp.id                         AS comp_id,
               comp.uu_row_id                  AS comp_uu_row_id,
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
               store.id                        AS store_id,
               store.number                    AS store_number,
               store.name                      AS store_name
            FROM employee emp
               JOIN company comp ON emp.company_id = comp.id
               LEFT JOIN address ON comp.address_id = address.id
               LEFT OUTER JOIN fastinfo_prod_import.department_vw dept ON comp.dataset_code = dept.dataset AND emp.department = dept.code
               LEFT OUTER JOIN fastinfo_prod_import.store_vw store ON comp.dataset_code = store.dataset AND emp.store_number = store.number
         ) AS inner_employees
         ORDER BY from_priority
      ) AS employees
   """

   fun findOne(user: User): EmployeeEntity? =
      findOne(user.myId(), user.myEmployeeType(), user.myCompany())

   fun findOne(id: Long, company: Company): EmployeeEntity? {
      val found = jdbc.findFirstOrNull(
         "${employeeBaseQuery()} WHERE comp_id = :comp_id AND emp_id = :emp_id LIMIT 1",
         mutableMapOf("comp_id" to company.myId(), "emp_id" to id),
         RowMapper { rs, _ -> mapRow(rs) }
      )

      logger.trace("Searching for Employee: {} {} {} resulted in {}", company, found)

      return found
   }

   fun findOne(id: Long, employeeType: String, company: Company): EmployeeEntity? {
      val found = jdbc.findFirstOrNull(
         "${employeeBaseQuery()} WHERE comp_id = :comp_id AND emp_id = :emp_id AND emp_type = :emp_type LIMIT 1",
         mutableMapOf("comp_id" to company.myId(), "emp_id" to id, "emp_type" to employeeType),
         RowMapper { rs, _ -> mapRow(rs) }
      )

      logger.trace("Searching for Employee: {} {} {} resulted in {}", id, employeeType, company, found)

      return found
   }

   fun findOne(number: Int, employeeType: String, company: Company): EmployeeEntity? {
      logger.debug("Searching for employee with {} {} {}", number, employeeType, company)

      val found = jdbc.findFirstOrNull(
         """
            ${employeeBaseQuery()}
            WHERE emp_number = :emp_number
                  AND emp_type = :emp_type
                  AND comp_id = :comp_id
            LIMIT 1
            """,
         mapOf("emp_number" to number, "emp_type" to employeeType, "comp_id" to company.myId()),
         RowMapper { rs, _ -> mapRow(rs) }
      )

      logger.trace("Searching for Employee: {} {} resulted in {}", number, employeeType, found)

      return found
   }

   fun findOne(user: AuthenticatedUser): EmployeeEntity? {
      logger.debug("Searching for Employee using {}", user)

      val found = jdbc.findFirstOrNull(
         """
         ${employeeBaseQuery()}
         WHERE emp_id = :id
               AND emp_type = :employee_type
               AND comp_id = :comp_id
         LIMIT 1
         """,
         mutableMapOf("id" to user.myId(), "employee_type" to user.myEmployeeType(), "comp_id" to user.myCompany().myId()),
         RowMapper { rs, _ -> mapRow(rs) }
      )

      logger.trace("Searching for Employee: {} resulted in {}", user, found)

      return found
   }

   fun findAll(pageRequest: EmployeePageRequest, company: Company): RepositoryPage<EmployeeEntity, PageRequest> {
      val firstNameMi = pageRequest.firstNameMi
      val lastName = pageRequest.lastName
      val searchString = pageRequest.search
      val params = mutableMapOf<String, Any?>(
         "comp_id" to company.myId(),
         "limit" to pageRequest.size(),
         "offset" to pageRequest.offset(),
         "firstNameMi" to firstNameMi,
         "lastName" to lastName,
         "search_query" to searchString
      )
      var totalElements: Long? = null
      val elements = mutableListOf<EmployeeEntity>()
      var where = StringBuilder(" WHERE comp_id = :comp_id AND emp_cynergi_system_admin = false ")
      var and = " AND "
      var sortBy = " emp_${pageRequest.snakeSortBy()} ${pageRequest.sortDirection()}  "

      if (!firstNameMi.isNullOrEmpty()) {
         where.append(and).append(" emp_first_name_mi = :firstNameMi ")
      }

      if (!lastName.isNullOrEmpty()) {
         where.append(and).append(" emp_last_name = :lastName ")
      }

      // postgres uses index only if query use <-> pg_trgm operator, not pg_trgm function
      if (!searchString.isNullOrEmpty()) {
         val fieldToSearch = "COALESCE(emp_first_name_mi, '') || ' ' || COALESCE(emp_last_name, '')"
         where.append(and).append(" $fieldToSearch <-> :search_query < 0.9 ")
         sortBy = " $fieldToSearch <-> :search_query "
      }

      val pagedQuery = StringBuilder("${employeeBaseQuery()} $where ")

      val query =
         """
         WITH paged AS (
            $pagedQuery
         )
         SELECT
            p.*,
            count(*) OVER() as total_elements
         FROM paged AS p
         ORDER BY $sortBy
         LIMIT :limit OFFSET :offset
      """
      logger.trace("Fetching all employees using {} / {}", query, params)

      jdbc.query(query, params) { rs ->
         if (totalElements == null) {
            totalElements = rs.getLong("total_elements")
         }

         elements.add(mapRow(rs))
      }

      return RepositoryPage(
         requested = pageRequest,
         elements = elements,
         totalElements = totalElements ?: 0
      )
   }

   fun exists(user: User): Boolean =
      exists(user.myId(), user.myEmployeeType(), user.myCompany())

   fun exists(id: Long, employeeType: String, company: Company): Boolean {
      val exists = jdbc.queryForObject(
         """
         SELECT count(emp_id) = 1 FROM (
            SELECT emp_id, emp_type, comp_id FROM (
               SELECT
                  1 AS from_priority,
                  emp.id AS emp_id,
                  'sysz' AS emp_type,
                  comp.id AS comp_id
               FROM fastinfo_prod_import.employee_vw emp
                  JOIN company comp ON emp.dataset = comp.dataset_code
                  LEFT OUTER JOIN fastinfo_prod_import.department_vw dept ON comp.dataset_code = dept.dataset AND emp.department = dept.code
                  LEFT OUTER JOIN fastinfo_prod_import.store_vw store ON comp.dataset_code = store.dataset AND emp.store_number = store.number
               UNION
               SELECT
                  2 AS from_priority,
                  emp.id AS emp_id,
                  'eli' AS emp_type,
                  comp.id AS comp_id
               FROM employee emp
                  JOIN company comp ON emp.company_id = comp.id
                  LEFT OUTER JOIN fastinfo_prod_import.department_vw dept ON comp.dataset_code = dept.dataset AND emp.department = dept.code
                  LEFT OUTER JOIN fastinfo_prod_import.store_vw store ON comp.dataset_code = store.dataset AND emp.store_number = store.number
            ) AS inner_employees
            ORDER BY from_priority
         ) AS employees
         WHERE emp_id = :emp_id AND emp_type = :emp_type AND comp_id = :comp_id""",
         mapOf("emp_id" to id, "emp_type" to employeeType, "comp_id" to company.myId()),
         Boolean::class.java
      )!!

      logger.trace("Checking if Employee: {}/{}/{} exists resulted in {}", id, employeeType, company, exists)

      return exists
   }

   fun doesNotExist(user: User) =
      !exists(user)

   @Transactional
   fun insert(entity: EmployeeEntity): EmployeeEntity {
      logger.debug("Inserting employee {}", entity)

      return jdbc.insertReturning(
         """
         INSERT INTO employee(number, last_name, first_name_mi, pass_code, store_number, active, department, cynergi_system_admin, company_id, alternative_store_indicator, alternative_area)
         VALUES (:number, :last_name, :first_name_mi, :pass_code, :store_number, :active, :department, :cynergi_system_admin, :company_id, :alternative_store_indicator, :alternative_area)
         RETURNING
            *
         """.trimIndent(),
         mapOf(
            "number" to entity.number,
            "last_name" to entity.lastName,
            "first_name_mi" to entity.firstNameMi.trimToNull(), // not sure this is a good practice as it isn't being enforced by the database, but should be once the employee data is managed in PostgreSQL
            "pass_code" to passwordEncoderService.encode(entity.passCode),
            "active" to entity.active,
            "cynergi_system_admin" to entity.cynergiSystemAdmin,
            "company_id" to entity.company.myId(),
            "department" to entity.department?.myCode(),
            "store_number" to entity.store?.myNumber(),
            "alternative_store_indicator" to entity.alternativeStoreIndicator,
            "alternative_area" to entity.alternativeArea
         ),
         RowMapper { rs, _ ->
            mapDDLRow(rs, entity.company, entity.department, entity.store)
         }
      )
   }

   fun mapRow(rs: ResultSet, columnPrefix: String = "emp_", companyColumnPrefix: String = "comp_", departmentColumnPrefix: String = "dept_", storeColumnPrefix: String = "store_"): EmployeeEntity {
      return EmployeeEntity(
         id = rs.getLong("${columnPrefix}id"),
         type = rs.getString("${columnPrefix}type"),
         number = rs.getInt("${columnPrefix}number"),
         company = companyRepository.mapRow(rs, companyColumnPrefix),
         lastName = rs.getString("${columnPrefix}last_name"),
         firstNameMi = rs.getString("${columnPrefix}first_name_mi"), // FIXME fix query so that it isn't trimming stuff to null when employee is managed by PostgreSQL
         passCode = rs.getString("${columnPrefix}pass_code"),
         active = rs.getBoolean("${columnPrefix}active"),
         department = departmentRepository.mapRowOrNull(rs, companyRepository.mapRow(rs, companyColumnPrefix), departmentColumnPrefix),
         cynergiSystemAdmin = rs.getBoolean("${columnPrefix}cynergi_system_admin"),
         alternativeStoreIndicator = rs.getString("${columnPrefix}alternative_store_indicator"),
         alternativeArea = rs.getLong("${columnPrefix}alternative_area")
      )
   }

   fun mapRowOrNull(rs: ResultSet, columnPrefix: String = "emp_", companyPrefix: String = "comp_", departmentPrefix: String = "dept_", storePrefix: String = "store_"): EmployeeEntity? =
      try {
         if (rs.getString("${columnPrefix}id") != null) {
            mapRow(rs, columnPrefix, companyPrefix, departmentPrefix, storePrefix)
         } else {
            null
         }
      } catch (e: SQLException) {
         null
      }

   private fun mapDDLRow(rs: ResultSet, company: Company, department: Department?, store: Store?): EmployeeEntity =
      EmployeeEntity(
         id = rs.getLong("id"),
         type = "eli",
         number = rs.getInt("number"),
         lastName = rs.getString("last_name"),
         firstNameMi = rs.getString("first_name_mi"), // FIXME fix query so that it isn't trimming stuff to null when employee is managed by PostgreSQL
         passCode = rs.getString("pass_code"),
         active = rs.getBoolean("active"),
         cynergiSystemAdmin = rs.getBoolean("cynergi_system_admin"),
         company = company,
         department = department,
         store = store,
         alternativeStoreIndicator = rs.getString("alternative_store_indicator"),
         alternativeArea = rs.getLong("alternative_area")
      )
}
