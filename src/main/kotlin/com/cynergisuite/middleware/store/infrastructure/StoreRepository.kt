package com.cynergisuite.middleware.store.infrastructure

import com.cynergisuite.domain.PageRequest
import com.cynergisuite.domain.infrastructure.RepositoryPage
import com.cynergisuite.extensions.findFirstOrNull
import com.cynergisuite.extensions.getOffsetDateTime
import com.cynergisuite.middleware.store.Store
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
         s.time_created AS time_created,
         s.time_updated AS time_updated,
         s.number AS number,
         s.name AS name,
         s.dataset AS dataset
      FROM fastinfo_prod_import.store_vw s
   """.trimIndent()

   fun findOne(id: Long): Store? {
      val found = jdbc.findFirstOrNull("$selectBase WHERE id = :id", mapOf("id" to id), simpleStoreRowMapper)

      logger.trace("Searching for Store: {} resulted in {}", id, found)

      return found
   }

   fun findByNumber(number: Int): Store? {
      val found = jdbc.findFirstOrNull("$selectBase WHERE number = :number", mapOf("number" to number), simpleStoreRowMapper)

      logger.trace("Search for Store by number: {} resulted in {}", number, found)

      return found
   }

   fun findAll(pageRequest: PageRequest): RepositoryPage<Store> {
      var totalElements: Long? = null
      val elements = mutableListOf<Store>()

      jdbc.query(
         """
         WITH paged AS (
            $selectBase
         )
         SELECT
            p.*,
            count(*) OVER() as total_elements
         FROM paged AS p
         ORDER BY ${pageRequest.sortBy} ${pageRequest.sortDirection}
         LIMIT ${pageRequest.size}
            OFFSET ${pageRequest.offset()}
         """.trimIndent(),
         emptyMap<String, Any>()
      ) { rs ->
         if (totalElements == null) {
            totalElements = rs.getLong("total_elements")
         }

         elements.add(mapRow(rs))
      }

      return RepositoryPage(
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

   fun maybeMapRow(rs: ResultSet, columnPrefix: String): Store? =
      if (rs.getString("${columnPrefix}id") != null) {
         mapRow(rs, columnPrefix)
      } else {
         null
      }

   fun mapRow(rs: ResultSet, columnPrefix: String = EMPTY): Store =
      simpleStoreRowMapper.mapRow(rs, columnPrefix)

   fun mapRow(row: Row, columnPrefix: String = EMPTY): Store =
      Store(
         id = row.getLong("${columnPrefix}id"),
         timeCreated = row.getOffsetDateTime("${columnPrefix}time_created"),
         timeUpdated = row.getOffsetDateTime("${columnPrefix}time_updated"),
         number = row.getInteger("${columnPrefix}number"),
         name = row.getString("${columnPrefix}name"),
         dataset = row.getString("${columnPrefix}dataset")
      )
}

private class StoreRowMapper : RowMapper<Store> {
   override fun mapRow(rs: ResultSet, rowNum: Int): Store =
      mapRow(rs, EMPTY)

   fun mapRow(rs: ResultSet, columnPrefix: String): Store =
      Store(
         id = rs.getLong("${columnPrefix}id"),
         timeCreated = rs.getOffsetDateTime("${columnPrefix}time_created"),
         timeUpdated = rs.getOffsetDateTime("${columnPrefix}time_updated"),
         number = rs.getInt("${columnPrefix}number"),
         name = rs.getString("${columnPrefix}name"),
         dataset = rs.getString("${columnPrefix}dataset")
      )
}
