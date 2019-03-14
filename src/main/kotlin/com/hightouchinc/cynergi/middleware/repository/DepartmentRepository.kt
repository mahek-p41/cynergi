package com.hightouchinc.cynergi.middleware.repository

import com.hightouchinc.cynergi.middleware.entity.Department
import com.hightouchinc.cynergi.middleware.entity.helper.SimpleIdentifiableEntity
import com.hightouchinc.cynergi.middleware.extensions.findFirstOrNull
import com.hightouchinc.cynergi.middleware.extensions.getOffsetDateTime
import com.hightouchinc.cynergi.middleware.extensions.getUuid
import com.hightouchinc.cynergi.middleware.extensions.insertReturning
import com.hightouchinc.cynergi.middleware.extensions.updateReturning
import io.micronaut.spring.tx.annotation.Transactional
import org.apache.commons.lang3.StringUtils.EMPTY
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.sql.ResultSet
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DepartmentRepository @Inject constructor(
   private val jdbc: NamedParameterJdbcTemplate
) : Repository<Department> {
   private val logger: Logger = LoggerFactory.getLogger(DepartmentRepository::class.java)
   private val simpleDepartmentRowMapper = DepartmentRowMapper()

   override fun findOne(id: Long): Department? {
      val found = jdbc.findFirstOrNull("SELECT * FROM department WHERE id = :id", mapOf("id" to id), simpleDepartmentRowMapper)

      logger.trace("Searching for Department: {} resulted in {}", id, found)

      return found
   }

   override fun exists(id: Long): Boolean {
      val exists = jdbc.queryForObject("SELECT EXISTS(SELECT id FROM department WHERE id = :id)", mapOf("id" to id), Boolean::class.java)!!

      logger.trace("Checking if Department: {} exists resulted in {}", id, exists)

      return exists
   }

   @Transactional
   override fun insert(entity: Department): Department {
      logger.debug("Inserting department {}", entity)

      return jdbc.insertReturning("""
         INSERT INTO department(name, level, company_id)
         VALUES (:name, :level, :company_id)
         RETURNING
            *
         """.trimIndent(),
         mapOf(
            "name" to entity.name,
            "level" to entity.level,
            "company_id" to entity.company.entityId()!!
         ),
         simpleDepartmentRowMapper
      )
   }

   @Transactional
   override fun update(entity: Department): Department {
      logger.debug("Updating department {}", entity)

      return jdbc.updateReturning("""
         UPDATE department
         SET
            name = :name,
            level = :level,
            company_id = :company_id
         WHERE id = :id
         RETURNING
            *
         """.trimIndent(),
         mapOf(
            "id" to entity.id!!,
            "name" to entity.name,
            "level" to entity.level,
            "company_id" to entity.company.entityId()!!
         ),
         simpleDepartmentRowMapper
      )
   }
}

private class DepartmentRowMapper(
   private val columnPrefix: String = EMPTY
) : RowMapper<Department> {
   override fun mapRow(rs: ResultSet, rowNum: Int): Department =
      Department(
         id = rs.getLong("${columnPrefix}id"),
         uuRowId = rs.getUuid("${columnPrefix}uu_row_id"),
         timeCreated = rs.getOffsetDateTime("${columnPrefix}time_created"),
         timeUpdated = rs.getOffsetDateTime("${columnPrefix}time_updated"),
         name = rs.getString("${columnPrefix}name"),
         level = rs.getInt("${columnPrefix}level"),
         company = SimpleIdentifiableEntity(rs.getLong("${columnPrefix}company_id"))
      )
}
