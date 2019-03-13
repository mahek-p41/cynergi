package com.hightouchinc.cynergi.middleware.repository

import com.hightouchinc.cynergi.middleware.entity.Menu
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
class MenuRepository @Inject constructor(
   private val jdbc: NamedParameterJdbcTemplate
) : TypeDomainRepository<Menu> {
   private val logger: Logger = LoggerFactory.getLogger(MenuRepository::class.java)
   private val simpleMenuRowMapper = MenuRowMapper()

   override fun findOne(id: Long): Menu? {
      val found = jdbc.findFirstOrNull("SELECT * FROM menu WHERE id = :id", mapOf("id" to id), simpleMenuRowMapper)

      logger.trace("Searching for Menu: {} resulted in {}", id, found)

      return found
   }

   override fun findOne(value: String): Menu? {
      val found = jdbc.findFirstOrNull("SELECT * FROM menu WHERE name = :name", mapOf("name" to value), simpleMenuRowMapper)

      logger.trace("Search for Module: {} resulted in {}", value, found)

      return found
   }

   override fun findAll(): List<Menu> =
      jdbc.query("SELECT * FROM menu", emptyMap<String, Any>(), simpleMenuRowMapper)

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
