package com.cynergisuite.middleware.region.infrastructure

import com.cynergisuite.extensions.getUuid
import com.cynergisuite.extensions.insertReturning
import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.division.infrastructure.DivisionRepository
import com.cynergisuite.middleware.region.RegionEntity
import org.apache.commons.lang3.StringUtils
import org.jdbi.v3.core.Jdbi
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.sql.ResultSet
import java.sql.SQLException
import javax.inject.Inject
import javax.inject.Singleton
import javax.transaction.Transactional

@Singleton
class RegionRepository @Inject constructor(
   private val jdbc: Jdbi,
   private val divisionRepository: DivisionRepository
) {
   private val logger: Logger = LoggerFactory.getLogger(RegionRepository::class.java)

   @Transactional
   fun insert(region: RegionEntity): RegionEntity {
      logger.debug("Inserting region {}", region)
      return jdbc.insertReturning(
         """
            INSERT INTO region(division_id, number, name, description)
            VALUES (:division_id, :number, :name, :description)
            RETURNING *
         """.trimIndent(),
         mapOf(
            "division_id" to region.division.id,
            "number" to region.number,
            "name" to region.name,
            "description" to region.description
         )
      ) { rs, _ -> mapRow(rs, region) }
   }

   fun mapRow(rs: ResultSet, company: CompanyEntity, columnPrefix: String = StringUtils.EMPTY): RegionEntity =
      RegionEntity(
         id = rs.getUuid("${columnPrefix}id"),
         division = divisionRepository.mapRow(rs, company, "div_"),
         number = rs.getLong("${columnPrefix}number"),
         name = rs.getString("${columnPrefix}name"),
         description = rs.getString("${columnPrefix}description")
      )

   fun mapRowOrNull(rs: ResultSet, company: CompanyEntity, columnPrefix: String = "reg_", companyPrefix: String = "comp_", departmentPrefix: String = "dept_", storePrefix: String = "store_"): RegionEntity? =
      try {
         if (rs.getString("${columnPrefix}id") != null) {
            mapRow(rs, company, "reg_")
         } else {
            null
         }
      } catch (e: SQLException) {
         null
      }

   fun mapRow(rs: ResultSet, region: RegionEntity, columnPrefix: String = StringUtils.EMPTY): RegionEntity =
      RegionEntity(
         id = rs.getUuid("${columnPrefix}id"),
         number = rs.getLong("${columnPrefix}number"),
         division = region.division,
         name = rs.getString("${columnPrefix}name"),
         description = rs.getString("${columnPrefix}description")
      )
}
