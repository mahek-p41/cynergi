package com.cynergisuite.middleware.employee.infrastructure

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
import com.cynergisuite.middleware.store.StoreEntity
import com.cynergisuite.middleware.store.infrastructure.StoreRepository
import io.micronaut.spring.tx.annotation.Transactional
import io.reactiverse.reactivex.pgclient.PgPool
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.sql.ResultSet
import javax.inject.Inject
import javax.inject.Singleton

/* FIXME
 * Notes for the future
 * 1. Due to current practices in the data for employees first_name_mi can be blank rather than null.  This should be
 *    fixed so that it is more consistent in the database and is not smoothed over by the business logic.
 */
@Singleton
class EmployeeRepository @Inject constructor(
   private val companyRepository: CompanyRepository,
   private val departmentRepository: DepartmentRepository,
   private val jdbc: NamedParameterJdbcTemplate,
   private val storeRepository: StoreRepository,
   private val passwordEncoderService: PasswordEncoderService,
   private val postgresClient: PgPool
) {
   private val logger: Logger = LoggerFactory.getLogger(EmployeeRepository::class.java)

   public fun employeeBaseQuery() = """
      SELECT * FROM (
         SELECT * FROM (
            SELECT
               1 AS from_priority,
               emp.id AS emp_id,
               'sysz' AS emp_type,
               emp.number AS emp_number,
               emp.last_name AS emp_last_name,
               emp.first_name_mi AS emp_first_name_mi,
               emp.pass_code AS emp_pass_code,
               emp.active AS emp_active,
               false AS emp_cynergi_system_admin,
               comp.id AS comp_id,
               comp.uu_row_id AS comp_uu_row_id,
               comp.time_created AS comp_time_created,
               comp.time_updated AS comp_time_updated,
               comp.name AS comp_name,
               comp.doing_business_as AS comp_doing_business_as,
               comp.client_code AS comp_client_code,
               comp.client_id AS comp_client_id,
               comp.dataset_code AS comp_dataset_code,
               comp.federal_id_number AS comp_federal_id_number,
               dept.id AS dept_id,
               dept.code AS dept_code,
               dept.description AS dept_description,
               dept.security_profile AS dept_security_profile,
               dept.default_menu AS dept_default_menu,
               fpis.id AS fpis_id,
               fpis.number AS fpis_number,
               fpis.name AS fpis_name
            FROM company comp
               JOIN fastinfo_prod_import.employee_vw emp ON comp.dataset_code = emp.dataset
               LEFT OUTER JOIN fastinfo_prod_import.department_vw dept ON comp.dataset_code = dept.dataset AND emp.department = dept.code
               LEFT OUTER JOIN fastinfo_prod_import.store_vw fpis ON comp.dataset_code = fpis.dataset AND emp.store_number = fpis.number
            UNION
            SELECT
               2 AS from_priority,
               emp.id AS emp_id,
               'eli' AS emp_type,
               emp.number AS emp_number,
               emp.last_name AS emp_last_name,
               emp.first_name_mi AS emp_first_name_mi,
               emp.pass_code AS emp_pass_code,
               emp.active AS emp_active,
               emp.cynergi_system_admin AS emp_cynergi_system_admin,
               comp.id AS comp_id,
               comp.uu_row_id AS comp_uu_row_id,
               comp.time_created AS comp_time_created,
               comp.time_updated AS comp_time_updated,
               comp.name AS comp_name,
               comp.doing_business_as AS comp_doing_business_as,
               comp.client_code AS comp_client_code,
               comp.client_id AS comp_client_id,
               comp.dataset_code AS comp_dataset_code,
               comp.federal_id_number AS comp_federal_id_number,
               dept.id AS dept_id,
               dept.code AS dept_code,
               dept.description AS dept_description,
               dept.security_profile AS dept_security_profile,
               dept.default_menu AS dept_default_menu,
               fpis.id AS fpis_id,
               fpis.number AS fpis_number,
               fpis.name AS fpis_name
            FROM company comp
               JOIN employee emp ON comp.id = emp.company_id
               LEFT OUTER JOIN fastinfo_prod_import.department_vw dept ON comp.dataset_code = dept.dataset AND emp.department = dept.code
               LEFT OUTER JOIN fastinfo_prod_import.store_vw fpis ON comp.dataset_code = fpis.dataset AND emp.store_number = fpis.number
         ) AS inner_employees
         ORDER BY from_priority
      ) AS employees
   """

   fun findOne(user: User): EmployeeEntity? =
      findOne(user.myId()!!, user.myEmployeeType(), user.myCompany())

   fun findOne(id: Long, employeeType: String, company: Company): EmployeeEntity? {
      val found = jdbc.findFirstOrNull(
         "${employeeBaseQuery()} WHERE comp_id = :comp_id AND emp_id = :emp_id AND emp_type = :emp_type",
         mutableMapOf("comp_id" to company.myId(), "emp_id" to id, "emp_type" to employeeType),
         RowMapper { rs, _ -> mapRow(rs) }
      )

      logger.trace("Searching for Employee: {} {} {} resulted in {}", id, employeeType, company, found)

      return found
   }

   fun findOne(number: Int, employeeType: String = "sysz", company: Company): EmployeeEntity? {
      logger.debug("Searching for employee with {} {} {}", number, employeeType, company)

      val found = jdbc.findFirstOrNull(
         "${employeeBaseQuery()} WHERE emp_number = :number AND emp_type = :emp_type LIMIT 1",
         mapOf("emp_number" to number, "emp_type" to employeeType),
         RowMapper { rs, _ -> mapRow(rs) }
      )

      logger.trace("Searching for Employee: {} {} resulted in {}", number, employeeType, found)

      return found
   }

   fun findOne(user: AuthenticatedUser): EmployeeEntity? {
      logger.debug("Searching for Employee using {}", user)

      val found = jdbc.findFirstOrNull("""
         ${employeeBaseQuery()}
         WHERE emp_id = :id
               AND emp_employee_type = :employee_type
               AND comp_id = :comp_id
         """,
         mutableMapOf("id" to user.myId(), "employee_type" to user.myEmployeeType(), "store_number" to user.myCompany().myId()),
         RowMapper { rs, _ -> mapRow(rs) }
      )

      logger.trace("Searching for Employee: {} resulted in {}", user, found)

      return found
   }

   fun exists(user: User): Boolean =
      exists(user.myId()!!, user.myEmployeeType(), user.myCompany())

   fun exists(id: Long, employeeType: String = "sysz", company: Company): Boolean {
      val exists = jdbc.queryForObject("""
         SELECT count(emp_id) = 1 FROM (
            SELECT * FROM (
               SELECT
                  1 AS from_priority,
                  emp.id AS emp_id,
                  'sysz' AS emp_type,
                  comp.id AS comp_id
               FROM company comp
                  JOIN fastinfo_prod_import.employee_vw emp ON comp.dataset_code = emp.dataset
                  JOIN fastinfo_prod_import.department_vw dept ON comp.dataset_code = dept.dataset AND emp.department = dept.code
                  LEFT OUTER JOIN fastinfo_prod_import.store_vw fpis ON comp.dataset_code = fpis.dataset AND emp.store_number = fpis.number
               UNION
               SELECT
                  2 AS from_priority,
                  emp.id AS emp_id,
                  'eli' AS emp_type,
                  comp.id AS comp_id
               FROM company comp
                  JOIN employee emp ON comp.id = emp.company_id
                  JOIN fastinfo_prod_import.department_vw dept ON comp.dataset_code = dept.dataset AND emp.department = dept.code
                  LEFT OUTER JOIN fastinfo_prod_import.store_vw fpis ON comp.dataset_code = fpis.dataset AND emp.store_number = fpis.number
            ) AS inner_employees
            ORDER BY from_priority
         ) AS employees
         WHERE emp_id = :emp_id AND emp_type = :emp_type AND comp_id = :comp_id""",
         mapOf("emp_id" to id, "emp_type" to employeeType, "comp_id" to company.myId()),
         Boolean::class.java
      )!!

      logger.trace("Checking if Employee: {} exists resulted in {}", id, exists)

      return exists
   }

   fun doesNotExist(user: User) =
      !exists(user)

   @Transactional
   fun insert(entity: EmployeeEntity): EmployeeEntity {
      logger.debug("Inserting employee {}", entity)

      return jdbc.insertReturning("""
         INSERT INTO employee(number, last_name, first_name_mi, pass_code, store_number, active, department, cynergi_system_admin, company_id)
         VALUES (:number, :last_name, :first_name_mi, :pass_code, :store_number, :active, :department, :cynergi_system_admin, :company_id)
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
            "store_number" to entity.store?.number
         ),
         RowMapper { rs, _ ->
            mapDDLRow(rs, entity.company, entity.department, entity.store)
         }
      )
   }

   fun mapRow(rs: ResultSet, columnPrefix: String = "e_", companyColumnPrefix: String = "c_", departmentColumnPrefix: String = "d_", storeColumnPrefix: String = "s_"): EmployeeEntity {
      val company = companyRepository.mapRow(rs, companyColumnPrefix)

      return EmployeeEntity(
         id = rs.getLong("${columnPrefix}id"),
         type = rs.getString("${columnPrefix}employee_type"),
         number = rs.getInt("${columnPrefix}number"),
         company = company,
         lastName = rs.getString("${columnPrefix}last_name"),
         firstNameMi = rs.getString("${columnPrefix}first_name_mi"),  // FIXME fix query so that it isn't trimming stuff to null when employee is managed by PostgreSQL
         passCode = rs.getString("${columnPrefix}pass_code"),
         store = storeRepository.mapRowOrNull(rs, company, storeColumnPrefix),
         active = rs.getBoolean("${columnPrefix}active"),
         department = departmentRepository.mapRow(rs, company, departmentColumnPrefix),
         cynergiSystemAdmin = rs.getBoolean("${columnPrefix}cynergi_system_admin")
      )
   }

   fun mapRowOrNull(rs: ResultSet, columnPrefix: String = "e_", companyColumnPrefix: String = "c", departmentColumnPrefix: String = "d_", storeColumnPrefix: String = "s_"): EmployeeEntity?  =
      if (rs.getString("${columnPrefix}id") != null) {
         mapRow(rs, columnPrefix, companyColumnPrefix, departmentColumnPrefix, storeColumnPrefix)
      } else {
         null
      }

   private fun mapDDLRow(rs: ResultSet, company: Company, department: Department?, store: StoreEntity?) : EmployeeEntity =
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
         store = store
      )
}
