package com.cynergisuite.middleware.region.infrastructure

import com.cynergisuite.extensions.insertReturning
import com.cynergisuite.middleware.company.Company
import com.cynergisuite.middleware.division.infrastructure.DivisionRepository
import com.cynergisuite.middleware.region.RegionEntity
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
class RegionRepository @Inject constructor(
   private val jdbc: NamedParameterJdbcTemplate,
   private val divisionRepository: DivisionRepository
) {
   private val logger: Logger = LoggerFactory.getLogger(RegionRepository::class.java)

   @Transactional
   fun insert(region: RegionEntity): RegionEntity {
      logger.debug("Inserting region {}", region)
      return jdbc.insertReturning("""
            INSERT INTO region(division_id, number, name, description)
            VALUES (:division_id, :number, :name, :description)
            RETURNING *
         """.trimIndent(),
         mapOf(
            "division_id" to region.division.id,
            "number" to region.number,
            "name" to region.name,
            "description" to region.description
         ),
         RowMapper { rs, _ -> mapRowSimple(rs, region) }
      )
   }

   fun mapRow(rs: ResultSet, company: Company, columnPrefix: String = StringUtils.EMPTY): RegionEntity =
      RegionEntity(
         id = rs.getLong("${columnPrefix}id"),
         division = divisionRepository.mapRow(rs, company, "div_"),
         number = rs.getInt("${columnPrefix}number"),
         name = rs.getString("${columnPrefix}name"),
         description = rs.getString("${columnPrefix}description")
      )

   fun mapRowSimple(rs: ResultSet, region: RegionEntity, columnPrefix: String = StringUtils.EMPTY): RegionEntity =
      RegionEntity(
         id = rs.getLong("${columnPrefix}id"),
         division = region.division,
         number = rs.getInt("${columnPrefix}number"),
         name = rs.getString("${columnPrefix}name"),
         description = rs.getString("${columnPrefix}description")
      )
}
