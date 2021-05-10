package com.cynergisuite.middleware.division.infrastructure

import com.cynergisuite.domain.PageRequest
import com.cynergisuite.domain.infrastructure.RepositoryPage
import com.cynergisuite.extensions.*
import com.cynergisuite.middleware.company.Company
import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.division.DivisionEntity
import com.cynergisuite.middleware.employee.infrastructure.SimpleEmployeeRepository
import org.apache.commons.lang3.StringUtils.EMPTY
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.sql.ResultSet
import javax.inject.Inject
import javax.inject.Singleton
import javax.transaction.Transactional

@Singleton
class DivisionRepository @Inject constructor(
   private val jdbc: NamedParameterJdbcTemplate,
   private val simpleEmployeeRepository: SimpleEmployeeRepository
) {
   private val logger: Logger = LoggerFactory.getLogger(DivisionRepository::class.java)

   private fun selectBaseQuery(): String {
      return """
         WITH emp AS (
            ${simpleEmployeeRepository.employeeBaseQuery()}
         )
         SELECT
            div.id                                 AS div_id,
            div.number                             AS div_number,
            div.name                               AS div_name,
            div.manager_number                     AS div_manager_number,
            div.description                        AS div_description,
            div.effective_date                     AS div_effective_date,
            div.ending_date                        AS div_ending_date,
            emp.emp_id                             AS emp_id,
            emp.emp_type                           AS emp_type,
            emp.emp_number                         AS emp_number,
            emp.emp_last_name                      AS emp_last_name,
            emp.emp_first_name_mi                  AS emp_first_name_mi,
            emp.emp_pass_code                      AS emp_pass_code,
            emp.emp_active                         AS emp_active,
            emp.emp_department                     AS emp_department,
            emp. emp_cynergi_system_admin          AS emp_cynergi_system_admin,
            emp.emp_alternative_store_indicator    AS emp_alternative_store_indicator,
            emp.emp_alternative_area               AS emp_alternative_area,
            emp.comp_id                            AS comp_id,
            emp.comp_uu_row_id                     AS comp_uu_row_id,
            emp.comp_time_created                  AS comp_time_created,
            emp.comp_time_updated                  AS comp_time_updated,
            emp.comp_name                          AS comp_name,
            emp.comp_doing_business_as             AS comp_doing_business_as,
            emp.comp_client_code                   AS comp_client_code,
            emp.comp_client_id                     AS comp_client_id,
            emp.comp_dataset_code                  AS comp_dataset_code,
            emp.comp_federal_id_number             AS comp_federal_id_number,
            emp.address_id                         AS address_id,
            emp.address_name                       AS address_name,
            emp.address_address1                   AS address_address1,
            emp.address_address2                   AS address_address2,
            emp.address_city                       AS address_city,
            emp.address_state                      AS address_state,
            emp.address_postal_code                AS address_postal_code,
            emp.address_latitude                   AS address_latitude,
            emp.address_longitude                  AS address_longitude,
            emp.address_country                    AS address_country,
            emp.address_county                     AS address_county,
            emp.address_phone                      AS address_phone,
            emp.address_fax                        AS address_fax,
            emp.dept_id                            AS dept_id,
            emp.dept_code                          AS dept_code,
            emp.dept_description                   AS dept_description
	      FROM division AS div
            LEFT JOIN emp ON emp.emp_number = div.manager_number
      """
   }

   fun findOne(id: Long, company: Company): DivisionEntity? {
      val params = mutableMapOf<String, Any?>("id" to id, "comp_id" to company.myId(), "comp_dataset_code" to company.myDataset())
      val query =
         """${selectBaseQuery()} WHERE div.id = :id
                                                AND div.company_id = :comp_id
                                                AND (div.manager_number IS null OR comp_dataset_code = :comp_dataset_code)"""
      logger.trace("Searching for Division params {}: \nQuery {}", params, query)

      val found = jdbc.findFirstOrNull(
         query, params
      ) { rs, _ ->
         mapRow(rs, company, "div_")
      }

      logger.trace("Searching for Division params {}: \nQuery {} \nResulted in {}", params, query, found)

      return found
   }

   fun findAll(company: Company, page: PageRequest): RepositoryPage<DivisionEntity, PageRequest> {
      val params = mutableMapOf<String, Any?>("comp_id" to company.myId(), "comp_dataset_code" to company.myDataset())
      val query =
         """
         WITH paged AS (
            ${selectBaseQuery()}
            WHERE div.company_id = :comp_id AND (div.manager_number IS null OR comp_dataset_code = :comp_dataset_code)
         )
         SELECT
            p.*,
            count(*) OVER() as total_elements
         FROM paged AS p
         ORDER by div_${page.snakeSortBy()} ${page.sortDirection()}
         LIMIT ${page.size()} OFFSET ${page.offset()}
      """
      var totalElements: Long? = null
      val resultList: MutableList<DivisionEntity> = mutableListOf()

      jdbc.query(query, params) { rs ->
         resultList.add(mapRow(rs, company, "div_"))
         if (totalElements == null) {
            totalElements = rs.getLong("total_elements")
         }
      }

      val found = RepositoryPage(
         requested = page,
         elements = resultList,
         totalElements = totalElements ?: 0
      )

      logger.trace("Searching for Division params {}: \nQuery {} \nResulted in {}", params, query, found)

      return found
   }

   @Transactional
   fun insert(entity: DivisionEntity): DivisionEntity {
      logger.debug("Inserting division {}", entity)
      
      return jdbc.insertReturning(
         """
            INSERT INTO division(company_id, name, description, manager_number, effective_date, ending_date)
            VALUES (:company_id, :name, :description, :manager_number, :effective_date, :ending_date)
            RETURNING *
         """.trimIndent(),
         mapOf(
            "company_id" to entity.company.id,
            "name" to entity.name,
            "description" to entity.description,
            "manager_number" to entity.divisionalManager?.number,
            "effective_date" to entity.effectiveDate,
            "ending_date" to entity.endingDate,
         )
      ) { rs, _ -> mapRow(rs, entity) }
   }

   @Transactional
   fun update(id: Long, entity: DivisionEntity): DivisionEntity {
      logger.debug("Updating division {}", entity)

      return jdbc.updateReturning(
         """
         UPDATE division
         SET
            company_id = :company_id,
            name = :name,
            description = :description,
            manager_number = :manager_number,
            effective_date = :effective_date,
            ending_date = :ending_date
         WHERE id = :id
         RETURNING
            *
         """.trimIndent(),
         mapOf(
            "id" to id,
            "name" to entity.name,
            "company_id" to entity.company.myId(),
            "description" to entity.description,
            "manager_number" to entity.divisionalManager?.number,
            "effective_date" to entity.effectiveDate,
            "ending_date" to entity.endingDate,
         )
      ) { rs, _ ->
         mapRow(rs, entity)
      }
   }

   @Transactional
   fun delete(id: Long, company: Company): DivisionEntity? {
      logger.debug("Deleting Division using {}/{}", id, company)

      val division = findOne(id, company)

      return if (division != null) {
         // Can not inject RegionRepository since it causes circular dependency issue
         deleteRegionToStore(division)
         deleteRegions(division)

         jdbc.deleteReturning(
            """
            DELETE FROM division
            WHERE id = :id
            RETURNING
               *""",
            mapOf("id" to id)
         ) { rs, _ -> mapRow(rs, division) }
      } else {
         null
      }
   }

   @Transactional
   fun deleteRegionToStore(division: DivisionEntity) {
      logger.debug("Deleting Region To Store belong to Division {}", division)

      jdbc.update(
         """
         DELETE FROM region_to_store
         WHERE region_id IN (
            SELECT id
            FROM region
            WHERE division_id = :division_id
         )
         """,
         mapOf("division_id" to division.id)
      )
   }

   @Transactional
   fun deleteRegions(division: DivisionEntity) {
      logger.debug("Deleting Regions belong to Division {}", division)

      jdbc.update(
         """
         DELETE FROM region
         WHERE division_id = :division_id
         """,
         mapOf("division_id" to division.id)
      )
   }

   fun mapRow(rs: ResultSet, company: Company, columnPrefix: String = EMPTY): DivisionEntity =
      DivisionEntity(
         id = rs.getLong("${columnPrefix}id"),
         company = CompanyEntity.create(company)!!, // Fix unsafe type cast by Factory method, as sequence of constructor with interface as an input doesn't work
         number = rs.getLong("${columnPrefix}number"),
         name = rs.getString("${columnPrefix}name"),
         description = rs.getString("${columnPrefix}description"),
         divisionalManager = simpleEmployeeRepository.mapRowOrNull(rs),
         effectiveDate = rs.getLocalDate("${columnPrefix}effective_date"),
         endingDate = rs.getLocalDateOrNull("${columnPrefix}ending_date"),
      )

   private fun mapRow(rs: ResultSet, division: DivisionEntity, columnPrefix: String = EMPTY): DivisionEntity =
      DivisionEntity(
         id = rs.getLong("${columnPrefix}id"),
         company = division.company,
         number = rs.getLong("${columnPrefix}number"),
         name = rs.getString("${columnPrefix}name"),
         description = rs.getString("${columnPrefix}description"),
         divisionalManager = division.divisionalManager,
         effectiveDate = rs.getLocalDate("${columnPrefix}effective_date"),
         endingDate = rs.getLocalDateOrNull("${columnPrefix}ending_date"),
      )
}
