package com.cynergisuite.middleware.employee.infrastructure

import com.cynergisuite.extensions.findFirstOrNull
import com.cynergisuite.extensions.getOffsetDateTime
import com.cynergisuite.extensions.insertReturning
import com.cynergisuite.extensions.trimToNull
import com.cynergisuite.extensions.updateReturning
import com.cynergisuite.middleware.employee.Employee
import com.cynergisuite.middleware.store.Store
import com.cynergisuite.middleware.store.infrastructure.StoreRepository
import io.micronaut.cache.annotation.Cacheable
import io.micronaut.spring.tx.annotation.Transactional
import io.reactiverse.reactivex.pgclient.PgPool
import io.reactiverse.reactivex.pgclient.Tuple
import io.reactivex.Maybe
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.sql.ResultSet
import java.time.OffsetDateTime
import javax.inject.Inject
import javax.inject.Singleton

/*
 * Notes for the future
 * 1. Due to current practices in the data for employees first_name_mi can be blank rather than null.  This should be
 *    fixed so that it is more consistent in the database and is not smoothed over by the business logic.
 */
@Singleton
class EmployeeRepository @Inject constructor(
   private val jdbc: NamedParameterJdbcTemplate,
   private val storeRepository: StoreRepository,
   private val postgresClient: PgPool
) {
   private val logger: Logger = LoggerFactory.getLogger(EmployeeRepository::class.java)

   private val selectBaseWithoutEmployeeStoreJoin = """
      WITH employees AS (
         SELECT id, time_created, time_updated, number, last_name, first_name_mi, pass_code, store_number, active, department, loc
         FROM (
            SELECT 1 AS from_priority,
               fpie.id AS id,
               fpie.time_created AS time_created,
               fpie.time_updated AS time_updated,
               fpie.number AS number,
               fpie.last_name AS last_name,
               fpie.first_name_mi AS first_name_mi,
               fpie.pass_code AS pass_code,
               fpie.store_number AS store_number,
               fpie.active AS active,
               fpie.department AS department,
               'ext' AS loc
            FROM fastinfo_prod_import.employee_vw fpie
            WHERE coalesce(trim(fpie.pass_code), '') <> ''
            UNION
            SELECT 2 AS from_priority,
               e.id AS id,
               e.time_created AS time_created,
               e.time_updated AS time_updated,
               e.number AS number,
               e.last_name AS last_name,
               e.first_name_mi AS first_name_mi,
               e.pass_code AS pass_code,
               e.store_number AS store_number,
               e.active AS active,
               e.department AS department,
               'int' AS loc
            FROM employee e
            WHERE coalesce(trim(e.pass_code), '') <> ''
         ) AS inner_emp
         ORDER BY from_priority
      ), stores AS (
         SELECT
            s.id AS s_id,
            s.time_created AS s_time_created,
            s.time_updated AS s_time_updated,
            s.number AS s_number,
            s.name AS s_name,
            s.dataset AS s_dataset
         FROM fastinfo_prod_import.store_vw s
      )
      SELECT
         id AS e_id,
         time_created AS e_time_created,
         time_updated AS e_time_updated,
         number AS e_number,
         last_name AS e_last_name,
         NULLIF(TRIM(first_name_mi), '') AS e_first_name_mi,
         pass_code AS e_pass_code,
         active AS e_active,
         department AS e_department,
         loc AS e_loc,
         s.s_id AS s_id,
         s.s_time_created AS s_time_created,
         s.s_time_updated AS s_time_updated,
         s.s_number AS s_number,
         s.s_name AS s_name,
         s.s_dataset AS s_dataset
      FROM employees e
           JOIN stores s
   """.trimIndent()

   val selectBase = "$selectBaseWithoutEmployeeStoreJoin ON e.store_number = s.s_number"

   fun findOne(id: Long, loc: String): Employee? {
      val found = jdbc.findFirstOrNull("$selectBase\nWHERE e.id = :id AND e.loc = :loc", mapOf("id" to id, "loc" to loc), RowMapper { rs, _ -> mapRow(rs) })

      logger.trace("Searching for Employee: {} {} resulted in {}", id, loc, found)

      return found
   }

   fun findOne(number: Int, loc: String): Employee? {
      val found = jdbc.findFirstOrNull("$selectBase\nWHERE e.number = :number AND loc = :loc", mapOf("number" to number, "loc" to loc), RowMapper { rs, _ -> mapRow(rs) })

      logger.trace("Searching for Employee: {} {} resulted in {}", number, loc, found)

      return found
   }

   fun findOne(id: Long, loc: String, storeNumber: Int): Employee? {
      val found = jdbc.findFirstOrNull("$selectBaseWithoutEmployeeStoreJoin\nON s.s_number = :store_number\nWHERE e.id = :id AND e.loc = :loc", mapOf("id" to id, "loc" to loc, "store_number" to storeNumber), RowMapper { rs, _ -> mapRow(rs)})

      logger.trace("Searching for Employee: {} {} {} resulted in {}", id, loc, storeNumber, found)

      return found
   }

   fun exists(id: Long, loc: String): Boolean {
      val exists = jdbc.queryForObject("SELECT EXISTS(SELECT id FROM ($selectBase) AS emp_exists WHERE emp_exists.id = :id)", mapOf("id" to id), Boolean::class.java)!!

      logger.trace("Checking if Employee: {} exists resulted in {}", id, exists)

      return exists
   }

   /**
    * This method returns a PG Reactive Single Employee that is really meant to be used only for authentication as it
    * unions together the cynergidb.employee table as well as the view referenced by the Foreign Data Wrapper that is
    * pointed at FastInfo to pull in Zortec data about an Employee
    */
   fun findUserByAuthentication(number: Int, passCode: String, storeNumber: Int?): Maybe<Employee> {
      logger.trace("Checking authentication for {} {}", number, storeNumber)

      val tuple: Tuple
      val query = if (storeNumber != null) {
         tuple = Tuple.of(storeNumber, number, passCode)

         """
         $selectBaseWithoutEmployeeStoreJoin
            ON s.s_number = $1
         WHERE e.number = $2
            AND e.pass_code = $3
            AND e.active = true
         LIMIT 1
         """.trimIndent()
      } else {
         tuple = Tuple.of(number, passCode)

         """
         $selectBase
         WHERE e.number = $1
            AND e.pass_code = $2
            AND e.active = true
         LIMIT 1
         """.trimIndent()
      }

      logger.trace(query)

      return postgresClient.rxPreparedQuery(query, tuple)
         .filter { rs -> rs.size() > 0 }
         .map { rs ->
            val iterator = rs.iterator()
            val row = iterator.next()

            Employee(
               id = row.getLong("e_id"),
               timeCreated = row.getOffsetDateTime("e_time_created"),
               timeUpdated = row.getOffsetDateTime("e_time_updated") ?: OffsetDateTime.now(),
               loc = row.getString("e_loc"),
               number = row.getInteger("e_number"),
               lastName = row.getString("e_last_name"),
               firstNameMi = row.getString("e_first_name_mi"),
               passCode = row.getString("e_pass_code"),
               store = storeRepository.mapRow(row, "s_"),
               active = row.getBoolean("e_active")
            )
         }
   }

   @Transactional
   fun insert(entity: Employee): Employee {
      logger.debug("Inserting employee {}", entity)

      return jdbc.insertReturning("""
         INSERT INTO employee(number, last_name, first_name_mi, pass_code, store_number, active)
         VALUES (:number, :last_name, :first_name_mi, :pass_code, :store_number, :active)
         RETURNING
            *
         """.trimIndent(),
         mapOf(
            "number" to entity.number,
            "last_name" to entity.lastName,
            "first_name_mi" to entity.firstNameMi.trimToNull(), // not sure this is a good practice as it isn't being enforced by the database, but should be once the employee data is managed in PostgreSQL
            "pass_code" to entity.passCode,
            "store_number" to entity.store.number,
            "active" to entity.active
         ),
         RowMapper { rs, _ ->
            mapDDLRow(rs, entity.store)
         }
      )
   }

   @Transactional
   fun update(entity: Employee): Employee {
      logger.debug("Updating employee {}", entity)

      return jdbc.updateReturning("""
         UPDATE employee
         SET
            number = :number,
            last_name = :last_name,
            first_name_mi = :first_name_mi,
            pass_code = :pass_code,
            store_number = :store_number,
            active = :active
         WHERE id = :id
         RETURNING
            *
         """.trimIndent(),
         mapOf(
            "id" to entity.id,
            "number" to entity.number,
            "last_name" to entity.lastName,
            "first_name_mi" to entity.firstNameMi.trimToNull(),  // not sure this is a good practice as it isn't being enforced by the database, but should be once the employee data is managed in PostgreSQL
            "pass_code" to entity.passCode,
            "store_number" to entity.store.number,
            "active" to entity.active
         ),
         RowMapper { rs, _ ->
            mapDDLRow(rs, entity.store)
         }
      )
   }

   private fun mapDDLRow(rs: ResultSet, store: Store) : Employee =
      Employee(
         id = rs.getLong("id"),
         timeCreated = rs.getOffsetDateTime("time_created"),
         timeUpdated = rs.getOffsetDateTime("time_updated"),
         loc = "int",
         number = rs.getInt("number"),
         lastName = rs.getString("last_name"),
         firstNameMi = rs.getString("first_name_mi"), // FIXME fix query so that it isn't trimming stuff to null when employee is managed by PostgreSQL
         passCode = rs.getString("pass_code"),
         store = store,
         active = rs.getBoolean("active"),
         department = rs.getString("department")
      )


   @Cacheable("user-cache")
   fun canEmployeeAccess(loc: String, asset: String, id: Long): Boolean {
      logger.debug("Check if user {} has access to asset {} via the database", id, asset)

      return if(asset == "check") { // everyone authenticated should be able to access this asset
         true
      } else {
         true // TODO do this check once the appropriate data from the menu/modules conversion is in place
      }
   }

   fun mapRow(rs: ResultSet, columnPrefix: String = "e_", storeColumnPrefix: String = "s_"): Employee  =
      Employee(
         id = rs.getLong("${columnPrefix}id"),
         timeCreated = rs.getOffsetDateTime("${columnPrefix}time_created"),
         timeUpdated = rs.getOffsetDateTime("${columnPrefix}time_updated"),
         loc = rs.getString("${columnPrefix}loc"),
         number = rs.getInt("${columnPrefix}number"),
         lastName = rs.getString("${columnPrefix}last_name"),
         firstNameMi = rs.getString("${columnPrefix}first_name_mi"),  // FIXME fix query so that it isn't trimming stuff to null when employee is managed by PostgreSQL
         passCode = rs.getString("${columnPrefix}pass_code"),
         store = storeRepository.mapRow(rs, storeColumnPrefix),
         active = rs.getBoolean("${columnPrefix}active"),
         department = rs.getString("${columnPrefix}department")
      )

   fun maybeMapRow(rs: ResultSet, columnPrefix: String = "e_", storeColumnPrefix: String = "s_"): Employee?  =
      if (rs.getString("${columnPrefix}id") != null) {
         mapRow(rs, columnPrefix)
      } else {
         null
      }
}
