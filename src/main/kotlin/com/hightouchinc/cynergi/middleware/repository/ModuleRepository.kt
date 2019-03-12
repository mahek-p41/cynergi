package com.hightouchinc.cynergi.middleware.repository

import com.hightouchinc.cynergi.middleware.entity.Module
import com.hightouchinc.cynergi.middleware.entity.helper.SimpleIdentifiableEntity
import com.hightouchinc.cynergi.middleware.extensions.findFirstOrNull
import com.hightouchinc.cynergi.middleware.extensions.getOffsetDateTime
import com.hightouchinc.cynergi.middleware.extensions.getUuid
import com.hightouchinc.cynergi.middleware.extensions.insertReturning
import com.hightouchinc.cynergi.middleware.extensions.updateReturning
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
class ModuleRepository @Inject constructor(
   private val jdbc: NamedParameterJdbcTemplate
) : Repository<Module> {
   private val logger: Logger = LoggerFactory.getLogger(ModuleRepository::class.java)
   private val simpleModuleRowMapper = ModuleRowMapper()

   override fun findOne(id: Long): Module? {
      val found = jdbc.findFirstOrNull("SELECT * FROM module WHERE id = :id", mapOf("id" to id), simpleModuleRowMapper)

      logger.trace("Searching for Module: {} resulted in {}", id, found)

      return found
   }

   override fun exists(id: Long): Boolean {
      val exists = jdbc.queryForObject("SELECT EXISTS(SELECT id FROM module WHERE id = :id)", mapOf("id" to id), Boolean::class.java)!!

      logger.trace("Checking if Module: {} exists resulted in {}", id, exists)

      return exists
   }

   @Transactional
   override fun insert(entity: Module): Module {
      logger.debug("Inserting module {}", entity)

      return jdbc.insertReturning("""
         INSERT INTO module(name, literal, menu_id)
         VALUES (:name, :literal, :menu_id)
         RETURNING
            *
         """.trimIndent(),
         mapOf(
            "name" to entity.name,
            "literal" to entity.literal,
            "menu_id" to entity.menu.entityId()
         ),
         simpleModuleRowMapper
      )
   }

   @Transactional
   override fun update(entity: Module): Module {
      logger.debug("Updating module {}", entity)

      return jdbc.updateReturning("""
         UPDATE module
         SET
            name = :name,
            literal = :literal,
            menu_id = :menu_id
         WHERE id = :id
         RETURNING
            *
         """.trimIndent(),
         mapOf(
            "id" to entity.id!!,
            "name" to entity.name,
            "literal" to entity.literal,
            "menu_id" to entity.menu.entityId()
         ),
         simpleModuleRowMapper
      )
   }
}

private class ModuleRowMapper(
   private val columnPrefix: String = EMPTY
) : RowMapper<Module> {
   override fun mapRow(rs: ResultSet, rowNum: Int): Module =
      Module(
         id = rs.getLong("${columnPrefix}id"),
         uuRowId = rs.getUuid("${columnPrefix}uu_row_id"),
         timeCreated = rs.getOffsetDateTime("${columnPrefix}time_created"),
         timeUpdated = rs.getOffsetDateTime("${columnPrefix}time_updated"),
         name = rs.getString("${columnPrefix}name"),
         literal = rs.getString("${columnPrefix}literal"),
         menu = SimpleIdentifiableEntity(rs.getLong("${columnPrefix}menu_id"))
      )
}
