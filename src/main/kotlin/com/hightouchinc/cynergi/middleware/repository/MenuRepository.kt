package com.hightouchinc.cynergi.middleware.repository

import com.hightouchinc.cynergi.middleware.entity.Menu
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
class MenuRepository @Inject constructor(
   private val jdbc: NamedParameterJdbcTemplate
) : Repository<Menu> {
   private val logger: Logger = LoggerFactory.getLogger(MenuRepository::class.java)
   private val simpleMenuRowMapper = MenuRowMapper()

   override fun findOne(id: Long): Menu? {
      val found = jdbc.findFirstOrNull("SELECT * FROM menu WHERE id = :id", mapOf("id" to id), simpleMenuRowMapper)

      logger.trace("Searching for Menu: {} resulted in {}", id, found)

      return found
   }

   override fun exists(id: Long): Boolean {
      val exists = jdbc.queryForObject("SELECT EXISTS(SELECT id FROM menu WHERE id = :id)", mapOf("id" to id), Boolean::class.java)!!

      logger.trace("Checking if Menu: {} exists resulted in {}", id, exists)

      return exists
   }

   override fun insert(entity: Menu): Menu {
      logger.debug("Inserting menu {}", entity)

      return jdbc.insertReturning("""
         INSERT INTO menu(name, literal)
         VALUES (:name, :literal)
         RETURNING
            *
         """.trimIndent(),
         mapOf(
            "name" to entity.name,
            "literal" to entity.literal
         ),
         simpleMenuRowMapper
      )
   }

   override fun update(entity: Menu): Menu {
      logger.debug("Updating menu {}", entity)

      return jdbc.updateReturning("""
         UPDATE menu
         SET
            name = :name,
            literal = :literal
         WHERE id = :id
         RETURNING
            *
         """.trimIndent(),
         mapOf(
            "id" to entity.id!!,
            "name" to entity.name,
            "literal" to entity.literal
         ),
         simpleMenuRowMapper
      )
   }
}

private class MenuRowMapper(
   private val columnPrefix: String = EMPTY
) : RowMapper<Menu> {
   override fun mapRow(rs: ResultSet, rowNum: Int): Menu =
      Menu(
         id = rs.getLong("${columnPrefix}id"),
         uuRowId = rs.getUuid("${columnPrefix}uu_row_id"),
         timeCreated = rs.getOffsetDateTime("${columnPrefix}time_created"),
         timeUpdated = rs.getOffsetDateTime("${columnPrefix}time_updated"),
         name = rs.getString("${columnPrefix}name"),
         literal = rs.getString("${columnPrefix}literal")
      )
}
