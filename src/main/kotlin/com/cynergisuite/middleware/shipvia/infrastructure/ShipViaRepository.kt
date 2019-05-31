package com.cynergisuite.middleware.shipvia.infrastructure

import com.cynergisuite.domain.infrastructure.Repository
import com.cynergisuite.extensions.*
import com.cynergisuite.middleware.shipvia.ShipVia
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
) : Repository<ShipVia> {
   private val logger: Logger = LoggerFactory.getLogger(ShipViaRepository::class.java)
   private val simpleShipViaRowMapper = ShipViaRowMapper()

   override fun findOne(id: Long): ShipVia? {
      val found = jdbc.findFirstOrNull("SELECT * FROM ship_via WHERE id = :id", mapOf("id" to id), simpleShipViaRowMapper)
      logger.trace("Searching for ShipVia: {} resulted in {}", id, found)
      return found
   }

   override fun exists(id: Long): Boolean {
      val exists = jdbc.queryForObject("SELECT EXISTS(SELECT id FROM ship_via WHERE id = :id)", mapOf("id" to id), Boolean::class.java)!!
      logger.trace("Checking if ShipVia: {} exists resulted in {}", id, exists)
      return exists
   }

   @Transactional
   override fun insert(entity: ShipVia): ShipVia {
      logger.debug("Inserting shipVia {}", entity)

      return jdbc.insertReturning("""
         INSERT INTO ship_via(name, description)
         VALUES (:name, :description)
         RETURNING
            *
         """.trimIndent(),
         mapOf(
            "name" to entity.name,
            "description" to entity.description
         ),
         simpleShipViaRowMapper
      )
   }

   @Transactional
   override fun update(entity: ShipVia): ShipVia {
      logger.debug("Updating shipVia {}", entity)

      return jdbc.updateReturning("""
         UPDATE ship_via
         SET
            name = :name,
            description = :description
         WHERE id = :id
         RETURNING
            *
         """.trimIndent(),
         mapOf(
            "id" to entity.id,
            "name" to entity.name,
            "description" to entity.description
         ),
         simpleShipViaRowMapper
      )
   }
}

private class ShipViaRowMapper(
   private val columnPrefix: String = EMPTY
) : RowMapper<ShipVia> {
   override fun mapRow(rs: ResultSet, rowNum: Int): ShipVia =
      ShipVia(
         id = rs.getLong("${columnPrefix}id"),
         uuRowId = rs.getUuid("${columnPrefix}uu_row_id"),
         timeCreated = rs.getOffsetDateTime("${columnPrefix}time_created"),
         timeUpdated = rs.getOffsetDateTime("${columnPrefix}time_updated"),
         name = rs.getString("${columnPrefix}name"),
         description = rs.getString("${columnPrefix}description")
      )
}
