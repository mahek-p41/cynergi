package com.hightouchinc.cynergi.middleware.repository

import com.hightouchinc.cynergi.middleware.entity.Employee
import com.hightouchinc.cynergi.middleware.extensions.findFirstOrNull
import com.hightouchinc.cynergi.middleware.extensions.getOffsetDateTime
import com.hightouchinc.cynergi.middleware.extensions.getUuid
import com.hightouchinc.cynergi.middleware.extensions.insertReturning
import com.hightouchinc.cynergi.middleware.extensions.updateReturning
import org.apache.commons.lang3.StringUtils.EMPTY
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.sql.ResultSet
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EmployeeRepository @Inject constructor(
   private val jdbc: NamedParameterJdbcTemplate
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

   override fun insert(entity: Employee): Employee {
      logger.debug("Inserting employee {}", entity)

      return jdbc.insertReturning("""
         INSERT INTO employee()
         VALUES ()
         RETURNING
            *
         """.trimIndent(),
         mapOf<String, Any>(),
         simpleEmployeeRowMapper
      )
   }

   override fun update(entity: Employee): Employee {
      logger.debug("Updating employee {}", entity)

      return jdbc.updateReturning("""
         UPDATE employee
         SET

         WHERE id = :id
         RETURNING
            *
         """.trimIndent(),
         mapOf(
            "id" to entity.id!!
         ),
         simpleEmployeeRowMapper
      )
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
         timeUpdated = rs.getOffsetDateTime("${columnPrefix}time_updated")
      )
}
