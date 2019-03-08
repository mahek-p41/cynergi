package com.hightouchinc.cynergi.middleware.repository

import com.hightouchinc.cynergi.middleware.entity.Company
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
class CompanyRepository @Inject constructor(
   private val jdbc: NamedParameterJdbcTemplate
) : Repository<Company> {
   private val logger: Logger = LoggerFactory.getLogger(CompanyRepository::class.java)
   private val simpleCompanyRowMapper = CompanyRowMapper()

   override fun findOne(id: Long): Company? {
      val found = jdbc.findFirstOrNull("SELECT * FROM company WHERE id = :id", mapOf("id" to id), simpleCompanyRowMapper)

      logger.trace("Searching for Company: {} resulted in {}", id, found)

      return found
   }

   override fun exists(id: Long): Boolean {
      val exists = jdbc.queryForObject("SELECT EXISTS(SELECT id FROM company WHERE id = :id)", mapOf("id" to id), Boolean::class.java)!!

      logger.trace("Checking if Company: {} exists resulted in {}", id, exists)

      return exists
   }

   override fun insert(entity: Company): Company {
      logger.debug("Inserting company {}", entity)

      return jdbc.insertReturning("""
         INSERT INTO company()
         VALUES ()
         RETURNING
            *
         """.trimIndent(),
         mapOf<String, Any>(),
         simpleCompanyRowMapper
      )
   }

   override fun update(entity: Company): Company {
      logger.debug("Updating company {}", entity)

      return jdbc.updateReturning("""
         UPDATE company
         SET

         WHERE id = :id
         RETURNING
            *
         """.trimIndent(),
         mapOf(
            "id" to entity.id!!
         ),
         simpleCompanyRowMapper
      )
   }
}

private class CompanyRowMapper(
   private val rowPrefix: String = EMPTY
) : RowMapper<Company> {
   override fun mapRow(rs: ResultSet, rowNum: Int): Company =
      Company(
         id = rs.getLong("${rowPrefix}id"),
         uuRowId = rs.getUuid("${rowPrefix}uu_row_id"),
         timeCreated = rs.getOffsetDateTime("${rowPrefix}time_created"),
         timeUpdated = rs.getOffsetDateTime("${rowPrefix}time_updated"),
         name = rs.getString("${rowPrefix}name"),
         organization = SimpleIdentifiableEntity(rs.getLong("${rowPrefix}organization_id"))
      )
}
