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
         INSERT INTO area()
         VALUES ()
         RETURNING
            *
         """.trimIndent(),
         mapOf<String, Any>(),
         simpleAreaRowMapper
      )
   }

   override fun update(entity: Area): Area {
      logger.debug("Updating area {}", entity)

      return jdbc.updateReturning("""
         UPDATE area
         SET

         WHERE id = :id
         RETURNING
            *
         """.trimIndent(),
         mapOf(
            "id" to entity.id!!
         ),
         simpleAreaRowMapper
      )
   }
}

private class AreaRowMapper(
   private val rowPrefix: String = EMPTY
) : RowMapper<Area> {
   override fun mapRow(rs: ResultSet, rowNum: Int): Area =
      Area(
         id = rs.getLong("${rowPrefix}id"),
         uuRowId = rs.getUuid("${rowPrefix}uu_row_id"),
         timeCreated = rs.getOffsetDateTime("${rowPrefix}time_created"),
         timeUpdated = rs.getOffsetDateTime("${rowPrefix}time_updated"),
         menu = SimpleIdentifiableEntity(rs.getLong("${rowPrefix}menu_id")),
         level = rs.getInt("${rowPrefix}level")
      )
}
