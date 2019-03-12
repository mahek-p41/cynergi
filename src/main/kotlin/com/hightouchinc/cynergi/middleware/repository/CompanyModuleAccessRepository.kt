package com.hightouchinc.cynergi.middleware.repository

import com.hightouchinc.cynergi.middleware.entity.CompanyModuleAccess
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
class CompanyModuleAccessRepository @Inject constructor(
   private val jdbc: NamedParameterJdbcTemplate
) : Repository<CompanyModuleAccess> {
   private val logger: Logger = LoggerFactory.getLogger(CompanyModuleAccessRepository::class.java)
   private val simpleCompanyModuleAccessRowMapper = CompanyModuleAccessRowMapper()

   override fun findOne(id: Long): CompanyModuleAccess? {
      val found = jdbc.findFirstOrNull("SELECT * FROM company_module_access WHERE id = :id", mapOf("id" to id), simpleCompanyModuleAccessRowMapper)

      logger.trace("Searching for CompanyModuleAccess: {} resulted in {}", id, found)

      return found
   }

   override fun exists(id: Long): Boolean {
      val exists = jdbc.queryForObject("SELECT EXISTS(SELECT id FROM company_module_access WHERE id = :id)", mapOf("id" to id), Boolean::class.java)!!

      logger.trace("Checking if CompanyModuleAccess: {} exists resulted in {}", id, exists)

      return exists
   }

   override fun insert(entity: CompanyModuleAccess): CompanyModuleAccess {
      logger.debug("Inserting company_module_access {}", entity)

      return jdbc.insertReturning("""
         INSERT INTO company_module_access()
         VALUES ()
         RETURNING
            *
         """.trimIndent(),
         mapOf<String, Any>(),
         simpleCompanyModuleAccessRowMapper
      )
   }

   override fun update(entity: CompanyModuleAccess): CompanyModuleAccess {
      logger.debug("Updating company_module_access {}", entity)

      return jdbc.updateReturning("""
         UPDATE company_module_access
         SET

         WHERE id = :id
         RETURNING
            *
         """.trimIndent(),
         mapOf(
            "id" to entity.id!!
         ),
         simpleCompanyModuleAccessRowMapper
      )
   }
}

private class CompanyModuleAccessRowMapper(
   private val columnPrefix: String = EMPTY
) : RowMapper<CompanyModuleAccess> {
   override fun mapRow(rs: ResultSet, rowNum: Int): CompanyModuleAccess =
      CompanyModuleAccess(
         id = rs.getLong("${columnPrefix}id"),
         uuRowId = rs.getUuid("${columnPrefix}uu_row_id"),
         timeCreated = rs.getOffsetDateTime("${columnPrefix}time_created"),
         timeUpdated = rs.getOffsetDateTime("${columnPrefix}time_updated"),
         level = rs.getInt("${columnPrefix}level"),
         company = SimpleIdentifiableEntity(rs.getLong("${columnPrefix}company_id")),
         module = SimpleIdentifiableEntity(rs.getLong("${columnPrefix}module_id"))
      )
}
