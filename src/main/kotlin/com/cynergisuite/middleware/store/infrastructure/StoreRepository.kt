package com.cynergisuite.middleware.store.infrastructure

import com.cynergisuite.domain.PageRequest
import com.cynergisuite.domain.infrastructure.RepositoryPage
import com.cynergisuite.extensions.findFirstOrNull
import com.cynergisuite.extensions.getOffsetDateTime
import com.cynergisuite.middleware.store.StoreEntity
import io.reactiverse.reactivex.pgclient.Row
import org.apache.commons.lang3.StringUtils.EMPTY
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
   private val jdbc: NamedParameterJdbcTemplate
) {
   private val logger: Logger = LoggerFactory.getLogger(StoreRepository::class.java)
   private val simpleStoreRowMapper = StoreRowMapper()

   @Language("PostgreSQL")
   final val selectBase = """
      SELECT
         s.id AS id,
         s.number AS number,
         s.name AS name,
         s.dataset AS dataset
      FROM fastinfo_prod_import.store_vw s
   """.trimIndent()

   fun findOne(id: Long): StoreEntity? {
      val found = jdbc.findFirstOrNull("$selectBase WHERE id = :id", mapOf("id" to id), simpleStoreRowMapper)

      logger.trace("Searching for Store: {} resulted in {}", id, found)

      return found
   }

   fun findOne(number: Int, dataset: String): StoreEntity? {
      val found = jdbc.findFirstOrNull("$selectBase WHERE number = :number AND s.dataset = :dataset", mapOf("number" to number, "dataset" to dataset), simpleStoreRowMapper)

      logger.trace("Search for Store by number: {} resulted in {}", number, found)

      return found
   }

   fun findAll(pageRequest: PageRequest, dataset: String): RepositoryPage<StoreEntity, PageRequest> {
      var totalElements: Long? = null
      val elements = mutableListOf<StoreEntity>()

      jdbc.query(
         """
         WITH paged AS (
            $selectBase
            WHERE number <> 9000
                  AND s.dataset = :dataset
         )
         SELECT
            p.*,
            count(*) OVER() as total_elements
         FROM paged AS p
         ORDER BY ${pageRequest.snakeSortBy()} ${pageRequest.sortDirection()}
         LIMIT ${pageRequest.size()}
            OFFSET ${pageRequest.offset()}
         """.trimIndent(),
         mapOf("dataset" to dataset)
      ) { rs ->
         if (totalElements == null) {
            totalElements = rs.getLong("total_elements")
         }

         elements.add(mapRow(rs))
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

   fun maybeMapRow(rs: ResultSet, columnPrefix: String): StoreEntity? =
      if (rs.getString("${columnPrefix}id") != null) {
         mapRow(rs, columnPrefix)
      } else {
         null
      }

   fun mapRow(rs: ResultSet, columnPrefix: String = EMPTY): StoreEntity =
      mapRowOrNull(rs, columnPrefix)!!

   fun mapRowOrNull(rs: ResultSet, columnPrefix: String = EMPTY): StoreEntity? =
      if (rs.getString("${columnPrefix}id") != null) {
         simpleStoreRowMapper.mapRow(rs, columnPrefix)
      } else {
         null
      }

   fun mapRow(row: Row, columnPrefix: String = EMPTY): StoreEntity? =
      if (row.getLong("${columnPrefix}id") != null) {
         StoreEntity(
            id = row.getLong("${columnPrefix}id"),
            number = row.getInteger("${columnPrefix}number"),
            name = row.getString("${columnPrefix}name"),
            dataset = row.getString("${columnPrefix}dataset")
         )
      } else {
         null
      }
}

private class StoreRowMapper : RowMapper<StoreEntity> {
   override fun mapRow(rs: ResultSet, rowNum: Int): StoreEntity =
      mapRow(rs, EMPTY)

   fun mapRow(rs: ResultSet, columnPrefix: String): StoreEntity =
      StoreEntity(
         id = rs.getLong("${columnPrefix}id"),
         number = rs.getInt("${columnPrefix}number"),
         name = rs.getString("${columnPrefix}name"),
         dataset = rs.getString("${columnPrefix}dataset")
      )
}
