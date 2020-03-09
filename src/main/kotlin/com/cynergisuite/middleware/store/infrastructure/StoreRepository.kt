package com.cynergisuite.middleware.store.infrastructure

import com.cynergisuite.domain.PageRequest
import com.cynergisuite.domain.infrastructure.DatasetRequiringRepository
import com.cynergisuite.domain.infrastructure.RepositoryPage
import com.cynergisuite.extensions.findFirstOrNull
import com.cynergisuite.extensions.insertReturning
import com.cynergisuite.middleware.company.Company
import com.cynergisuite.middleware.company.infrastructure.CompanyRepository
import com.cynergisuite.middleware.region.RegionEntity
import com.cynergisuite.middleware.store.StoreEntity
import io.micronaut.spring.tx.annotation.Transactional
import io.reactiverse.reactivex.pgclient.Row
import org.apache.commons.lang3.StringUtils.EMPTY
import org.bouncycastle.util.Store
import org.intellij.lang.annotations.Language
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.sql.ResultSet
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StoreRepository @Inject constructor(
   private val jdbc: NamedParameterJdbcTemplate,
   private val companyRepository: CompanyRepository
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
            comp.federal_id_number
         FROM fastinfo_prod_import.store_vw store
              JOIN company comp ON comp.dataset_code = store.dataset
      """
   }

   fun findOne(id: Long, company: Company): StoreEntity? {
      val params = mutableMapOf<String, Any?>("id" to id, "comp_id" to company.myId())
      val query = "${selectBaseQuery()} WHERE store.id = :id AND comp.id = :comp_id"
      val found = jdbc.findFirstOrNull(query, params) { mapRow(it, company) }

      logger.trace("Searching for Store: {} resulted in {}", id, found)

      return found
   }

   fun findOne(number: Int, company: Company): StoreEntity? {
      val params = mutableMapOf<String, Any?>("number" to number, "comp_id" to company.myId())
      val query = "${selectBaseQuery()} WHERE store.number = :number AND comp.id = :comp_id"
      val found = jdbc.findFirstOrNull(query, params) { mapRow(it, company) }

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

         elements.add(mapRow(rs, company))
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
         FROM company comp
              JOIN fastinfo_prod_import.store_vw store ON comp.dataset_code = store.dataset
         WHERE store.id = :store_id AND comp.id = :comp_id
      """.trimIndent(), mapOf("store_id" to id, "comp_id" to company.myId()), Boolean::class.java)!!

      logger.trace("Checking if Store: {} exists resulted in {}", id, exists)

      return exists
   }

   fun exists(number: Int, company: Company): Boolean {
      val exists = jdbc.queryForObject("""
         SELECT count(store.id) > 0
         FROM company comp
              JOIN fastinfo_prod_import.store_vw store ON comp.dataset_code = store.dataset
         WHERE store.number = :store_number AND comp.id = :comp_id
      """.trimIndent(), mapOf("store_number" to number, "comp_id" to company.myId()), Boolean::class.java)!!

      logger.trace("Checking if Store: {} exists resulted in {}", number, exists)

      return exists
   }

   fun doesNotExist(id: Long, company: Company): Boolean = !exists(id, company)

   fun mapRow(rs: ResultSet, columnPrefix: String = EMPTY): StoreEntity =
      StoreEntity(
         id = rs.getLong("${columnPrefix}id"),
         number = rs.getInt("${columnPrefix}number"),
         name = rs.getString("${columnPrefix}name"),
         company = companyRepository.mapRow(rs)
      )

   @Transactional
   fun assignToRegion(store: StoreEntity, region: RegionEntity): Pair<RegionEntity, StoreEntity> {
      logger.trace("Assigning Store {} to Region {}", store, region)

      jdbc.update("""
         INSERT INTO region_to_store (region_id, store_number)
         VALUES (:region_id, :store_number)
         """.trimIndent(),
         mapOf(
            "region_id" to region.id,
            "store_number" to store.number
         )
      )

      return region to store
   }

   fun mapRow(resultSet: ResultSet, company: Company, columnPrefix: String = EMPTY): StoreEntity =
      StoreEntity(
         id = resultSet.getLong("${columnPrefix}id"),
         number = resultSet.getInt("${columnPrefix}number"),
         name = resultSet.getString("${columnPrefix}name"),
         company = company
      )

   fun maybeMapRow(rs: ResultSet, company: Company, columnPrefix: String): StoreEntity? =
      if (rs.getString("${columnPrefix}id") != null) {
         mapRow(rs, company, columnPrefix)
      } else {
         null
      }

   fun mapRowOrNull(rs: ResultSet, company: Company, columnPrefix: String = EMPTY): StoreEntity? =
      if (rs.getString("${columnPrefix}id") != null) {
         mapRow(rs, company, columnPrefix)
      } else {
         null
      }

   fun mapRow(row: Row, company: Company, columnPrefix: String = EMPTY): StoreEntity? =
      if (row.getLong("${columnPrefix}id") != null) {
         StoreEntity(
            id = row.getLong("${columnPrefix}id"),
            number = row.getInteger("${columnPrefix}number"),
            name = row.getString("${columnPrefix}name"),
            company = company
         )
      } else {
         null
      }
}
