package com.cynergisuite.middleware.employee.infrastructure

import com.cynergisuite.extensions.findFirstOrNull
import com.cynergisuite.extensions.insertReturning
import com.cynergisuite.extensions.trimToNull
import com.cynergisuite.middleware.authentication.PasswordEncoderService
import com.cynergisuite.middleware.company.Company
import com.cynergisuite.middleware.employee.EmployeeEntity
import com.cynergisuite.middleware.store.StoreEntity
import com.cynergisuite.middleware.store.infrastructure.StoreRepository
import io.micronaut.cache.annotation.Cacheable
import io.micronaut.spring.tx.annotation.Transactional
import io.reactiverse.pgclient.impl.ArrayTuple
import io.reactiverse.reactivex.pgclient.PgPool
import io.reactiverse.reactivex.pgclient.Tuple
import io.reactivex.Maybe
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
   private val jdbc: NamedParameterJdbcTemplate,
   private val passwordEncoderService: PasswordEncoderService,
   private val storeRepository: StoreRepository,
   private val postgresClient: PgPool
) {
   private val logger: Logger = LoggerFactory.getLogger(EmployeeRepository::class.java)

   fun selectBaseQuery(params: MutableMap<String, Any?>, company: Company, datasetParamKey: String = ":dataset"): String { // TODO may need to handle datasetParamkey better, but maybe not
      return "${selectBaseWithoutEmployeeStoreJoinQuery(params, company.myDataset(), datasetParamKey)} ON e.store_number = s.number"
   }

   private fun selectBaseWithoutEmployeeStoreJoinQuery(params: MutableMap<String, Any?>, dataset: String, datasetParamKey: String = ":dataset"): String {
      return """
         WITH employees AS (
            SELECT from_priority, id, number, dataset, last_name, first_name_mi, pass_code, store_number, active, department, employee_type, allow_auto_store_assign
            FROM (
               SELECT
                  1 AS from_priority,
                  fpie.id AS id,
                  fpie.number AS number,
                  fpie.last_name AS last_name,
                  fpie.first_name_mi AS first_name_mi,
                  fpie.pass_code AS pass_code,
                  fpie.store_number AS store_number,
                  fpie.active AS active,
                  fpie.department AS department,
                  FALSE AS allow_auto_store_assign,
                  'sysz' AS employee_type,
                  fpie.dataset AS dataset
               FROM fastinfo_prod_import.employee_vw fpie
               WHERE coalesce(trim(fpie.pass_code), '') <> ''
               UNION
               SELECT
                  2 AS from_priority,
                  e.id AS id,
                  e.number AS number,
                  e.last_name AS last_name,
                  e.first_name_mi AS first_name_mi,
                  e.pass_code AS pass_code,
                  e.store_number AS store_number,
                  e.active AS active,
                  e.department AS department,
                  e.allow_auto_store_assign AS allow_auto_store_assign,
                  'eli' AS employee_type,
                  e.dataset AS dataset
               FROM employee e
               WHERE coalesce(trim(e.pass_code), '') <> ''
            ) AS inner_emp
            ORDER BY from_priority
         ), stores AS (
            SELECT
               s.id AS id,
               s.number AS number,
               s.name AS name,
               s.dataset AS dataset
            FROM fastinfo_prod_import.store_vw s
            WHERE s.dataset = $datasetParamKey
         )
         SELECT
            e.from_priority AS e_priority,
            e.id AS e_id,
            e.number AS e_number,
            e.dataset AS e_dataset,
            e.last_name AS e_last_name,
            NULLIF(TRIM(e.first_name_mi), '') AS e_first_name_mi,
            e.pass_code AS e_pass_code,
            e.active AS e_active,
            e.department AS e_department,
            e.employee_type AS e_employee_type,
            e.allow_auto_store_assign AS e_allow_auto_store_assign,
            s.id AS s_id,
            s.number AS s_number,
            s.name AS s_name,
            s.dataset AS s_dataset,
            ds.id AS ds_id,
            ds.number AS ds_number,
            ds.name AS ds_name,
            ds.dataset AS ds_dataset
         FROM employees e
              JOIN stores ds ON ds.number = (SELECT coalesce(max(store_number), 9000) FROM fastinfo_prod_import.employee_vw)
              LEFT OUTER JOIN stores s
      """.trimIndent()
   }

   fun findOne(id: Long, employeeType: String, company: Company): EmployeeEntity? {
      val params = mutableMapOf<String, Any?>("id" to id, "employee_type" to employeeType)
      val query = "${selectBaseQuery(params, company)} WHERE e.id = :id AND e.employee_type = :employee_type"
      val found = jdbc.findFirstOrNull(query, params, RowMapper { rs, _ -> mapRow(rs, company) })

      logger.trace("Searching for Employee: {} {} {} resulted in {}", id, employeeType, company, found)

      return found
   }

   fun findOne(number: Int, employeeType: String? = null, company: Company): EmployeeEntity? {
      val params = mutableMapOf<String, Any?>("number" to number)
      val query = StringBuilder(selectBaseQuery(params, company)).append("\nWHERE e.number = :number")

      if (employeeType != null) {
         params["employee_type"] = employeeType
         query.append("\nAND employee_type = :employee_type")
      }

      query.append("\nORDER BY e.from_priority\n LIMIT 1")

      logger.debug("Searching for employee {}, {} with {}", number, employeeType, query)

      val found = jdbc.findFirstOrNull(query.toString(), params, RowMapper { rs, _ -> mapRow(rs, company) })

      logger.trace("Searching for Employee: {} {} resulted in {}", number, employeeType, found)

      return found
   }

   @Cacheable("user-cache")
   fun findOne(user: AuthenticatedUser): EmployeeEntity? {
      val params = mutableMapOf("id" to user.myId(), "employee_type" to user.myEmployeeType(), "store_number" to user.myStoreNumber())
      val query = """
         ${selectBaseWithoutEmployeeStoreJoinQuery(params, user.myCompany())}
            ON s.number = :store_number
         WHERE e.id = :id
               AND e.employee_type = :employee_type
               AND e.dataset = :dataset
         """.trimIndent()

      logger.debug("Searching for Employee: {} with {} using {}", user, params, query)

      val found = jdbc.findFirstOrNull(query, params, RowMapper { rs, _ -> mapRow(rs, user.myCompany()) } )

      logger.trace("Searching for Employee: {} resulted in {}", user, found)

      return found
   }

   fun exists(id: Long, employeeType: String = "sysz", company: Company): Boolean {
      val query = """
      SELECT count(id) = 1
      FROM (
         SELECT id, 'sysz' AS employee_type, dataset AS dataset FROM fastinfo_prod_import.employee_vw fpie WHERE coalesce(trim(fpie.pass_code), '') <> ''
         UNION
         SELECT id, 'eli' AS employee_type, dataset AS dataset FROM employee e
      ) AS e
      WHERE e.id = :id
            AND employee_type = :employee_type
            AND dataset = :dataset
      """
      val exists = jdbc.queryForObject(query, mapOf("id" to id, "employee_type" to employeeType, "dataset" to dataset), Boolean::class.java)!!

      logger.trace("Checking if Employee: {} exists resulted in {}", id, exists)

      return exists
   }



   @Transactional
   fun insert(entity: EmployeeEntity): EmployeeEntity {
      logger.debug("Inserting employee {}", entity)

      return jdbc.insertReturning("""
         INSERT INTO employee(number, last_name, first_name_mi, pass_code, store_number, active, department, allow_auto_store_assign, company_id)
         VALUES (:number, :last_name, :first_name_mi, :pass_code, :store_number, :active, :department, :allow_auto_store_assign, :company_id)
         RETURNING
            *
         """.trimIndent(),
         mapOf(
            "number" to entity.number,
            "last_name" to entity.lastName,
            "first_name_mi" to entity.firstNameMi.trimToNull(), // not sure this is a good practice as it isn't being enforced by the database, but should be once the employee data is managed in PostgreSQL
            "pass_code" to passwordEncoderService.encode(entity.passCode),
            "store_number" to entity.store?.number,
            "active" to entity.active,
            "department" to entity.department,
            "allow_auto_store_assign" to entity.allowAutoStoreAssign,
            "company_id" to entity.company.myId()
         ),
         RowMapper { rs, _ ->
            mapDDLRow(rs, entity.company, entity.store)
         }
      )
   }

   fun mapRow(rs: ResultSet, company: Company, columnPrefix: String = "e_", storeColumnPrefix: String = "s_"): EmployeeEntity  =
      EmployeeEntity(
         id = rs.getLong("${columnPrefix}id"),
         type = rs.getString("${columnPrefix}employee_type"),
         number = rs.getInt("${columnPrefix}number"),
         company = company,
         lastName = rs.getString("${columnPrefix}last_name"),
         firstNameMi = rs.getString("${columnPrefix}first_name_mi"),  // FIXME fix query so that it isn't trimming stuff to null when employee is managed by PostgreSQL
         passCode = rs.getString("${columnPrefix}pass_code"),
         store = storeRepository.mapRowOrNull(rs, storeColumnPrefix),
         active = rs.getBoolean("${columnPrefix}active"),
         department = rs.getString("${columnPrefix}department"),
         allowAutoStoreAssign = rs.getBoolean("${columnPrefix}allow_auto_store_assign")
      )

   fun mapRowOrNull(rs: ResultSet, company: Company, columnPrefix: String = "e_", storeColumnPrefix: String = "s_"): EmployeeEntity?  =
      if (rs.getString("${columnPrefix}id") != null) {
         mapRow(rs, company, columnPrefix)
      } else {
         null
      }

   private fun mapDDLRow(rs: ResultSet, company: Company, store: StoreEntity?) : EmployeeEntity =
      EmployeeEntity(
         id = rs.getLong("id"),
         type = "eli",
         number = rs.getInt("number"),
         company = company,
         lastName = rs.getString("last_name"),
         firstNameMi = rs.getString("first_name_mi"), // FIXME fix query so that it isn't trimming stuff to null when employee is managed by PostgreSQL
         passCode = rs.getString("pass_code"),
         store = store,
         active = rs.getBoolean("active"),
         department = rs.getString("department")
      )
}
