package com.cynergisuite.middleware.division.infrastructure

import com.cynergisuite.extensions.insertReturning
import com.cynergisuite.middleware.company.Company
import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.division.DivisionEntity
import io.micronaut.spring.tx.annotation.Transactional
import org.apache.commons.lang3.StringUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.sql.ResultSet
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DivisionRepository @Inject constructor(
   private val jdbc: NamedParameterJdbcTemplate
) {
   private val logger: Logger = LoggerFactory.getLogger(DivisionRepository::class.java)

   @Transactional
   fun insert(entity: DivisionEntity): DivisionEntity {
      logger.debug("Inserting division {}", entity)
      return jdbc.insertReturning(
         """
               INSERT INTO division(company_id, number, name, description)
               VALUES (:company_id, :number, :name, :description)
               RETURNING *
         """.trimIndent(),
         mapOf(
            "company_id" to entity.company.id,
            "number" to entity.number,
            "name" to entity.name,
            "description" to entity.description
         ),
         RowMapper { rs, _ -> mapRow(rs, entity) }
      )
   }

   fun mapRow(rs: ResultSet, company: Company, columnPrefix: String = StringUtils.EMPTY): DivisionEntity =
      DivisionEntity(
         id = rs.getLong("${columnPrefix}id"),
         company = company as CompanyEntity,
         number = rs.getInt("${columnPrefix}number"),
         name = rs.getString("${columnPrefix}name"),
         description = rs.getString("${columnPrefix}description")
      )

   fun mapRow(rs: ResultSet, division: DivisionEntity, columnPrefix: String = StringUtils.EMPTY): DivisionEntity =
      DivisionEntity(
         id = rs.getLong("${columnPrefix}id"),
         company = division.company,
         number = rs.getInt("${columnPrefix}number"),
         name = rs.getString("${columnPrefix}name"),
         description = rs.getString("${columnPrefix}description")
      )
}
