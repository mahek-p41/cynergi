package com.cynergisuite.middleware.employee.infrastructure

import com.cynergisuite.domain.infrastructure.Repository
import com.cynergisuite.extensions.findFirstOrNull
import com.cynergisuite.extensions.getOffsetDateTime
import com.cynergisuite.extensions.getUuid
import com.cynergisuite.extensions.insertReturning
import com.cynergisuite.extensions.updateReturning
import com.cynergisuite.middleware.employee.Employee
import io.micronaut.spring.tx.annotation.Transactional
import io.reactiverse.reactivex.pgclient.PgPool
import io.reactiverse.reactivex.pgclient.Tuple
import io.reactivex.Maybe
import org.apache.commons.lang3.StringUtils.EMPTY
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.sql.ResultSet
import java.time.OffsetDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EmployeeRepository @Inject constructor(
   private val jdbc: NamedParameterJdbcTemplate,
   private val postgresClient: PgPool
) : Repository<Employee> {
   private val logger: Logger = LoggerFactory.getLogger(EmployeeRepository::class.java)
   private val simpleEmployeeRowMapper = EmployeeRowMapper()

   override fun findOne(id: Long): Employee? {
      val found = jdbc.findFirstOrNull("SELECT * FROM employee WHERE id = :id", mapOf("id" to id), simpleEmployeeRowMapper)

      logger.trace("Searching for Employee: {} resulted in {}", id, found)

      return found
   }

   override fun exists(id: Long): Boolean {
      val exists = jdbc.queryForObject("SELECT EXISTS(SELECT id FROM employee WHERE id = :id)", mapOf("id" to id), Boolean::class.java)!!

      logger.trace("Checking if Employee: {} exists resulted in {}", id, exists)

      return exists
   }

   /**
    * This method returns a PG Reactive Single Employee that is really meant to be used only for authentication as it
    * unions together the cynergidb.employee table as well as the view referenced by the Foreign Data Wrapper that is
    * pointed at FastInfo to pull in Zortec data about an Employee
    */
   fun findUserByAuthentication(number: Int, passCode: String): Maybe<Employee> {
      logger.trace("Checking authentication for {}", number)

      return postgresClient.rxPreparedQuery("""
         SELECT
            fpie.id AS id,
            fpie.uu_row_id AS uu_row_id,
            fpie.time_created AS time_created,
            fpie.time_updated AS time_update,
            fpie.number AS number,
            fpie.pass_code AS pass_code,
            fpie.active AS active
         FROM fastinfo_prod_import.employee_vw fpie
         WHERE fpie.number = $1
            AND fpie.pass_code = $2
            AND fpie.active = true
            AND char_length(fpie.pass_code) > 0
         UNION
         SELECT
            e.id AS id,
            e.uu_row_id AS uu_row_id,
            e.time_created AS time_created,
            e.time_updated AS time_update,
            e.number AS number,
            e.pass_code AS pass_code,
            e.active AS active
         FROM employee e
         WHERE e.number = $1
            AND e.pass_code = $2
            AND e.active = true
         LIMIT 1
         """.trimIndent(), Tuple.of(number, passCode))
         .filter { rs -> rs.size() > 0 }
         .map { rs ->
            val iterator = rs.iterator()
            val row = iterator.next()

            Employee(
               id = row.getLong("id"),
               uuRowId = row.getUUID("uu_row_id"),
               timeCreated = row.getOffsetDateTime("time_created"),
               timeUpdated = row.getOffsetDateTime("time_updated") ?: OffsetDateTime.now(),
               number = row.getInteger("number"),
               passCode = row.getString("pass_code"),
               active = row.getBoolean("active")
            )
         }
   }

   @Transactional
   override fun insert(entity: Employee): Employee {
      logger.debug("Inserting employee {}", entity)

      return jdbc.insertReturning("""
         INSERT INTO employee(number, pass_code, active)
         VALUES (:number, :pass_code, :active)
         RETURNING
            *
         """.trimIndent(),
         mapOf(
            "number" to entity.number,
            "pass_code" to entity.passCode,
            "active" to entity.active
         ),
         simpleEmployeeRowMapper
      )
   }

   @Transactional
   override fun update(entity: Employee): Employee {
      logger.debug("Updating employee {}", entity)

      return jdbc.updateReturning("""
         UPDATE employee
         SET
            number = :number,
            pass_code = :pass_code,
            active = :active
         WHERE id = :id
         RETURNING
            *
         """.trimIndent(),
         mapOf(
            "id" to entity.id,
            "number" to entity.number,
            "pass_code" to entity.passCode,
            "active" to entity.active
         ),
         simpleEmployeeRowMapper
      )
   }

   fun canEmployeeAccess(assert: String, id: Long): Boolean {
      return true // TODO do this check once the appropriate data from the menu/modules conversion is in place
   }
}

private class EmployeeRowMapper(
   private val columnPrefix: String = EMPTY
) : RowMapper<Employee> {
   override fun mapRow(rs: ResultSet, rowNum: Int): Employee =
      Employee(
         id = rs.getLong("${columnPrefix}id"),
         uuRowId = rs.getUuid("${columnPrefix}uu_row_id"),
         timeCreated = rs.getOffsetDateTime("${columnPrefix}time_created"),
         timeUpdated = rs.getOffsetDateTime("${columnPrefix}time_updated"),
         number = rs.getInt("${columnPrefix}number"),
         passCode = rs.getString("${columnPrefix}pass_code"),
         active = rs.getBoolean("${columnPrefix}active")
      )
}
