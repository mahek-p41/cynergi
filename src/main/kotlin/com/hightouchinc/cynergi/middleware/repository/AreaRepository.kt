package com.hightouchinc.cynergi.middleware.repository

import com.hightouchinc.cynergi.middleware.entity.Area
import com.hightouchinc.cynergi.middleware.entity.helper.SimpleIdentifiableEntity
import com.hightouchinc.cynergi.middleware.extensions.findFirstOrNull
import com.hightouchinc.cynergi.middleware.extensions.getOffsetDateTime
import com.hightouchinc.cynergi.middleware.extensions.getUuid
import com.hightouchinc.cynergi.middleware.extensions.insertReturning
import com.hightouchinc.cynergi.middleware.extensions.updateReturning
import org.apache.commons.lang3.StringUtils.EMPTY
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.sql.ResultSet
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AreaRepository @Inject constructor(
   private val jdbc: NamedParameterJdbcTemplate
) : Repository<Area> {
   private val logger: Logger = LoggerFactory.getLogger(AreaRepository::class.java)
   private val simpleAreaRowMapper = AreaRowMapper()

   override fun findOne(id: Long): Area? {
      val found = jdbc.findFirstOrNull("SELECT * FROM area WHERE id = :id", mapOf("id" to id), simpleAreaRowMapper)

      logger.trace("Searching for Area: {} resulted in {}", id, found)

      return found
   }

   override fun exists(id: Long): Boolean {
      val exists = jdbc.queryForObject("SELECT EXISTS(SELECT id FROM area WHERE id = :id)", mapOf("id" to id), Boolean::class.java)!!

      logger.trace("Checking if Area: {} exists resulted in {}", id, exists)

      return exists
   }

   override fun insert(entity: Area): Area {
      logger.debug("Inserting area {}", entity)

      return jdbc.insertReturning("""
         INSERT INTO area(company_id, menu_id, level)
         VALUES (:company_id, :menu_id, :level)
         RETURNING
            *
         """.trimIndent(),
         mapOf(
            "company_id" to entity.company.entityId(),
            "menu_id" to entity.menu.entityId(),
            "level" to entity.level
         ),
         simpleAreaRowMapper
      )
   }

   override fun update(entity: Area): Area {
      logger.debug("Updating area {}", entity)

      return jdbc.updateReturning("""
         UPDATE area
         SET
            company_id = :company_id,
            menu_id = :menu_id,
            level = :level
         WHERE id = :id
         RETURNING
            *
         """.trimIndent(),
         mapOf(
            "id" to entity.id!!,
            "company_id" to entity.company.entityId(),
            "menu_id" to entity.menu.entityId(),
            "level" to entity.level
         ),
         simpleAreaRowMapper
      )
   }

   fun findAreasByLevel(level: Int): List<Area> {
      TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
   }
}

private class AreaRowMapper(
   private val columnPrefix: String = EMPTY
) : RowMapper<Area> {
   override fun mapRow(rs: ResultSet, rowNum: Int): Area =
      Area(
         id = rs.getLong("${columnPrefix}id"),
         uuRowId = rs.getUuid("${columnPrefix}uu_row_id"),
         timeCreated = rs.getOffsetDateTime("${columnPrefix}time_created"),
         timeUpdated = rs.getOffsetDateTime("${columnPrefix}time_updated"),
         company = SimpleIdentifiableEntity(rs.getLong("${columnPrefix}company_id")),
         menu = SimpleIdentifiableEntity(rs.getLong("${columnPrefix}menu_id")),
         level = rs.getInt("${columnPrefix}level")
      )
}
