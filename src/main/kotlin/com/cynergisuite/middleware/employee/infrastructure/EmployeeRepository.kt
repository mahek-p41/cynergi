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
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
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

   private val selectBaseWithoutEmployeeStoreJoin = """
      WITH employees AS (
         SELECT id, time_created, time_updated, number, last_name, first_name_mi, pass_code, store_number, active, department, loc, allow_auto_store_assign
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
               FALSE AS allow_auto_store_assign,
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
               e.allow_auto_store_assign AS allow_auto_store_assign,
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
         allow_auto_store_assign AS e_allow_auto_store_assign,
         s.s_id AS s_id,
         s.s_time_created AS s_time_created,
         s.s_time_updated AS s_time_updated,
         s.s_number AS s_number,
         s.s_name AS s_name,
         s.s_dataset AS s_dataset,
         ds.s_id AS ds_id,
         ds.s_time_created AS ds_time_created,
         ds.s_time_updated AS ds_time_updated,
         ds.s_number AS ds_number,
         ds.s_name AS ds_name,
         ds.s_dataset AS ds_dataset
      FROM employees e
           JOIN stores ds ON ds.s_number = (SELECT coalesce(max(store_number), 9000) FROM fastinfo_prod_import.employee_vw)
           LEFT OUTER JOIN stores s
   """.trimIndent()

   val selectBase = "$selectBaseWithoutEmployeeStoreJoin ON e.store_number = s.s_number"

   fun findOne(id: Long, loc: String): EmployeeEntity? {
      val found = jdbc.findFirstOrNull("$selectBase\nWHERE e.id = :id AND e.loc = :loc", mapOf("id" to id, "loc" to loc), RowMapper { rs, _ -> mapRow(rs) })

      logger.trace("Searching for Employee: {} {} resulted in {}", id, loc, found)

      return found
   }

   fun findOne(number: Int, loc: String): EmployeeEntity? {
      val found = jdbc.findFirstOrNull("$selectBase\nWHERE e.number = :number AND loc = :loc", mapOf("number" to number, "loc" to loc), RowMapper { rs, _ -> mapRow(rs) })

      logger.trace("Searching for Employee: {} {} resulted in {}", number, loc, found)

      return found
   }

   fun findOne(user: AuthenticatedUser): EmployeeEntity? {
      val found = jdbc.findFirstOrNull("""
         $selectBaseWithoutEmployeeStoreJoin
            ON s.s_number = :store_number
         WHERE e.id = :id
               AND e.loc = :loc
         """.trimIndent(),
         mapOf(
            "id" to user.id,
            "loc" to user.loc,
            "store_number" to user.storeNumber
         ),
         RowMapper { rs, _ -> mapRow(rs) }
      )

      logger.trace("Searching for Employee: {} resulted in {}", user, found)

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
   fun findUserByAuthentication(number: Int, passCode: String, storeNumber: Int?): Maybe<EmployeeEntity> {
      logger.trace("Checking authentication for {} {}", number, storeNumber)

      val tuple: Tuple
      val query = if (storeNumber != null) {
         tuple = Tuple.of(storeNumber, number)

         """
         $selectBaseWithoutEmployeeStoreJoin
            ON s.s_number = $1
         WHERE e.number = $2
            AND e.active = true
         """.trimIndent()
      } else {
         tuple = Tuple.of(number)

         """
         $selectBase
         WHERE e.number = $1
            AND e.active = true
         """.trimIndent()
      }

      logger.trace(query)

      return postgresClient.rxPreparedQuery(query, tuple)
         .filter { rs -> rs.size() > 0 }
         .map { rs ->
            val iterator = rs.iterator()
            val row = iterator.next()

            val defaultStore = storeRepository.mapRow(row, "ds_")
            val employee = EmployeeEntity(
               id = row.getLong("e_id"),
               timeCreated = row.getOffsetDateTime("e_time_created"),
               timeUpdated = row.getOffsetDateTime("e_time_updated") ?: OffsetDateTime.now(),
               loc = row.getString("e_loc"),
               number = row.getInteger("e_number"),
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
            if (employee.loc == "int") {
               passwordEncoderService.matches(passCode, employee.passCode)
            } else {
               employee.passCode == passCode // FIXME remove this when all users are loaded out of cynergidb and are encoded by BCrypt
            }
         }
         .filter { (employee, _) -> employee.store != null || employee.allowAutoStoreAssign } // FIXME reconsider this filter
         .map { (employee, defaultStore) ->
            if (employee.store == null && employee.allowAutoStoreAssign) {
               employee.copy(store = defaultStore)
            } else {
               employee
            }
         }
   }

   @Transactional
   fun insert(entity: EmployeeEntity): EmployeeEntity {
      logger.debug("Inserting employee {}", entity)

      return jdbc.insertReturning("""
         INSERT INTO employee(number, last_name, first_name_mi, pass_code, store_number, active, allow_auto_store_assign)
         VALUES (:number, :last_name, :first_name_mi, :pass_code, :store_number, :active, :allow_auto_store_assign)
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
            "allow_auto_store_assign" to entity.allowAutoStoreAssign
         ),
         RowMapper { rs, _ ->
            mapDDLRow(rs, entity.store)
         }
      )
   }

   @Cacheable("user-cache")
   fun canEmployeeAccess(loc: String, asset: String, id: Long): Boolean {
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
         timeCreated = rs.getOffsetDateTime("${columnPrefix}time_created"),
         timeUpdated = rs.getOffsetDateTime("${columnPrefix}time_updated"),
         loc = rs.getString("${columnPrefix}loc"),
         number = rs.getInt("${columnPrefix}number"),
         lastName = rs.getString("${columnPrefix}last_name"),
         firstNameMi = rs.getString("${columnPrefix}first_name_mi"),  // FIXME fix query so that it isn't trimming stuff to null when employee is managed by PostgreSQL
         passCode = rs.getString("${columnPrefix}pass_code"),
         store = storeRepository.mapRow(rs, storeColumnPrefix),
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
}
