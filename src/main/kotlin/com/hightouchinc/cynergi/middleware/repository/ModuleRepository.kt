package com.hightouchinc.cynergi.middleware.repository

import com.hightouchinc.cynergi.middleware.entity.Menu
import com.hightouchinc.cynergi.middleware.entity.Module
import com.hightouchinc.cynergi.middleware.entity.helper.SimpleIdentifiableEntity
import com.hightouchinc.cynergi.middleware.extensions.findFirstOrNull
import com.hightouchinc.cynergi.middleware.extensions.getOffsetDateTime
import com.hightouchinc.cynergi.middleware.extensions.getUuid
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
) : TypeDomainRepository<Module> {
   private val logger: Logger = LoggerFactory.getLogger(ModuleRepository::class.java)
   private val simpleModuleRowMapper = ModuleRowMapper()
   private val prefixedModuleRowMapper = ModuleRowMapper("mod_")

   override fun findOne(id: Long): Module? {
      val found = jdbc.findFirstOrNull("SELECT * FROM module WHERE id = :id", mapOf("id" to id), simpleModuleRowMapper)

      logger.trace("Searching for Module: {} resulted in {}", id, found)

      return found
   }

   override fun findOne(value: String): Module? {
      val found = jdbc.findFirstOrNull("SELECT * FROM module WHERE name = :name", mapOf("name" to value), simpleModuleRowMapper)

      logger.trace("Search for Module: {} resulted in {}", value, found)

      return found
   }

   override fun findAll(): List<Module> =
      jdbc.query("SELECT * FROM module", emptyMap<String, Any>(), simpleModuleRowMapper)

   fun findAllAssociatedWithMenu(menu: Menu): List<Module> =
      jdbc.query("SELECT * FROM module WHERE menu_id = :menu_id", mapOf("menu_id" to menu.id), simpleModuleRowMapper)

   fun mapRow(rs: ResultSet, row: Int = 0): Module? =
      rs.getString("mod_id")?.let { prefixedModuleRowMapper.mapRow(rs, row) }
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
