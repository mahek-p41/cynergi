package com.cynergisuite.middleware.region.infrastructure

import com.cynergisuite.domain.PageRequest
import com.cynergisuite.domain.infrastructure.RepositoryPage
import com.cynergisuite.extensions.deleteReturning
import com.cynergisuite.extensions.findFirstOrNull
import com.cynergisuite.extensions.insertReturning
import com.cynergisuite.extensions.updateReturning
import com.cynergisuite.middleware.company.Company
import com.cynergisuite.middleware.division.infrastructure.DivisionRepository
import com.cynergisuite.middleware.employee.infrastructure.SimpleEmployeeRepository
import com.cynergisuite.middleware.region.RegionEntity
import com.cynergisuite.middleware.store.Store
import org.apache.commons.lang3.StringUtils.EMPTY
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.sql.ResultSet
import java.sql.SQLException
import javax.inject.Inject
import javax.inject.Singleton
import javax.transaction.Transactional

@Singleton
class RegionRepository @Inject constructor(
   private val jdbc: NamedParameterJdbcTemplate,
   private val divisionRepository: DivisionRepository,
   private val simpleEmployeeRepository: SimpleEmployeeRepository
) {
   private val logger: Logger = LoggerFactory.getLogger(RegionRepository::class.java)

   fun selectBaseQuery(): String {
      return """
         WITH emp AS (
            ${simpleEmployeeRepository.employeeBaseQuery()}
         )
         SELECT
               reg.id                                 AS reg_id,
               reg.name                               AS reg_name,
               reg.number                             AS reg_number,
               reg.manager_number                     AS reg_manager_number,
               reg.division_id                        AS reg_division_id,
               reg.description                        AS reg_description,
               div.id                                 AS div_id,
               div.number                             AS div_number,
               div.name                               AS div_name,
               div.manager_number                     AS div_manager_number,
               div.description                        AS div_description,
               emp.emp_id                             AS emp_id,
               emp.emp_type                           AS emp_type,
               emp.emp_number                         AS emp_number,
               emp.emp_last_name                      AS emp_last_name,
               emp.emp_first_name_mi                  AS emp_first_name_mi,
               emp.emp_pass_code                      AS emp_pass_code,
               emp.emp_active                         AS emp_active,
               emp.emp_department                     AS emp_department,
               emp.emp_cynergi_system_admin           AS emp_cynergi_system_admin,
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
	      FROM region AS reg
            JOIN division AS div ON div.id = reg.division_id
            LEFT JOIN emp ON emp.emp_number = reg.manager_number
      """
   }

   fun findOne(id: Long, company: Company): RegionEntity? {
      val params = mutableMapOf<String, Any?>("id" to id, "comp_id" to company.myId(), "comp_dataset_code" to company.myDataset())
      val query =
         """${selectBaseQuery()} WHERE reg.id = :id
                                                AND div.company_id = :comp_id
                                                AND (reg.manager_number IS null OR comp_dataset_code = :comp_dataset_code)"""

      logger.trace("Searching for Region params {}: \nQuery {}", params, query)

      val found = jdbc.findFirstOrNull(
         query,
         params
      ) { rs, _ ->
         mapRow(rs, company, "reg_")
      }

      logger.trace("Searching for Region params {}: \nQuery {} \nResulted in {}", params, query, found)

      return found
   }

   fun findAll(company: Company, page: PageRequest): RepositoryPage<RegionEntity, PageRequest> {
      val params = mutableMapOf<String, Any?>("comp_id" to company.myId(), "comp_dataset_code" to company.myDataset())
      val query =
         """
         WITH paged AS (
            ${selectBaseQuery()}
            WHERE div.company_id = :comp_id
                  AND (reg.manager_number IS null OR comp_dataset_code = :comp_dataset_code)
         )
         SELECT
            p.*,
            count(*) OVER() as total_elements
         FROM paged AS p
         ORDER by reg_${page.snakeSortBy()} ${page.sortDirection()}
         LIMIT ${page.size()} OFFSET ${page.offset()}
      """
      var totalElements: Long? = null
      val resultList: MutableList<RegionEntity> = mutableListOf()

      jdbc.query(query, params) { rs ->
         resultList.add(mapRow(rs, company, "reg_"))

         if (totalElements == null) {
            totalElements = rs.getLong("total_elements")
         }
      }

      return RepositoryPage(
         requested = page,
         elements = resultList,
         totalElements = totalElements ?: 0
      )
   }

   @Transactional
   fun insert(entity: RegionEntity): RegionEntity {
      logger.debug("Inserting region {}", entity)

      return jdbc.insertReturning(
         """
            INSERT INTO region(division_id, name, description, manager_number)
            VALUES (:division_id, :name, :description, :manager_number)
            RETURNING *
         """.trimIndent(),
         mapOf(
            "division_id" to entity.division.id,
            "name" to entity.name,
            "description" to entity.description,
            "manager_number" to entity.regionalManager?.number,
         )
      ) { rs, _ -> mapRow(rs, entity) }
   }

   @Transactional
   fun update(id: Long, entity: RegionEntity): RegionEntity {
      logger.debug("Updating region {}", entity)

      return jdbc.updateReturning(
         """
         UPDATE region
         SET
            division_id = :division_id,
            name = :name,
            description = :description,
            manager_number = :manager_number
         WHERE id = :id
         RETURNING
            *
         """.trimIndent(),
         mapOf(
            "id" to id,
            "name" to entity.name,
            "division_id" to entity.division.id,
            "description" to entity.description,
            "manager_number" to entity.regionalManager?.number,
         )
      ) { rs, _ ->
         mapRow(rs, entity)
      }
   }

   @Transactional
   fun delete(id: Long, company: Company): RegionEntity? {
      logger.debug("Deleting Region using {}/{}", id, company)

      val region = findOne(id, company)

      return if (region != null) {
         // Can not inject DivisionRepository since it causes circular dependency issue
         deleteRegionToStore(region)

         jdbc.deleteReturning(
            """
            DELETE FROM region
            WHERE id = :id
            RETURNING
               *""",
            mapOf("id" to id)
         ) { rs, _ -> mapRow(rs, region) }
      } else {
         null
      }
   }

   @Transactional
   private fun deleteRegionToStore(region: RegionEntity) {
      logger.debug("Deleting Region To Store belong to region {}", region)

      jdbc.update(
         """
         DELETE FROM region_to_store
         WHERE region_id = :region_id
         """,
         mapOf("region_id" to region.id)
      )
   }

   @Transactional
   fun disassociateStoreFromRegion(region: RegionEntity, store: Store, company: Company) {
      logger.debug("Deleting Region To Store region id {}, store number {}", region, store)

      jdbc.update(
         """
         DELETE FROM region_to_store
         WHERE region_id = :region_id AND store_number = :store_number
         """,
         mapOf(
            "region_id" to region.myId(),
            "store_number" to store.myId()
         )
      )
   }

   fun assignStoreToRegion(region: RegionEntity, store: Store, company: Company) {
      logger.trace("Assigning Store {} to Region {}", region, store)

      jdbc.update(
         """
         INSERT INTO region_to_store (region_id, store_number, company_id)
         VALUES (:region_id, :store_number, :company_id)
         """.trimIndent(),
         mapOf(
            "region_id" to region.myId(),
            "store_number" to store.myNumber(),
            "company_id" to company.myId()
         )
      )
   }

   fun reassignStoreToRegion(region: RegionEntity, store: Store, company: Company) {
      logger.trace("Re-assigning Store {} to Region {}", region, store)

      jdbc.update(
         """
         UPDATE region_to_store
         SET
            region_id = :region_id
         WHERE store_number = :store_number AND company_id = :company_id
         """,
         mapOf(
            "region_id" to region.myId(),
            "store_number" to store.myNumber(),
            "company_id" to company.myId()
         )
      )
   }

   fun isStoreAssignedToRegion(store: Store, company: Company): Boolean {
      val exists = jdbc.queryForObject(
         """
         SELECT EXISTS (SELECT * FROM region_to_store WHERE store_number = :store_number AND company_id = :company_id)
         """,
         mapOf("store_number" to store.myNumber(), "company_id" to company.myId()),
         Boolean::class.java
      )!!

      logger.trace("Checking if a store is assigned to a region")

      return exists
   }

   private fun mapRow(rs: ResultSet, company: Company, columnPrefix: String = "reg_"): RegionEntity =
      RegionEntity(
         id = rs.getLong("${columnPrefix}id"),
         number = rs.getLong("${columnPrefix}number"),
         division = divisionRepository.mapRow(rs, company, "div_"),
         name = rs.getString("${columnPrefix}name"),
         description = rs.getString("${columnPrefix}description"),
         regionalManager = simpleEmployeeRepository.mapRowOrNull(rs),
      )

   fun mapRowOrNull(rs: ResultSet, company: Company, columnPrefix: String = "reg_", companyPrefix: String = "comp_", departmentPrefix: String = "dept_", storePrefix: String = "store_"): RegionEntity? =
      try {
         if (rs.getString("${columnPrefix}id") != null) {
            mapRow(rs, company)
         } else {
            null
         }
      } catch (e: SQLException) {
         logger.error("Unable to map Region", e)
         null
      }

   fun mapRow(rs: ResultSet, region: RegionEntity, columnPrefix: String = EMPTY): RegionEntity =
      RegionEntity(
         id = rs.getLong("${columnPrefix}id"),
         number = rs.getLong("${columnPrefix}number"),
         division = region.division,
         name = rs.getString("${columnPrefix}name"),
         description = rs.getString("${columnPrefix}description"),
         regionalManager = region.regionalManager,
      )
}
