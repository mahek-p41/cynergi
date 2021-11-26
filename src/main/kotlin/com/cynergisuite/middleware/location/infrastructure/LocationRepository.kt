package com.cynergisuite.middleware.location.infrastructure

import com.cynergisuite.domain.PageRequest
import com.cynergisuite.domain.infrastructure.RepositoryPage
import com.cynergisuite.extensions.findFirstOrNull
import com.cynergisuite.extensions.query
import com.cynergisuite.extensions.queryForObject
import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.location.LocationEntity
import io.micronaut.transaction.annotation.ReadOnly
import org.apache.commons.lang3.StringUtils.EMPTY
import org.intellij.lang.annotations.Language
import org.jdbi.v3.core.Jdbi
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.sql.ResultSet
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocationRepository @Inject constructor(
   private val jdbc: Jdbi
) {
   private val logger: Logger = LoggerFactory.getLogger(LocationRepository::class.java)

   @Language("PostgreSQL")
   fun selectBaseQuery(): String {
      return """
         SELECT
            location.id                   AS id,
            location.number               AS number,
            location.name                 AS name,
            comp.id                       AS comp_id,
            comp.time_created             AS comp_time_created,
            comp.time_updated             AS comp_time_updated,
            comp.name                     AS comp_name,
            comp.doing_business_as        AS comp_doing_business_as,
            comp.client_code              AS comp_client_code,
            comp.client_id                AS comp_client_id,
            comp.dataset_code             AS comp_dataset_code,
            comp.federal_id_number        AS comp_federal_id_number,
            address.id                    AS address_id,
            address.name                  AS address_name,
            address.address1              AS address_address1,
            address.address2              AS address_address2,
            address.city                  AS address_city,
            address.state                 AS address_state,
            address.postal_code           AS address_postal_code,
            address.latitude              AS address_latitude,
            address.longitude             AS address_longitude,
            address.country               AS address_country,
            address.county                AS address_county,
            address.phone                 AS address_phone,
            address.fax                   AS address_fax
         FROM fastinfo_prod_import.location_vw location
              JOIN company comp ON comp.dataset_code = location.dataset AND comp.deleted = FALSE
              LEFT JOIN address ON comp.address_id = address.id AND address.deleted = FALSE
      """
   }

   @ReadOnly
   fun findOne(id: Long, company: CompanyEntity): LocationEntity? {
      val params = mutableMapOf<String, Any?>("id" to id, "comp_id" to company.id)
      val query =
         """
         ${selectBaseQuery()}
         WHERE location.id = :id
               AND comp.id = :comp_id
      """.trimMargin()

      logger.trace("{} / {}", query, params)

      val found = jdbc.findFirstOrNull(query, params) { rs, _ -> mapRow(rs) }

      logger.trace("Searching for Location with params {} resulted in {}", params, found)

      return found
   }

   @ReadOnly
   fun findOne(locationNumber: Int, company: CompanyEntity): LocationEntity? {
      val params = mutableMapOf("location_number" to locationNumber, "comp_id" to company.id)
      val query =
         """
            ${selectBaseQuery()}
            WHERE location.number = :location_number
               AND comp.id = :comp_id
         """.trimIndent()

      logger.debug("Searching for Location by number {}/{}", query, params)

      val found = jdbc.findFirstOrNull(query, params) { rs, _ -> mapRow(rs) }

      logger.trace("Search for location by number with params resulted in {}", params, found)

      return found
   }

   @ReadOnly
   fun findAll(pageRequest: PageRequest, company: CompanyEntity): RepositoryPage<LocationEntity, PageRequest> {
      val params =
         mutableMapOf("comp_id" to company.id, "limit" to pageRequest.size(), "offset" to pageRequest.offset())
      var totalElements: Long? = null
      val elements = mutableListOf<LocationEntity>()
      val pagedQuery = StringBuilder("${selectBaseQuery()} WHERE comp.id = :comp_id")

      val query =
         """
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

      logger.trace("Fetching all locations using {} / {}", query, params)

      jdbc.query(query, params) { rs, _ ->
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

   @ReadOnly
   fun exists(id: Long, company: CompanyEntity): Boolean {
      val exists = jdbc.queryForObject(
         """
            SELECT count(location.id) > 0
            FROM fastinfo_prod_import.location_vw location
               JOIN company comp ON comp.dataset_code = location.dataset AND comp.deleted = FALSE
            WHERE location.id = :location_id
         """.trimIndent(),
         mapOf("location_id" to id, "comp_id" to company.id),
         Boolean::class.java
      )

      logger.trace("Checking if Location: {} exists resulted in {}", id, exists)

      return exists
   }

   @ReadOnly
   fun exists(number: Int, company: CompanyEntity): Boolean {
      val exists = jdbc.queryForObject(
         """
            SELECT count(location.id) > 0
            FROM fastinfo_prod_import.location_vw location
               JOIN company comp ON comp.dataset_code = location.dataset AND comp.deleted = FALSE
            WHERE location.number = :location_number
               AND comp.id = :comp_id
         """.trimIndent(),
         mapOf("location_number" to number, "comp_id" to company.id),
         Boolean::class.java
      )

      logger.trace("Checking if Location: {} exists resulted in {}", number, exists)

      return exists
   }

   fun maybeMapRow(rs: ResultSet, columnPrefix: String = EMPTY) =
      if (rs.getString("${columnPrefix}id") != null) {
         mapRow(rs, columnPrefix)
      } else {
         null
      }

   private fun mapRow(rs: ResultSet, columnPrefix: String = EMPTY) =
      LocationEntity(
         id = rs.getLong("${columnPrefix}id"),
         number = rs.getInt("${columnPrefix}number"),
         name = rs.getString("${columnPrefix}name"),
      )
}
