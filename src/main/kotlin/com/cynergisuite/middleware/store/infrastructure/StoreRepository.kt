package com.cynergisuite.middleware.store.infrastructure

import com.cynergisuite.domain.PageRequest
import com.cynergisuite.domain.infrastructure.DatasetRepository
import com.cynergisuite.domain.infrastructure.RepositoryPage
import com.cynergisuite.extensions.findFirstOrNull
import com.cynergisuite.middleware.company.Company
import com.cynergisuite.middleware.store.StoreEntity
import io.micronaut.cache.annotation.Cacheable
import io.reactiverse.reactivex.pgclient.Row
import org.apache.commons.lang3.StringUtils.EMPTY
import org.intellij.lang.annotations.Language
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.SingleColumnRowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.sql.ResultSet
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StoreRepository @Inject constructor(
   private val jdbc: NamedParameterJdbcTemplate
) : DatasetRepository {
   private val logger: Logger = LoggerFactory.getLogger(StoreRepository::class.java)

   @Language("PostgreSQL")
   fun selectBaseQuery(params: MutableMap<String, Any?>, company: Company, datasetParamKey: String = ":dataset"): String {
      params["dataset"] = company.myDataset()

      return """
         SELECT
            s.id AS id,
            s.number AS number,
            s.name AS name,
            s.dataset AS dataset
         FROM fastinfo_prod_import.store_vw s
         WHERE s.dataset = $datasetParamKey
      """
   }

   override fun findDataset(id: Long): String? {
      logger.debug("Search for dataset of store by id {}", id)

      val found = jdbc.findFirstOrNull("SELECT dataset FROM fastinfo_prod_import.store_vw WHERE id = :id", mapOf("id" to id), SingleColumnRowMapper(String::class.java))

      logger.trace("Search for dataset of store by id {} resulted in {}", id, found)

      return found
   }

   fun findOne(id: Long, company: Company): StoreEntity? {
      val params = mutableMapOf<String, Any?>("id" to id)
      val query = "${selectBaseQuery(params, company)} AND id = :id"
      val found = jdbc.findFirstOrNull(query, params) { mapRow(it, company) }

      logger.trace("Searching for Store: {} resulted in {}", id, found)

      return found
   }

   @Cacheable("store-cache")
   fun findOne(number: Int, company: Company): StoreEntity? {
      val params = mutableMapOf<String, Any?>("number" to number)
      val query = "${selectBaseQuery(params, company)} AND number = :number"
      val found = jdbc.findFirstOrNull(query, params) { mapRow(it, company) }

      logger.trace("Search for Store by number: {} resulted in {}", number, found)

      return found
   }

   fun findAll(pageRequest: PageRequest, company: Company): RepositoryPage<StoreEntity, PageRequest> {
      val params = mutableMapOf<String, Any?>()
      val query = "${selectBaseQuery(params, company)} AND number <> 9000"
      var totalElements: Long? = null
      val elements = mutableListOf<StoreEntity>()

      jdbc.query(
         """
         WITH paged AS (
            ${query}
         )
         SELECT
            p.*,
            count(*) OVER() as total_elements
         FROM paged AS p
         ORDER BY ${pageRequest.snakeSortBy()} ${pageRequest.sortDirection()}
         LIMIT ${pageRequest.size()}
            OFFSET ${pageRequest.offset()}
         """,
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

   fun exists(id: Long): Boolean {
      val exists = jdbc.queryForObject("SELECT EXISTS(SELECT id FROM fastinfo_prod_import.store_vw WHERE id = :id)", mapOf("id" to id), Boolean::class.java)!!

      logger.trace("Checking if Store: {} exists resulted in {}", id, exists)

      return exists
   }

   fun exists(number: Int): Boolean {
      val exists = jdbc.queryForObject("SELECT EXISTS(SELECT number FROM fastinfo_prod_import.store_vw WHERE number = :number)", mapOf("number" to number), Boolean::class.java)!!

      logger.trace("Checking if Store: {} exists resulted in {}", number, exists)

      return exists
   }

   fun doesNotExist(id: Long): Boolean = !exists(id)

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
