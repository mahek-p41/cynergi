package com.cynergisuite.middleware.employee.infrastructure

import com.cynergisuite.extensions.findFirstOrNull
import com.cynergisuite.extensions.getOffsetDateTime
import com.cynergisuite.extensions.insertReturning
import com.cynergisuite.extensions.trimToNull
import com.cynergisuite.middleware.authentication.AuthenticatedUser
import com.cynergisuite.middleware.authentication.PasswordEncoderService
import com.cynergisuite.middleware.employee.EmployeeEntity
import com.cynergisuite.middleware.store.StoreEntity
import com.cynergisuite.middleware.store.infrastructure.StoreRepository
import io.micronaut.cache.annotation.Cacheable
import io.micronaut.spring.tx.annotation.Transactional
import io.reactiverse.reactivex.pgclient.PgPool
import io.reactiverse.reactivex.pgclient.Tuple
import io.reactivex.Maybe
import org.intellij.lang.annotations.Language
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.lang.StringBuilder
import java.sql.ResultSet
import java.time.OffsetDateTime
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

   @Language("PostgreSQL")
   private val selectBaseWithoutEmployeeStoreJoin = """
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
         ${storeRepository.selectBase}
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

   val selectBase = "$selectBaseWithoutEmployeeStoreJoin ON e.store_number = s.number"

   fun findOne(id: Long, employeeType: String, dataset: String): EmployeeEntity? {
      val found = jdbc.findFirstOrNull("$selectBase\nWHERE e.id = :id AND e.employee_type = :employee_type", mapOf("id" to id, "employee_type" to employeeType, "dataset" to dataset), RowMapper { rs, _ -> mapRow(rs) })

      logger.trace("Searching for Employee: {} {} {} resulted in {}", id, employeeType, dataset, found)

      return found
   }

   fun findOne(number: Int, employeeType: String? = null, dataset: String): EmployeeEntity? {
      val params = mutableMapOf<String, Any>("number" to number)
      val query = StringBuilder(selectBase)
         .append("\nWHERE e.number = :number")

      if (employeeType != null) {
         params["employee_type"] = employeeType
         query.append("\nAND employee_type = :employee_type")
      }

      query.append("\nORDER BY e.from_priority\n LIMIT 1")

      logger.debug("Searching for employee {}, {} with {}", number, employeeType, query)

      val found = jdbc.findFirstOrNull(query.toString(), params, RowMapper { rs, _ -> mapRow(rs) })

      logger.trace("Searching for Employee: {} {} resulted in {}", number, employeeType, found)

      return found
   }

   fun findOne(user: AuthenticatedUser): EmployeeEntity? {
      val found = jdbc.findFirstOrNull("""
         $selectBaseWithoutEmployeeStoreJoin
            ON s.number = :store_number
         WHERE e.id = :id
               AND e.employee_type = :employee_type
         """.trimIndent(),
         mapOf(
            "id" to user.myId(),
            "employee_type" to user.myEmployeeType(),
            "store_number" to user.myStoreNumber(),
            "dataset" to user.myDataset()
         ),
         RowMapper { rs, _ -> mapRow(rs) }
      )

      logger.trace("Searching for Employee: {} resulted in {}", user, found)

      return found
   }

   fun exists(id: Long, employeeType: String): Boolean {
      val exists = jdbc.queryForObject("SELECT EXISTS(SELECT id FROM ($selectBase) AS emp_exists WHERE emp_exists.id = :id)", mapOf("id" to id), Boolean::class.java)!!

      logger.trace("Checking if Employee: {} exists resulted in {}", id, exists)

      return exists
   }

   /**
    * This method returns a PG Reactive Single Employee that is really meant to be used only for authentication as it
    * unions together the cynergidb.employee table as well as the view referenced by the Foreign Data Wrapper that is
    * pointed at FastInfo to pull in Zortec data about an Employee
    */
   fun findUserByAuthentication(number: Int, passCode: String, dataset: String, storeNumber: Int?): Maybe<EmployeeEntity> {
      logger.trace("Checking authentication for {} {}", number, storeNumber)

      val tuple: Tuple
      val query = if (storeNumber != null) {
         tuple = Tuple.of(storeNumber, number, dataset)

         """
         ${selectBaseWithoutEmployeeStoreJoin.replace(":dataset", "$3")}
            ON s.number = $1
         WHERE e.number = $2
            AND e.active = true
         ORDER BY e.from_priority
         """.trimIndent()
      } else {
         tuple = Tuple.of(number, dataset)

         """
         ${selectBase.replace(":dataset", "$2")}
         WHERE e.number = $1
            AND e.active = true
         ORDER BY e.from_priority
         """.trimIndent()
      }

      logger.trace("Checking authentication for {} {} using {}", number, storeNumber, query)

      return postgresClient.rxPreparedQuery(query, tuple)
         .filter { rs -> rs.size() > 0 }
         .map { rs ->
            val iterator = rs.iterator()
            val row = iterator.next()

            val defaultStore = storeRepository.mapRow(row, "ds_")
            val employee = EmployeeEntity(
               id = row.getLong("e_id"),
               type = row.getString("e_employee_type"),
               number = row.getInteger("e_number"),
               dataset = row.getString("e_dataset"),
               lastName = row.getString("e_last_name"),
               firstNameMi = row.getString("e_first_name_mi"),
               passCode = row.getString("e_pass_code"),
               store = storeRepository.mapRow(row, "s_"),
               active = row.getBoolean("e_active"),
               allowAutoStoreAssign = row.getBoolean("e_allow_auto_store_assign")
            )

            employee to defaultStore
         }
         .filter { (employee, _) ->
            if (employee.type == "eli") {
               passwordEncoderService.matches(passCode, employee.passCode)
            } else {
               employee.passCode == passCode // FIXME remove this when all users are loaded out of cynergidb and are encoded by BCrypt
            }
         }
         .filter { (employee, _) -> employee.store != null || employee.allowAutoStoreAssign } // FIXME reconsider this filter
         .map { (employee, defaultStore) ->
            if (employee.store == null && employee.allowAutoStoreAssign) {
               logger.debug("Employee {} is allowed to auto store assign using {}", number, defaultStore)

               employee.copy(store = defaultStore)
            } else {
               logger.debug("Employee {} is not allowed to auto store assign", number)

               employee
            }
         }
   }

   @Transactional
   fun insert(entity: EmployeeEntity): EmployeeEntity {
      logger.debug("Inserting employee {}", entity)

      return jdbc.insertReturning("""
         INSERT INTO employee(number, last_name, first_name_mi, pass_code, store_number, active, allow_auto_store_assign, dataset)
         VALUES (:number, :last_name, :first_name_mi, :pass_code, :store_number, :active, :allow_auto_store_assign, :dataset)
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
            "allow_auto_store_assign" to entity.allowAutoStoreAssign,
            "dataset" to entity.dataset
         ),
         RowMapper { rs, _ ->
            mapDDLRow(rs, entity.store)
         }
      )
   }

   @Cacheable("user-cache")
   fun canEmployeeAccess(employeeType: String, asset: String, id: Long): Boolean {
      logger.debug("Check if user {} has access to asset {} via the database", id, asset)

      return if(asset == "check") { // everyone authenticated should be able to access this asset
         true
      } else {
         true // TODO do this check once the appropriate data from the menu/modules conversion is in place
      }
   }

   fun mapRow(rs: ResultSet, columnPrefix: String = "e_", storeColumnPrefix: String = "s_"): EmployeeEntity  =
      EmployeeEntity(
         id = rs.getLong("${columnPrefix}id"),
         type = rs.getString("${columnPrefix}employee_type"),
         number = rs.getInt("${columnPrefix}number"),
         dataset = rs.getString("${columnPrefix}dataset"),
         lastName = rs.getString("${columnPrefix}last_name"),
         firstNameMi = rs.getString("${columnPrefix}first_name_mi"),  // FIXME fix query so that it isn't trimming stuff to null when employee is managed by PostgreSQL
         passCode = rs.getString("${columnPrefix}pass_code"),
         store = storeRepository.mapRowOrNull(rs, storeColumnPrefix),
         active = rs.getBoolean("${columnPrefix}active"),
         department = rs.getString("${columnPrefix}department"),
         allowAutoStoreAssign = rs.getBoolean("${columnPrefix}allow_auto_store_assign")
      )

   fun mapRowOrNull(rs: ResultSet, columnPrefix: String = "e_", storeColumnPrefix: String = "s_"): EmployeeEntity?  =
      if (rs.getString("${columnPrefix}id") != null) {
         mapRow(rs, columnPrefix)
      } else {
         null
      }

   private fun mapDDLRow(rs: ResultSet, store: StoreEntity?) : EmployeeEntity =
      EmployeeEntity(
         id = rs.getLong("id"),
         type = "eli",
         number = rs.getInt("number"),
         dataset = "cyneli",
         lastName = rs.getString("last_name"),
         firstNameMi = rs.getString("first_name_mi"), // FIXME fix query so that it isn't trimming stuff to null when employee is managed by PostgreSQL
         passCode = rs.getString("pass_code"),
         store = store,
         active = rs.getBoolean("active"),
         department = rs.getString("department")
      )
}
