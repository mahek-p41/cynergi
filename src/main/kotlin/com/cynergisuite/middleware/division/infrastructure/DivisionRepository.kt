package com.cynergisuite.middleware.division.infrastructure

import com.cynergisuite.extensions.getOffsetDateTime
import com.cynergisuite.extensions.getUuid
import com.cynergisuite.extensions.insertReturning
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
               INSERT INTO public.division(company_id, number, name, manager_number, description)
               VALUES (:company_id, :number, :name, :manager_number, :description)
               RETURNING *
            """.trimIndent(),
         mapOf(
            "company_id" to entity.company?.id,
            "number" to entity.number,
            "name" to entity.name,
            "manager_number" to entity.manager?.myNumber(),
            "description" to entity.description
         ),
         RowMapper { rs, _ -> mapRow(rs) }
      )
   }

   fun mapRowOrNull(rs: ResultSet, columnPrefix: String = StringUtils.EMPTY): DivisionEntity? {
      return if (!rs.getString("${columnPrefix}name").isNullOrEmpty()) {
         mapRow(rs, columnPrefix)
      } else {
         null
      }
   }

   fun mapRow(rs: ResultSet, columnPrefix: String = StringUtils.EMPTY): DivisionEntity =
      DivisionEntity(
         id = rs.getLong("${columnPrefix}id"),
         uuRowId = rs.getUuid("${columnPrefix}uu_row_id"),
         timeCreated = rs.getOffsetDateTime("${columnPrefix}time_created"),
         timeUpdated = rs.getOffsetDateTime("${columnPrefix}time_updated"),
         number = rs.getInt("${columnPrefix}number"),
         name = rs.getString("${columnPrefix}name"),
         manager = entity.manager,
         description = rs.getString("${columnPrefix}description")
      )
}
