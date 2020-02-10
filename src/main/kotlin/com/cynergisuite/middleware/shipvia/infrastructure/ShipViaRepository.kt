package com.cynergisuite.middleware.shipvia.infrastructure

import com.cynergisuite.domain.PageRequest
import com.cynergisuite.domain.infrastructure.RepositoryPage
import com.cynergisuite.extensions.*
import com.cynergisuite.middleware.shipvia.ShipViaEntity
import io.micronaut.spring.tx.annotation.Transactional
import org.apache.commons.lang3.StringUtils.EMPTY
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
   private val simpleShipViaRowMapper = ShipViaRowMapper()

   fun findOne(id: Long): ShipViaEntity? {
      logger.debug("Searching for ShipVia by id {}", id)
      val found = jdbc.findFirstOrNull("""SELECT sv.id, sv.uu_row_id, sv.time_created, sv.time_updated, sv.description, c.dataset_code as dataset 
         FROM ship_via sv
         JOIN company c on sv.company_id = c.id 
         WHERE sv.id = :id""", mapOf("id" to id), simpleShipViaRowMapper)

      logger.trace("Searching for ShipVia: {} resulted in {}", id, found)

      return found
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
            (
               SELECT id
               FROM company c
               WHERE c.dataset_code = :dataset
            )
         )
         RETURNING
            *
         """.trimIndent(),
         mapOf(
            "description" to entity.description,
            "dataset" to entity.dataset
         ),
         RowMapper { rs, _ ->
            ShipViaEntity(
               id = rs.getLong("id"),
               uuRowId = rs.getUuid("uu_row_id"),
               timeCreated = rs.getOffsetDateTime("time_created"),
               timeUpdated = rs.getOffsetDateTime("time_updated"),
               description = rs.getString("description"),
               dataset = entity.dataset
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
            "description" to entity.description
         ),
         RowMapper { rs, _ ->
            ShipViaEntity(
               id = rs.getLong("id"),
               uuRowId = rs.getUuid("uu_row_id"),
               timeCreated = rs.getOffsetDateTime("time_created"),
               timeUpdated = rs.getOffsetDateTime("time_updated"),
               description = rs.getString("description"),
               dataset = entity.dataset
            )
         }
      )
   }

   fun findAll(pageRequest: PageRequest, dataset: String): RepositoryPage<ShipViaEntity, PageRequest> {
      var totalElements: Long? = null
      val shipVia = mutableListOf<ShipViaEntity>()

      jdbc.query("""
         SELECT
            sv.id,
            sv.uu_row_id,
            sv.time_created,
            sv.time_updated,
            sv.description,
            c.dataset_code as dataset,
            count(*) OVER() as total_elements
         FROM ship_via AS sv
         JOIN company c
         ON sv.company_id = c.id
         WHERE c.dataset_code = :dataset
         ORDER BY ${pageRequest.sortBy()} ${pageRequest.sortDirection()}
         LIMIT ${pageRequest.size()}
         OFFSET ${pageRequest.offset()}
         """.trimIndent(),
         mapOf(
            "dataset" to dataset
         )
      ) { rs ->
         if (totalElements == null) {
            totalElements = rs.getLong("total_elements")
         }

         shipVia.add(simpleShipViaRowMapper.mapRow(rs,0))
      }

      return RepositoryPage(
         requested = pageRequest,
         elements = shipVia,
         totalElements = totalElements ?: 0
      )

   }
}

private class ShipViaRowMapper(
   private val columnPrefix: String = EMPTY
) : RowMapper<ShipViaEntity> {
   override fun mapRow(rs: ResultSet, rowNum: Int): ShipViaEntity =
      ShipViaEntity(
         id = rs.getLong("${columnPrefix}id"),
         uuRowId = rs.getUuid("${columnPrefix}uu_row_id"),
         timeCreated = rs.getOffsetDateTime("${columnPrefix}time_created"),
         timeUpdated = rs.getOffsetDateTime("${columnPrefix}time_updated"),
         description = rs.getString("${columnPrefix}description"),
         dataset = rs.getString("${columnPrefix}dataset")
      )
}
