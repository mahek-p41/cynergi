package com.cynergisuite.middleware.employee.infrastructure

import com.cynergisuite.domain.PageRequest
import com.cynergisuite.domain.infrastructure.RepositoryPage
import com.cynergisuite.extensions.findFirstOrNull
import com.cynergisuite.extensions.insertReturning
import com.cynergisuite.extensions.query
import com.cynergisuite.extensions.queryForObject
import com.cynergisuite.extensions.trimToNull
import com.cynergisuite.middleware.authentication.user.AuthenticatedEmployee
import com.cynergisuite.middleware.authentication.user.User
import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.company.infrastructure.CompanyRepository
import com.cynergisuite.middleware.department.DepartmentEntity
import com.cynergisuite.middleware.department.infrastructure.DepartmentRepository
import com.cynergisuite.middleware.employee.EmployeeEntity
import com.cynergisuite.middleware.employee.EmployeePageRequest
import com.cynergisuite.middleware.store.Store
import com.cynergisuite.middleware.store.infrastructure.StoreRepository
import io.micronaut.transaction.annotation.ReadOnly
import jakarta.inject.Inject
import jakarta.inject.Singleton
import org.jdbi.v3.core.Jdbi
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.sql.ResultSet
import javax.transaction.Transactional

/* FIXME
 * Notes for the future
 * 1. Due to current practices in the data for employees first_name_mi can be blank rather than null.  This should be
 *    fixed so that it is more consistent in the database and is not smoothed over by the business logic.
 */
