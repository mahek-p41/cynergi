package com.cynergisuite.middleware.shipvia.infrastructure

import com.cynergisuite.domain.PageRequest
import com.cynergisuite.domain.infrastructure.RepositoryPage
import com.cynergisuite.extensions.findFirstOrNull
import com.cynergisuite.extensions.insertReturning
import com.cynergisuite.extensions.queryPaged
import com.cynergisuite.extensions.updateReturning
import com.cynergisuite.middleware.company.Company
import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.shipvia.ShipViaEntity
import io.micronaut.spring.tx.annotation.Transactional
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.sql.ResultSet
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ShipViaRepository @Inject constructor(
   private val jdbc: NamedParameterJdbcTemplate
) {
   private val logger: Logger = LoggerFactory.getLogger(ShipViaRepository::class.java)
   private fun baseSelectQuery() = """
      SELECT
         shipVia.id             AS id,
         shipVia.uu_row_id      AS uu_row_id,
         shipVia.time_created   AS time_created,
         shipVia.time_updated   AS time_updated,
         shipVia.description    AS description,
         shipVia.number         AS number,
         comp.id                AS comp_id,
         comp.uu_row_id         AS comp_uu_row_id,
         comp.name              AS comp_name,
         comp.doing_business_as AS comp_doing_business_as,
         comp.client_code       AS comp_client_code,
         comp.client_id         AS comp_client_id,
         comp.dataset_code      AS comp_dataset_code,
         comp.federal_id_number AS comp_federal_id_number,
         count(*) OVER()        AS total_elements
      FROM ship_via shipVia
           JOIN company comp ON shipVia.company_id = comp.id
   """

   fun findOne(id: Long): ShipViaEntity? {
      logger.debug("Searching for ShipVia by id {}", id)

      val found = jdbc.findFirstOrNull("${baseSelectQuery()} WHERE shipVia.id = :id", mapOf("id" to id), this::mapRow)

      logger.trace("Searching for ShipVia: {} resulted in {}", id, found)

      return found
   }

   fun findAll(pageRequest: PageRequest, company: Company): RepositoryPage<ShipViaEntity, PageRequest> {
      return jdbc.queryPaged("""
         ${baseSelectQuery()}
         WHERE comp.id = :comp_id
         ORDER BY shipVia.${pageRequest.snakeSortBy()} ${pageRequest.sortDirection()}
         LIMIT :limit OFFSET :offset
         """.trimIndent(),
         mapOf(
            "comp_id" to company.myId(),
            "limit" to pageRequest.size(),
            "offset" to pageRequest.offset()
         ),
         pageRequest
      ) { rs, elements ->
         do {
            elements.add(mapRow(rs))
         } while(rs.next())
      }
   }
   fun exists(id: Long): Boolean {
      val exists = jdbc.queryForObject("SELECT EXISTS(SELECT id FROM ship_via WHERE id = :id)", mapOf("id" to id), Boolean::class.java)!!

      logger.trace("Checking if ShipVia: {} exists resulted in {}", id, exists)

      return exists
   }

   @Transactional
   fun insert(entity: ShipViaEntity): ShipViaEntity {
      logger.debug("Inserting shipVia {}", entity)

      return jdbc.insertReturning(
         """
         INSERT INTO ship_via(description, company_id)
         VALUES (
            :description,
            :comp_id
         )
         RETURNING
            *
         """.trimIndent(),
         mapOf(
            "description" to entity.description,
            "comp_id" to entity.company.myId()
         ),
         RowMapper { rs, _ ->
            ShipViaEntity(
               id = rs.getLong("id"),
               description = rs.getString("description"),
               number = rs.getInt("number"),
               company = entity.company
            )
         }
      )
   }

   @Transactional
   fun update(entity: ShipViaEntity): ShipViaEntity {
      logger.debug("Updating shipVia {}", entity)

      return jdbc.updateReturning("""
         UPDATE ship_via
         SET
            description = :description
         WHERE id = :id
         RETURNING
            *
         """.trimIndent(),
         mapOf(
            "id" to entity.id,
            "description" to entity.description,
            "number" to entity.number
         ),
         RowMapper { rs, _ ->
            ShipViaEntity(
               id = rs.getLong("id"),
               description = rs.getString("description"),
               number = rs.getInt("number"),
               company = entity.company
            )
         }
      )
   }

   private fun mapRow(rs: ResultSet): ShipViaEntity {
      return ShipViaEntity(
         id = rs.getLong("id"),
         description = rs.getString("description"),
         number = rs.getInt("number"),
         company = CompanyEntity(
            id = rs.getLong("comp_id"),
            name = rs.getString("comp_name"),
            doingBusinessAs = rs.getString("comp_doing_business_as"),
            clientCode = rs.getString("comp_client_code"),
            clientId = rs.getInt("comp_client_id"),
            datasetCode = rs.getString("comp_dataset_code"),
            federalIdNumber = rs.getString("comp_federal_id_number")
         )
      )
   }
}

