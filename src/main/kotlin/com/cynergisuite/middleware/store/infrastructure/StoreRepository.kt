package com.cynergisuite.middleware.store.infrastructure

import com.cynergisuite.domain.PageRequest
import com.cynergisuite.domain.infrastructure.DatasetRequiringRepository
import com.cynergisuite.domain.infrastructure.RepositoryPage
import com.cynergisuite.extensions.findFirstOrNull
import com.cynergisuite.middleware.authentication.user.User
import com.cynergisuite.middleware.company.Company
import com.cynergisuite.middleware.location.Location
import com.cynergisuite.middleware.region.RegionEntity
import com.cynergisuite.middleware.region.infrastructure.RegionRepository
import com.cynergisuite.middleware.store.Store
import com.cynergisuite.middleware.store.StoreEntity
import io.micronaut.spring.tx.annotation.Transactional
import org.apache.commons.lang3.StringUtils.EMPTY
import org.intellij.lang.annotations.Language
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.sql.ResultSet
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StoreRepository @Inject constructor(
   private val jdbc: NamedParameterJdbcTemplate,
   private val regionRepository: RegionRepository
) : DatasetRequiringRepository {
   private val logger: Logger = LoggerFactory.getLogger(StoreRepository::class.java)

   @Language("PostgreSQL")
   private fun selectBaseQuery(): String {
      return """
         SELECT
            store.id AS id,
            store.number AS number,
            store.name AS name,
            comp.id AS comp_id,
            comp.uu_row_id AS comp_uu_row_id,
            comp.time_created AS comp_time_created,
            comp.time_updated AS comp_time_updated,
            comp.name AS comp_name,
            comp.doing_business_as AS comp_doing_business_as,
            comp.client_code AS comp_client_code,
            comp.client_id AS comp_client_id,
            comp.dataset_code AS comp_dataset_code,
            comp.federal_id_number AS comp_federal_id_number,
            region.id AS reg_id,
            region.number AS reg_number,
            region.division_id AS reg_division_id,
            region.name AS reg_name,
            region.description AS reg_description,
            division.id AS div_id,
            division.number AS div_number,
            division.name AS div_name,
            division.description AS div_description
         FROM fastinfo_prod_import.store_vw store
              JOIN company comp ON comp.dataset_code = store.dataset
              LEFT OUTER JOIN region_to_store r2s ON r2s.store_number = store.number AND r2s.company_id = :comp_id
              LEFT OUTER JOIN region ON r2s.region_id = region.id
              LEFT OUTER JOIN division ON region.division_id = division.id
      """
   }

   /**
    * The sub-query added to make sure we get unassigned store
    * but won't get the store of region belong to another company
    * which have the same store number
    **/
   private val subQuery = """
      (r2s.region_id IS null
         OR r2s.region_id NOT IN (
               SELECT region.id
               FROM region JOIN division ON region.division_id = division.id
               WHERE division.company_id <> :comp_id
            ))
   """

   fun findOne(id: Long, company: Company): StoreEntity? {
      val params = mutableMapOf<String, Any?>("id" to id, "comp_id" to company.myId())
      val query = """
         ${selectBaseQuery()}
         WHERE store.id = :id
               AND comp.id = :comp_id
               AND $subQuery
      """.trimMargin()

      logger.trace("{} / {}", query, params)

      val found = jdbc.findFirstOrNull(query, params) { mapRowWithRegion(it, company) }

      logger.trace("Searching for Store with params {} resulted in {}", params, found)

      return found
   }

   fun findOne(number: Int, company: Company): StoreEntity? {
      val params = mutableMapOf("number" to number, "comp_id" to company.myId())
      val query = """${selectBaseQuery()} WHERE store.number = :number AND comp.id = :comp_id
                                                AND $subQuery
      """.trimMargin()

      logger.debug("Searching for Store by number {}/{}", query, params)

      val found = jdbc.findFirstOrNull(query, params) { mapRowWithRegion(it, company) }

      logger.trace("Search for Store by number with params resulted in {}", params, found)

      return found
   }

   fun findAll(pageRequest: PageRequest, user: User): RepositoryPage<StoreEntity, PageRequest> {
      val company = user.myCompany()
      val params = mutableMapOf("comp_id" to company.myId(), "limit" to pageRequest.size(), "offset" to pageRequest.offset())
      var totalElements: Long? = null
      val elements = mutableListOf<StoreEntity>()
      val pagedQuery = StringBuilder("${selectBaseQuery()} WHERE comp.id = :comp_id AND $subQuery")

      when(user.myAlternativeStoreIndicator()) {
         "N" -> {
            pagedQuery.append(" AND store.number = :store_number ")
            params["store_number"] = user.myLocation().myNumber()
         }
         "R" -> {
            pagedQuery.append(" AND region.id = :region_id ")
            params["region_id"] = user.myAlternativeArea()
         }
         "D" -> {
            pagedQuery.append(" AND division.number = :division_number ")
            params["division_number"] = user.myAlternativeArea()
         }
         else -> {
            pagedQuery.append(" AND store.number <> 9000 ")
         }
      }

      val query = """
         WITH paged AS (
            $pagedQuery
         )
         SELECT
            p.*,
            count(*) OVER() as total_elements
         FROM paged AS p
         ORDER BY ${pageRequest.snakeSortBy()} ${pageRequest.sortDirection()}
         LIMIT :limit OFFSET :offset
      """

      logger.trace("Fetching all stores using {} / {}", query, params)

      jdbc.query(query, params) { rs ->
         if (totalElements == null) {
            totalElements = rs.getLong("total_elements")
         }

         elements.add(mapRowWithRegion(rs, company))
      }

      return RepositoryPage(
         requested = pageRequest,
         elements = elements,
         totalElements = totalElements ?: 0
      )
   }

   override fun exists(id: Long, company: Company): Boolean {
      val exists = jdbc.queryForObject("""
         SELECT count(store.id) > 0
         FROM fastinfo_prod_import.store_vw store
            JOIN company comp ON comp.dataset_code = store.dataset
            LEFT JOIN region_to_store r2s ON r2s.store_number = store.number AND r2s.company_id = comp.id
            LEFT JOIN region ON  r2s.region_id = region.id
			   LEFT JOIN division ON division.company_id = comp.id AND region.division_id = division.id
         WHERE store.id = :store_id
               AND $subQuery
      """.trimIndent(), mapOf("store_id" to id, "comp_id" to company.myId()), Boolean::class.java)!!

      logger.trace("Checking if Store: {} exists resulted in {}", id, exists)

      return exists
   }

   /**
    * The sub-query added to make sure we won't get the store of region belong to another company
    * which have the same store number
    **/
   fun exists(number: Int, company: Company): Boolean {
      val exists = jdbc.queryForObject("""
         SELECT count(store.id) > 0
         FROM fastinfo_prod_import.store_vw store
            JOIN company comp ON comp.dataset_code = store.dataset
            LEFT JOIN region_to_store r2s ON r2s.store_number = store.number AND r2s.company_id = comp.id
            LEFT JOIN region ON  r2s.region_id = region.id
			   LEFT JOIN division ON division.company_id = comp.id AND region.division_id = division.id
         WHERE store.number = :store_number
                  AND comp.id = :comp_id
                  AND $subQuery
      """.trimIndent(), mapOf("store_number" to number, "comp_id" to company.myId()), Boolean::class.java)!!

      logger.trace("Checking if Store: {} exists resulted in {}", number, exists)

      return exists
   }

   fun doesNotExist(id: Long, company: Company): Boolean = !exists(id, company)

   @Transactional
   fun assignToRegion(store: Location, region: RegionEntity, companyId: Long): Pair<RegionEntity, Location> {
      logger.trace("Assigning Store {} to Region {}", store, region)

      jdbc.update("""
         INSERT INTO region_to_store (region_id, store_number, company_id)
         VALUES (:region_id, :store_number, :company_id)
         """.trimIndent(),
         mapOf(
            "region_id" to region.id,
            "store_number" to store.myNumber(),
            "company_id" to companyId
         )
      )

      return region to store
   }

   fun mapRow(rs: ResultSet, company: Company, columnPrefix: String = EMPTY): Store =
      StoreEntity(
         id = rs.getLong("${columnPrefix}id"),
         number = rs.getInt("${columnPrefix}number"),
         name = rs.getString("${columnPrefix}name"),
         region = regionRepository.mapRowOrNull(rs, company, "reg_"),
         company = company
      )

   fun mapRowWithRegion(rs: ResultSet, company: Company, columnPrefix: String = EMPTY): StoreEntity =
         StoreEntity(
            id = rs.getLong("${columnPrefix}id"),
            number = rs.getInt("${columnPrefix}number"),
            name = rs.getString("${columnPrefix}name"),
            region = regionRepository.mapRowOrNull(rs, company, "reg_"),
            company = company
         )


   fun mapRowOrNull(rs: ResultSet, company: Company, columnPrefix: String = EMPTY): Store? =
      if (rs.getString("${columnPrefix}id") != null) {
         mapRow(rs, company, columnPrefix)
      } else {
         null
      }
}