@Singleton
class EmployeeRepository @Inject constructor(
   private val companyRepository: CompanyRepository,
   private val departmentRepository: DepartmentRepository,
   private val jdbc: Jdbi,
   private val storeRepository: StoreRepository,
) {
   private val logger: Logger = LoggerFactory.getLogger(EmployeeRepository::class.java)

   private fun employeeBaseQuery() = "SELECT * FROM system_employees_fimvw"

   @ReadOnly
   fun findOne(user: User): EmployeeEntity? =
      findOne(user.myId(), user.myEmployeeType(), user.myCompany())

   @ReadOnly
   fun findOne(id: Long, employeeType: String, company: CompanyEntity): EmployeeEntity? {
      logger.trace("Searching for Employee: {} {} {}", id, employeeType, company)

      val found = jdbc.findFirstOrNull(
         "${employeeBaseQuery()} WHERE comp_id = :comp_id AND emp_id = :emp_id AND emp_type = :emp_type",
         mutableMapOf("comp_id" to company.id, "emp_id" to id, "emp_type" to employeeType)
      ) { rs, _ -> mapRow(rs) }

      logger.trace("Searching for Employee: {} {} {} resulted in {}", id, employeeType, company, found)

      return found
   }

   @ReadOnly
   fun findOne(id: Long, company: CompanyEntity): EmployeeEntity? {
      val found = jdbc.findFirstOrNull(
         "${employeeBaseQuery()} WHERE comp_id = :comp_id AND emp_id = :emp_id LIMIT 1",
         mutableMapOf("comp_id" to company.id, "emp_id" to id)
      ) { rs, _ -> mapRow(rs) }

      logger.trace("Searching for Employee: {} resulted in {}", id, company, found)

      return found
   }

   @ReadOnly
   fun findOne(number: Int, employeeType: String, company: CompanyEntity): EmployeeEntity? {
      logger.debug("Searching for employee with {} {} {}", number, employeeType, company)

      val found = jdbc.findFirstOrNull(
         "${employeeBaseQuery()} WHERE emp_number = :emp_number AND emp_type = :emp_type AND comp_id = :comp_id",
         mapOf("emp_number" to number, "emp_type" to employeeType, "comp_id" to company.id)
      ) { rs, _ -> mapRow(rs) }

      logger.trace("Searching for Employee: {} {} resulted in {}", number, employeeType, found)

      return found
   }

   @ReadOnly
   fun findOne(user: AuthenticatedEmployee): EmployeeEntity? {
      logger.debug("Searching for Employee using {}", user)

      val found = jdbc.findFirstOrNull(
         """
         ${employeeBaseQuery()}
         WHERE emp_id = :id
               AND emp_type = :employee_type
               AND comp_id = :comp_id
         """,
         mutableMapOf("id" to user.myId(), "employee_type" to user.myEmployeeType(), "comp_id" to user.myCompany().id)
      ) { rs, _ -> mapRow(rs) }

      logger.trace("Searching for Employee: {} resulted in {}", user, found)

      return found
   }

   @ReadOnly
   fun findAll(pageRequest: EmployeePageRequest, company: CompanyEntity): RepositoryPage<EmployeeEntity, PageRequest> {
      val firstNameMi = pageRequest.firstNameMi
      val lastName = pageRequest.lastName
      val searchString = pageRequest.search
      val params = mutableMapOf(
         "comp_id" to company.id,
         "limit" to pageRequest.size(),
         "offset" to pageRequest.offset(),
         "firstNameMi" to firstNameMi,
         "lastName" to lastName,
         "search_query" to searchString
      )
      var totalElements: Long? = null
      val elements = mutableListOf<EmployeeEntity>()
      val where = StringBuilder(" WHERE comp_id = :comp_id AND emp_cynergi_system_admin = false ")
      val and = " AND "
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

      jdbc.query(query, params) { rs, _ ->
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

   @ReadOnly
   fun exists(user: User): Boolean =
      exists(user.myId(), user.myEmployeeType(), user.myCompany())

   @ReadOnly
   fun exists(id: Long, employeeType: String, company: CompanyEntity): Boolean {
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
                  JOIN company comp ON emp.dataset = comp.dataset_code AND comp.deleted = FALSE
                  LEFT OUTER JOIN fastinfo_prod_import.department_vw dept ON comp.dataset_code = dept.dataset AND emp.department = dept.code
                  LEFT OUTER JOIN system_stores_fimvw store ON comp.dataset_code = store.dataset AND emp.store_number = store.number
               UNION
               SELECT
                  2 AS from_priority,
                  emp.id AS emp_id,
                  'eli' AS emp_type,
                  comp.id AS comp_id
               FROM employee emp
                  JOIN company comp ON emp.company_id = comp.id AND comp.deleted = FALSE
                  LEFT OUTER JOIN fastinfo_prod_import.department_vw dept ON comp.dataset_code = dept.dataset AND emp.department = dept.code
                  LEFT OUTER JOIN system_stores_fimvw store ON comp.dataset_code = store.dataset AND emp.store_number = store.number
            ) AS inner_employees
            ORDER BY from_priority
         ) AS employees
         WHERE emp_id = :emp_id AND emp_type = :emp_type AND comp_id = :comp_id""",
         mapOf("emp_id" to id, "emp_type" to employeeType, "comp_id" to company.id),
         Boolean::class.java
      )

      logger.trace("Checking if Employee: {}/{}/{} exists resulted in {}", id, employeeType, company, exists)

      return exists
   }

   @ReadOnly
   fun exists(id: Long, company: CompanyEntity): Boolean {
      val exists = jdbc.queryForObject(
         """
         SELECT count(emp_id) = 1 FROM (
            SELECT emp_id, comp_id FROM (
               SELECT
                  1 AS from_priority,
                  emp.id AS emp_id,
                  comp.id AS comp_id
               FROM fastinfo_prod_import.employee_vw emp
                  JOIN company comp ON emp.dataset = comp.dataset_code AND comp.deleted = FALSE
                  LEFT OUTER JOIN fastinfo_prod_import.department_vw dept ON comp.dataset_code = dept.dataset AND emp.department = dept.code
                  LEFT OUTER JOIN fastinfo_prod_import.store_vw store ON comp.dataset_code = store.dataset AND emp.store_number = store.number
               UNION
               SELECT
                  2 AS from_priority,
                  emp.id AS emp_id,
                  comp.id AS comp_id
               FROM employee emp
                  JOIN company comp ON emp.company_id = comp.id AND comp.deleted = FALSE
                  LEFT OUTER JOIN fastinfo_prod_import.department_vw dept ON comp.dataset_code = dept.dataset AND emp.department = dept.code
                  LEFT OUTER JOIN fastinfo_prod_import.store_vw store ON comp.dataset_code = store.dataset AND emp.store_number = store.number
            ) AS inner_employees
            ORDER BY from_priority
         ) AS employees
         WHERE emp_id = :emp_id AND comp_id = :comp_id""",
         mapOf("emp_id" to id, "comp_id" to company.id),
         Boolean::class.java
      )

      logger.trace("Checking if Employee: {}/{}/{} exists resulted in {}", id, company, exists)

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
         VALUES (:number, :last_name, :first_name_mi, hash_passcode(:pass_code), :store_number, :active, :department, :cynergi_system_admin, :company_id, :alternative_store_indicator, :alternative_area)
         RETURNING
            *
         """.trimIndent(),
         mapOf(
            "number" to entity.number,
            "last_name" to entity.lastName,
            "first_name_mi" to entity.firstNameMi.trimToNull(), // not sure this is a good practice as it isn't being enforced by the database, but should be once the employee data is managed in PostgreSQL
            "pass_code" to entity.passCode,
            "active" to entity.active,
            "cynergi_system_admin" to entity.cynergiSystemAdmin,
            "company_id" to entity.company.id,
            "department" to entity.department?.myCode(),
            "store_number" to entity.store?.myNumber(),
            "alternative_store_indicator" to entity.alternativeStoreIndicator,
            "alternative_area" to entity.alternativeArea
         )
      ) { rs, _ ->
         mapDDLRow(rs, entity.company, entity.department, entity.store)
      }
   }

   fun mapRow(
      rs: ResultSet,
      columnPrefix: String = "emp_",
      companyColumnPrefix: String = "comp_",
      companyAddressColumnPrefix: String = "address_",
      departmentColumnPrefix: String = "dept_",
      storeColumnPrefix: String = "store_"
   ): EmployeeEntity {
      val company = companyRepository.mapRow(rs, companyColumnPrefix, companyAddressColumnPrefix)

      return EmployeeEntity(
         id = rs.getLong("${columnPrefix}id"),
         type = rs.getString("${columnPrefix}type"),
         number = rs.getInt("${columnPrefix}number"),
         company = company,
         lastName = rs.getString("${columnPrefix}last_name"),
         firstNameMi = rs.getString("${columnPrefix}first_name_mi"), // FIXME fix query so that it isn't trimming stuff to null when employee is managed by PostgreSQL
         passCode = rs.getString("${columnPrefix}pass_code"),
         store = storeRepository.mapRowOrNull(rs, company, storeColumnPrefix),
         active = rs.getBoolean("${columnPrefix}active"),
         department = departmentRepository.mapRowOrNull(rs, company, departmentColumnPrefix),
         cynergiSystemAdmin = rs.getBoolean("${columnPrefix}cynergi_system_admin"),
         alternativeStoreIndicator = rs.getString("${columnPrefix}alternative_store_indicator"),
         alternativeArea = rs.getLong("${columnPrefix}alternative_area")
      )
   }

   fun mapRowOrNull(
      rs: ResultSet,
      columnPrefix: String = "emp_",
      companyColumnPrefix: String = "comp_",
      companyAddressColumnPrefix: String = "comp_address_",
      departmentColumnPrefix: String = "dept_",
      storeColumnPrefix: String = "store_"
   ): EmployeeEntity? =
      if (rs.getString("${columnPrefix}id") != null) {
         mapRow(rs, columnPrefix, companyColumnPrefix, companyAddressColumnPrefix, departmentColumnPrefix, storeColumnPrefix)
      } else {
         null
      }

   private fun mapDDLRow(rs: ResultSet, company: CompanyEntity, department: DepartmentEntity?, store: Store?): EmployeeEntity =
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

   @ReadOnly
   fun findPurchaseOrderApprovers(user: User): List<EmployeeEntity> {
      val params = mapOf("comp_id" to user.myCompany().id)
      val query =
         """
         WITH emp AS (
            ${employeeBaseQuery()}
         )
         SELECT
            emp.*
         FROM
            fastinfo_prod_import.operator_vw opr
               JOIN emp ON opr.number = emp.emp_number AND opr.dataset = emp.comp_dataset_code
         WHERE comp_id = :comp_id AND COALESCE(purchase_order_security, 0) >= (
            SELECT m.level
            FROM module m
                  JOIN module_type_domain type ON m.module_type_id = type.id
            WHERE m.company_id = :comp_id AND type.value = 'POCHG'
         )
      """
      logger.trace("Fetching all purchase order approvers using {} / {}", query, params)

      val elements = mutableListOf<EmployeeEntity>()

      jdbc.query(query, params) { rs, _ ->
         elements.add(mapRow(rs))
      }

      return elements
   }
}
