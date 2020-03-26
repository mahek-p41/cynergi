package com.cynergisuite.middleware.store.infrastructure

import com.cynergisuite.domain.PageRequest
import com.cynergisuite.domain.infrastructure.DatasetRequiringRepository
import com.cynergisuite.domain.infrastructure.RepositoryPage
import com.cynergisuite.extensions.findFirstOrNull
import com.cynergisuite.extensions.getOffsetDateTime
import com.cynergisuite.extensions.getUuid
import com.cynergisuite.middleware.company.Company
import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.division.DivisionEntity
import com.cynergisuite.middleware.location.Location
import com.cynergisuite.middleware.region.RegionEntity
import com.cynergisuite.middleware.region.infrastructure.RegionRepository
import com.cynergisuite.middleware.store.SimpleStore
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
            comp.federal_id_number,
            region.id AS reg_id,
            region.uu_row_id AS reg_uu_row_id,
            region.time_created AS reg_time_created,
            region.time_updated AS reg_time_updated,
            region.division_id AS reg_division_id,
            region.number AS reg_number,
            region.name AS reg_name,
            region.description AS reg_description,
            division.id AS div_id,
            division.uu_row_id AS div_uu_row_id,
            division.time_created AS div_time_created,
            division.time_updated AS div_time_updated,
            division.number AS div_number,
            division.name AS div_name,
            division.description AS div_description
         FROM fastinfo_prod_import.store_vw store
              JOIN region_to_store r2s ON r2s.store_number = store.number
				  JOIN region ON region.id = r2s.region_id
				  JOIN division ON region.division_id = division.id
              JOIN company comp ON comp.dataset_code = store.dataset AND division.company_id = comp.id
      """
   }

   fun findOne(id: Long, company: Company): StoreEntity? {
      val params = mutableMapOf<String, Any?>("id" to id, "comp_id" to company.myId())
      val query = "${selectBaseQuery()} WHERE store.id = :id AND comp.id = :comp_id"
      val found = jdbc.findFirstOrNull(query, params) { mapRowWithRegion(it, company) }

      logger.trace("Searching for Store: {} resulted in {}", id, found)

      return found
   }

   fun findOne(number: Int, company: Company): StoreEntity? {
      val params = mutableMapOf<String, Any?>("number" to number, "comp_id" to company.myId())
      val query = "${selectBaseQuery()} WHERE store.number = :number AND comp.id = :comp_id"

      logger.debug("Searching for Store by number {}/{}", query, params)

      val found = jdbc.findFirstOrNull(query, params) { mapRow(it) }

      logger.trace("Search for Store by number: {} resulted in {}", number, found)

      return found
   }

   fun findAll(pageRequest: PageRequest, company: Company): RepositoryPage<StoreEntity, PageRequest> {
      val params = mutableMapOf<String, Any?>("comp_id" to company.myId(), "limit" to pageRequest.size(), "offset" to pageRequest.offset())
      var totalElements: Long? = null
      val elements = mutableListOf<StoreEntity>()

      jdbc.query("""
         WITH paged AS (
            ${selectBaseQuery()} WHERE comp.id = :comp_id AND store.number <> 9000
         )
         SELECT
            p.*,
            count(*) OVER() as total_elements
         FROM paged AS p
         ORDER BY ${pageRequest.snakeSortBy()} ${pageRequest.sortDirection()}
         LIMIT :limit OFFSET :offset""",
         params
      ) { rs ->
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
              JOIN region_to_store r2s ON r2s.store_number = store.number
				  JOIN region ON region.id = r2s.region_id
				  JOIN division ON region.division_id = division.id AND division.company_id = comp.id
         WHERE store.id = :store_id AND comp.id = :comp_id
      """.trimIndent(), mapOf("store_id" to id, "comp_id" to company.myId()), Boolean::class.java)!!

      logger.trace("Checking if Store: {} exists resulted in {}", id, exists)

      return exists
   }

   fun exists(number: Int, company: Company): Boolean {
      val exists = jdbc.queryForObject("""
         SELECT count(store.id) > 0
         FROM fastinfo_prod_import.store_vw store
              JOIN company comp ON comp.dataset_code = store.dataset
              JOIN region_to_store r2s ON r2s.store_number = store.number
				  JOIN region ON region.id = r2s.region_id
				  JOIN division ON region.division_id = division.id AND division.company_id = comp.id
         WHERE store.number = :store_number AND comp.id = :comp_id
      """.trimIndent(), mapOf("store_number" to number, "comp_id" to company.myId()), Boolean::class.java)!!

      logger.trace("Checking if Store: {} exists resulted in {}", number, exists)

      return exists
   }

   fun doesNotExist(id: Long, company: Company): Boolean = !exists(id, company)

   @Transactional
   fun assignToRegion(store: Location, region: RegionEntity): Pair<RegionEntity, Location> {
      logger.trace("Assigning Store {} to Region {}", store, region)

      jdbc.update("""
         INSERT INTO region_to_store (region_id, store_number)
         VALUES (:region_id, :store_number)
         """.trimIndent(),
         mapOf(
            "region_id" to region.id,
            "store_number" to store.myNumber()
         )
      )

      return region to store
   }

   private fun mapRow(rs: ResultSet) =
      StoreEntity(
         id = rs.getLong("id"),
         number = rs.getInt("number"),
         name = rs.getString("name"),
         region = RegionEntity(
            id = rs.getLong("reg_id"),
            uuRowId = rs.getUuid("reg_uu_row_id"),
            timeCreated = rs.getOffsetDateTime("reg_time_created"),
            timeUpdated = rs.getOffsetDateTime("reg_time_updated"),
            number = rs.getInt("reg_number"),
            name = rs.getString("reg_name"),
            description = rs.getString("reg_description"),
            division = DivisionEntity(
               id = rs.getLong("div_id"),
               uuRowId = rs.getUuid("div_uu_row_id"),
               timeCreated = rs.getOffsetDateTime("div_time_created"),
               timeUpdated = rs.getOffsetDateTime("div_time_updated"),
               number = rs.getInt("div_number"),
               name = rs.getString("div_name"),
               description = rs.getString("div_description"),
               company = CompanyEntity(
                  id = rs.getLong("comp_id"),
                  uuRowId = rs.getUuid("comp_uu_row_id"),
                  timeCreated = rs.getOffsetDateTime("comp_time_created"),
                  timeUpdated = rs.getOffsetDateTime("comp_time_updated"),
                  name = rs.getString("comp_name"),
                  doingBusinessAs = rs.getString("comp_doing_business_as"),
                  clientCode = rs.getString("comp_client_code"),
                  clientId = rs.getInt("comp_client_id"),
                  datasetCode = rs.getString("comp_dataset_code"),
                  federalIdNumber = rs.getString("federal_id_number")
               )
            )
         )
      )

   fun mapRow(rs: ResultSet, company: Company, columnPrefix: String = EMPTY): Store =
      SimpleStore(
         id = rs.getLong("${columnPrefix}id"),
         number = rs.getInt("${columnPrefix}number"),
         name = rs.getString("${columnPrefix}name"),
         company = company
      )

   fun mapRowWithRegion(rs: ResultSet, company: Company, columnPrefix: String = EMPTY): StoreEntity =
         StoreEntity(
            id = rs.getLong("${columnPrefix}id"),
            number = rs.getInt("${columnPrefix}number"),
            name = rs.getString("${columnPrefix}name"),
            region = regionRepository.mapRow(rs, company, "reg_")
         )


   fun mapRowOrNull(rs: ResultSet, company: Company, columnPrefix: String = EMPTY): Store? =
      if (rs.getString("${columnPrefix}id") != null) {
         mapRow(rs, company, columnPrefix)
      } else {
         null
      }
}
