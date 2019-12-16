package com.cynergisuite.middleware.company.infrastructure

import com.cynergisuite.domain.PageRequest
import com.cynergisuite.domain.infrastructure.RepositoryPage
import com.cynergisuite.extensions.findFirstOrNull
import com.cynergisuite.extensions.getOffsetDateTime
import com.cynergisuite.middleware.company.CompanyEntity
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
class CompanyRepository @Inject constructor(
   private val jdbc: NamedParameterJdbcTemplate
) {
   private val logger: Logger = LoggerFactory.getLogger(CompanyRepository::class.java)
   private val simpleCompanyRowMapper = CompanyRowMapper()

   @Language("PostgreSQL")
   final val selectBase = """
      SELECT
         c.id AS id,
         c.time_created AS time_created,
         c.time_updated AS time_updated,
         c.number AS number,
         c.name AS name,
         c.dataset AS dataset
      FROM fastinfo_prod_import.company_vw c
   """.trimIndent()

   fun findCompanyByStore(store: StoreEntity): CompanyEntity? {
      logger.debug("Search for company using store id {}", store.id)

      val found = jdbc.findFirstOrNull("""
         SELECT
            c.id AS id,
            c.time_created AS time_created,
            c.time_updated AS time_updated,
            c.number AS number,
            c.name AS name,
            c.dataset AS dataset
         FROM fastinfo_prod_import.company_vw c
         JOIN fastinfo_prod_import.store_vw s
         ON c.id = s.company_id
         WHERE s.id = :store_id
         """.trimIndent(),
         mapOf("store_id" to store.id),
         RowMapper { rs, _ -> mapRow(rs) }
      )

      logger.debug("Searching for company by store id {} resulted in {}", store.id, found)

      return found
   }

   fun findOne(id: Long): CompanyEntity? {
      val found = jdbc.findFirstOrNull("$selectBase WHERE id = :id", mapOf("id" to id), simpleCompanyRowMapper)

      logger.trace("Searching for Company: {} resulted in {}", id, found)

      return found
   }

   fun findOneByNumber(number: Int): CompanyEntity? {
      val found = jdbc.findFirstOrNull("$selectBase WHERE number = :number", mapOf("number" to number), simpleCompanyRowMapper)

      logger.trace("Search for Company by number: {} resulted in {}", number, found)

      return found
   }

   fun findAll(pageRequest: PageRequest): RepositoryPage<CompanyEntity, PageRequest> {
      var totalElements: Long? = null
      val elements = mutableListOf<CompanyEntity>()

      jdbc.query(
         """
         WITH paged AS (
            $selectBase
         )
         SELECT
            p.*,
            count(*) OVER() as total_elements
         FROM paged AS p
         ORDER BY ${pageRequest.snakeSortBy()} ${pageRequest.sortDirection()}
         LIMIT ${pageRequest.size()}
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
         requested = pageRequest,
         elements = elements,
         totalElements = totalElements ?: 0
      )
   }

   fun exists(id: Long): Boolean {
      val exists = jdbc.queryForObject("SELECT EXISTS(SELECT id FROM fastinfo_prod_import.company_vw WHERE id = :id)", mapOf("id" to id), Boolean::class.java)!!

      logger.trace("Checking if Company: {} exists resulted in {}", id, exists)

      return exists
   }

   fun exists(number: Int): Boolean {
      val exists = jdbc.queryForObject("SELECT EXISTS(SELECT number FROM fastinfo_prod_import.company_vw WHERE number = :number)", mapOf("number" to number), Boolean::class.java)!!

      logger.trace("Checking if Company: {} exists resulted in {}", number, exists)

      return exists
   }

   fun doesNotExist(id: Long): Boolean = !exists(id)

   fun maybeMapRow(rs: ResultSet, columnPrefix: String): CompanyEntity? =
      if (rs.getString("${columnPrefix}id") != null) {
         mapRow(rs, columnPrefix)
      } else {
         null
      }

   fun mapRow(rs: ResultSet, columnPrefix: String = EMPTY): CompanyEntity =
      simpleCompanyRowMapper.mapRow(rs, columnPrefix)

   fun mapRow(row: Row, columnPrefix: String = EMPTY): CompanyEntity =
      CompanyEntity(
         id = row.getLong("${columnPrefix}id"),
         timeCreated = row.getOffsetDateTime("${columnPrefix}time_created"),
         timeUpdated = row.getOffsetDateTime("${columnPrefix}time_updated"),
         number = row.getInteger("${columnPrefix}number"),
         name = row.getString("${columnPrefix}name"),
         dataset = row.getString("${columnPrefix}dataset")
      )
}

private class CompanyRowMapper : RowMapper<CompanyEntity> {
   override fun mapRow(rs: ResultSet, rowNum: Int): CompanyEntity =
      mapRow(rs, EMPTY)

   fun mapRow(rs: ResultSet, columnPrefix: String): CompanyEntity =
      CompanyEntity(
         id = rs.getLong("${columnPrefix}id"),
         timeCreated = rs.getOffsetDateTime("${columnPrefix}time_created"),
         timeUpdated = rs.getOffsetDateTime("${columnPrefix}time_updated"),
         number = rs.getInt("${columnPrefix}number"),
         name = rs.getString("${columnPrefix}name"),
         dataset = rs.getString("${columnPrefix}dataset")
      )
}
