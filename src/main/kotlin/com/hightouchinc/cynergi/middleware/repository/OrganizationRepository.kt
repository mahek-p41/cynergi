package com.hightouchinc.cynergi.middleware.repository

import com.hightouchinc.cynergi.middleware.entity.Organization
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
class OrganizationRepository @Inject constructor(
   private val jdbc: NamedParameterJdbcTemplate
) : Repository<Organization> {
   private val logger: Logger = LoggerFactory.getLogger(OrganizationRepository::class.java)
   private val simpleOrganizationRowMapper = OrganizationRowMapper()

   override fun findOne(id: Long): Organization? {
      val found = jdbc.findFirstOrNull("SELECT * FROM organization WHERE id = :id", mapOf("id" to id), simpleOrganizationRowMapper)

      logger.trace("Searching for Organization: {} resulted in {}", id, found)

      return found
   }

   override fun exists(id: Long): Boolean {
      val exists = jdbc.queryForObject("SELECT EXISTS(SELECT id FROM organization WHERE id = :id)", mapOf("id" to id), Boolean::class.java)!!

      logger.trace("Checking if Organization: {} exists resulted in {}", id, exists)

      return exists
   }

   override fun insert(entity: Organization): Organization {
      logger.debug("Inserting organization {}", entity)

      return jdbc.insertReturning("""
         INSERT INTO organization(name, billing_account)
         VALUES (:name, :billing_account)
         RETURNING
            *
         """.trimIndent(),
         mapOf(
            "name" to entity.name,
            "billing_account" to entity.billingAccount
         ),
         simpleOrganizationRowMapper
      )
   }

   override fun update(entity: Organization): Organization {
      logger.debug("Updating organization {}", entity)

      return jdbc.updateReturning("""
         UPDATE organization
         SET
            name = :name,
            billing_account = :billiig_account
         WHERE id = :id
         RETURNING
            *
         """.trimIndent(),
         mapOf(
            "id" to entity.id!!,
            "name" to entity.name,
            "billing_account" to entity.billingAccount
         ),
         simpleOrganizationRowMapper
      )
   }
}

private class OrganizationRowMapper(
   private val rowPrefix: String = EMPTY
) : RowMapper<Organization> {
   override fun mapRow(rs: ResultSet, rowNum: Int): Organization =
      Organization(
         id = rs.getLong("${rowPrefix}id"),
         uuRowId = rs.getUuid("${rowPrefix}uu_row_id"),
         timeCreated = rs.getOffsetDateTime("${rowPrefix}time_created"),
         timeUpdated = rs.getOffsetDateTime("${rowPrefix}time_updated"),
         name = rs.getString("${rowPrefix}name"),
         billingAccount = rs.getString("${rowPrefix}billing_account")
      )
}
