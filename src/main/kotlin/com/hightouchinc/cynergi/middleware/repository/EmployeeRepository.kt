package com.hightouchinc.cynergi.middleware.repository

import com.hightouchinc.cynergi.middleware.entity.Employee
import com.hightouchinc.cynergi.middleware.entity.helper.SimpleIdentifiableEntity
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
         INSERT INTO employee(user_id, password, first_name, last_name, department_id, company_id)
         VALUES (:user_id, :password, :first_name, :last_name, :department_id, :company_id)
         RETURNING
            *
         """.trimIndent(),
         mapOf(
            "user_id" to entity.userId,
            "password" to entity.password,
            "first_name" to entity.firstName,
            "last_name" to entity.lastName,
            "department_id" to entity.department.entityId(),
            "company_id" to entity.company.entityId()
         ),
         simpleEmployeeRowMapper
      )
   }

   override fun update(entity: Employee): Employee {
      logger.debug("Updating employee {}", entity)

      return jdbc.updateReturning("""
         UPDATE employee
         SET
            user_id = :user_id,
            password = :password,
            first_name = :first_name,
            last_name = :last_name,,
            department_id = :department_id,
            company_id = :company_id
         WHERE id = :id
         RETURNING
            *
         """.trimIndent(),
         mapOf(
            "id" to entity.id!!,
            "user_id" to entity.userId,
            "password" to entity.password,
            "first_name" to entity.firstName,
            "last_name" to entity.lastName,
            "department_id" to entity.department.entityId(),
            "company_id" to entity.company.entityId()
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
         timeUpdated = rs.getOffsetDateTime("${columnPrefix}time_updated"),
         userId = rs.getString("${columnPrefix}user_id"),
         password = rs.getString("${columnPrefix}password"),
         firstName = rs.getString("${columnPrefix}first_name"),
         lastName = rs.getString("${columnPrefix}last_name"),
         department = SimpleIdentifiableEntity(rs.getLong("${columnPrefix}department")),
         company = SimpleIdentifiableEntity(rs.getLong("${columnPrefix}company"))
      )
}
