package com.cynergisuite.middleware.division.infrastructure

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
import io.micronaut.spring.tx.annotation.Transactional
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
) : DatasetRequiringRepository {
   private val logger: Logger = LoggerFactory.getLogger(DivisionRepository::class.java)

   fun divisionBaseQuery() = """
      SELECT
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
      FROM division div
         JOIN company comp ON div.company_id = comp.id
   """

   fun findOne(id: Long, company: Company): DivisionEntity? {
      logger.debug("Search for division by id {}", id)

      val found = jdbc.findFirstOrNull("${divisionBaseQuery()} WHERE div.id = :id AND comp.id = :comp_id", mapOf("id" to id, "comp_id" to company.myId()),
         RowMapper { rs, _ -> mapRow(rs, company) })

      logger.trace("Searching for division by id {} resulted in {}", id, found)

      return found
   }

   fun findAll(pageRequest: PageRequest, company: Company): RepositoryPage<DivisionEntity, PageRequest> {
      var totalElements: Long? = null
      val elements = mutableListOf<DivisionEntity>()

      val whereBuilder = StringBuilder(" WHERE comp.id = :comp_id ")

      jdbc.query("""
         WITH paged AS (
            ${divisionBaseQuery()}
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

         elements.add(mapRow(rs, company, "div_"))
      }

      return RepositoryPage(
         requested = pageRequest,
         elements = elements,
         totalElements = totalElements ?: 0
      )
   }

   override fun exists(id: Long, company: Company): Boolean {
      val exists = jdbc.queryForObject("SELECT EXISTS(SELECT id FROM division WHERE id = :id)", mapOf("id" to id), Boolean::class.java)!!

      logger.trace("Checking if Division: {} exists resulted in {}", id, exists)

      return exists
   }

   fun doesNotExist(id: Long, company: Company): Boolean = !exists(id, company)

   fun doesNotExist(division: DivisionEntity, company: Company): Boolean {
      val divisionId = division.myId()

      return if (divisionId != null) {
         !exists(divisionId, company)
      } else {
         false
      }
   }

   @Transactional
   fun insert(division: DivisionEntity, company: Company): DivisionEntity {
      logger.debug("Inserting division {}", division)

      return jdbc.insertReturning("""
         INSERT INTO division(company_id, number, name, employeeNumber, description)
         VALUES (:company_id, :number, :name, :employeeNumber, :description)
         RETURNING
            *
         """,
         mapOf(
            "company_id" to company.myId(),
            "number" to division.number,
            "name" to division.name,
            "employeeNumber" to division.employeeNumber,
            "description" to division.description
         ),
         RowMapper { rs, _ -> mapRow(rs, company, "div_") }
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

   fun mapRow(rs: ResultSet, company: Company, columnPrefix: String = "div_"): DivisionEntity =
      DivisionEntity(
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
