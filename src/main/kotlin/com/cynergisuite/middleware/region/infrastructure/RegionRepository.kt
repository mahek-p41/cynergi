package com.cynergisuite.middleware.region.infrastructure

import com.cynergisuite.domain.PageRequest
import com.cynergisuite.domain.infrastructure.DatasetRequiringRepository
import com.cynergisuite.domain.infrastructure.RepositoryPage
import com.cynergisuite.extensions.findFirstOrNull
import com.cynergisuite.extensions.getOffsetDateTime
import com.cynergisuite.extensions.getUuid
import com.cynergisuite.extensions.insertReturning
import com.cynergisuite.middleware.company.Company
import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.division.DivisionEntity
import com.cynergisuite.middleware.region.RegionEntity
import io.micronaut.spring.tx.annotation.Transactional
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.sql.ResultSet
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RegionRepository @Inject constructor(
   private val jdbc: NamedParameterJdbcTemplate
) : DatasetRequiringRepository {
   private val logger: Logger = LoggerFactory.getLogger(RegionRepository::class.java)

   fun regionBaseQuery() = """
      SELECT
         reg.id                  AS reg_id,
         reg.uu_row_id           AS reg_uu_row_id,
         reg.time_created        AS reg_time_created,
         reg.time_updated        AS reg_time_updated,
         reg.division_id         AS reg_division_id,
         reg.number              AS reg_number,
         reg.name                AS reg_name,
         reg.employee_number     AS reg_employee_number,
         reg.description         AS reg_description,
         div.id                  AS div_id,
         div.uu_row_id           AS div_uu_row_id,
         div.time_created        AS div_time_created,
         div.time_updated        AS div_time_updated,
         div.company_id          AS div_company_id,
         div.number              AS div_number,
         div.name                AS div_name,
         div.employee_number     AS div_employee_number,
         div.description         AS div_description,
         comp.id                 AS comp_id,
         comp.uu_row_id          AS comp_uu_row_id,
         comp.time_created       AS comp_time_created,
         comp.time_updated       AS comp_time_updated,
         comp.name               AS comp_name,
         comp.doing_business_as  AS comp_doing_business_as,
         comp.client_code        AS comp_client_code,
         comp.client_id          AS comp_client_id,
         comp.dataset_code       AS comp_dataset_code,
         comp.federal_id_number  AS comp_federal_id_number
      FROM region reg
         JOIN division div ON div.id = reg.division_id
         JOIN company comp ON div.company_id = comp.id
   """

   fun findOne(id: Long, company: Company): RegionEntity? {
      logger.debug("Search for region by id {}", id)

      val found = jdbc.findFirstOrNull("${regionBaseQuery()} WHERE reg.id = :id AND comp.id = :comp_id", mapOf("id" to id, "comp_id" to company.myId()),
         RowMapper { rs, _ -> mapRow(rs, "div_") })

      logger.trace("Searching for region by id {} resulted in {}", id, found)

      return found
   }

   fun findAll(pageRequest: PageRequest, company: Company): RepositoryPage<RegionEntity, PageRequest> {
      var totalElements: Long? = null
      val elements = mutableListOf<RegionEntity>()

      val whereBuilder = StringBuilder(" WHERE comp.id = :comp_id ")

      jdbc.query("""
         WITH paged AS (
            ${regionBaseQuery()}
            $whereBuilder
         )
         SELECT
            p.*,
            count(*) OVER() as total_elements
         FROM paged AS p
         ORDER BY ${pageRequest.snakeSortBy()} ${pageRequest.sortDirection()}
         LIMIT ${pageRequest.size()}
            OFFSET ${pageRequest.offset()}
         """.trimIndent(),
         mapOf(
            "comp_id" to company.myId()
         )
      ) { rs ->
         if (totalElements == null) {
            totalElements = rs.getLong("total_elements")
         }
         elements.add(mapRow(rs, "div_"))
      }

      return RepositoryPage(
         requested = pageRequest,
         elements = elements,
         totalElements = totalElements ?: 0
      )
   }

   override fun exists(id: Long, company: Company): Boolean {
      val exists = jdbc.queryForObject("SELECT EXISTS(${regionBaseQuery()} WHERE reg.id = :id AND comp.id = :comp_id)", mapOf("id" to id, "comp_id" to company.myId()), Boolean::class.java)!!

      logger.trace("Checking if Region: {} exists resulted in {}", id, exists)

      return exists
   }

   fun doesNotExist(id: Long, company: Company): Boolean = !exists(id, company)

   fun doesNotExist(region: RegionEntity, company: Company): Boolean {
      val regionId = region.myId()

      return if (regionId != null) {
         !exists(regionId, company)
      } else {
         false
      }
   }

   //TODO Refresh information on Transactional. Why do some need it and others do not?
   @Transactional
   fun insert(region: RegionEntity, division: DivisionEntity): RegionEntity {
      logger.debug("Inserting region {}", region)

      return jdbc.insertReturning("""
         INSERT INTO region(division_id, number, name, employeeNumber, description)
         VALUES (:division_id, :number, :name, :employeeNumber, :description)
         RETURNING
            *
         """,
         mapOf(
            "division_id" to division.id,
            "number" to region.number,
            "name" to region.name,
            "employeeNumber" to region.employeeNumber,
            "description" to region.description
         ),
         RowMapper { rs, _ -> mapRow(rs, "div_") }
      )
   }

   private fun mapCompany(rs: ResultSet, columnPrefix: String = "comp_"): Company {
      return CompanyEntity(
         id = rs.getLong("${columnPrefix}id"),
         uuRowId = rs.getUuid("${columnPrefix}uu_row_id"),
         timeCreated = rs.getOffsetDateTime("${columnPrefix}time_created"),
         timeUpdated = rs.getOffsetDateTime("${columnPrefix}time_updated"),
         name = rs.getString("${columnPrefix}name"),
         doingBusinessAs = rs.getString("${columnPrefix}doing_business_as"),
         clientCode = rs.getString("${columnPrefix}client_code"),
         clientId = rs.getInt("${columnPrefix}client_id"),
         federalIdNumber = rs.getString("${columnPrefix}federal_id_number"),
         datasetCode = rs.getString("${columnPrefix}dataset_code")
      )
   }

   private fun mapDivision(rs: ResultSet, columnPrefix: String = "div_"): DivisionEntity {
      return DivisionEntity(
         id = rs.getLong("${columnPrefix}id"),
         uuRowId = rs.getUuid("${columnPrefix}uu_row_id"),
         timeCreated = rs.getOffsetDateTime("${columnPrefix}time_created"),
         timeUpdated = rs.getOffsetDateTime("${columnPrefix}time_updated"),
         company = mapCompany(rs, "comp_"),
         number = rs.getInt("${columnPrefix}number"),
         name = rs.getString("${columnPrefix}name"),
         employeeNumber = rs.getInt("${columnPrefix}employee_number"),
         description = rs.getString("${columnPrefix}description")
      )
   }

   fun mapRow(rs: ResultSet, columnPrefix: String = "div_"): RegionEntity =
      RegionEntity(
         id = rs.getLong("${columnPrefix}id"),
         uuRowId = rs.getUuid("${columnPrefix}uu_row_id"),
         timeCreated = rs.getOffsetDateTime("${columnPrefix}time_created"),
         timeUpdated = rs.getOffsetDateTime("${columnPrefix}time_updated"),
         division = mapDivision(rs,"div_"),
         number = rs.getInt("${columnPrefix}number"),
         name = rs.getString("${columnPrefix}name"),
         employeeNumber = rs.getInt("${columnPrefix}employee_number"),
         description = rs.getString("${columnPrefix}description")
      )
}
