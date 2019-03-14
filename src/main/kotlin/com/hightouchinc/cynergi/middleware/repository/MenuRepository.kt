package com.hightouchinc.cynergi.middleware.repository

import com.hightouchinc.cynergi.middleware.entity.Company
import com.hightouchinc.cynergi.middleware.entity.Menu
import com.hightouchinc.cynergi.middleware.entity.MenuTree
import com.hightouchinc.cynergi.middleware.extensions.findFirstOrNull
import com.hightouchinc.cynergi.middleware.extensions.getOffsetDateTime
import com.hightouchinc.cynergi.middleware.extensions.getUuid
import org.apache.commons.lang3.StringUtils.EMPTY
import org.intellij.lang.annotations.Language
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.sql.ResultSet
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MenuRepository @Inject constructor(
   private val moduleRepository: ModuleRepository,
   private val jdbc: NamedParameterJdbcTemplate
) : TypeDomainRepository<Menu> {
   private val logger: Logger = LoggerFactory.getLogger(MenuRepository::class.java)
   private val simpleMenuRowMapper = MenuRowMapper()
   private val menuTreeRowMapper = MenuTreeRowMapper()
   @Language("PostgreSQL") private val menuLevelQuery = """
       SELECT
          a.id AS a_id,
          a.uu_row_id AS a_uu_row_id,
          a.time_created AS a_time_created,
          a.time_updated AS a_time_updated,
          a.level AS a_level,
          m.id AS m_id,
          m.uu_row_id AS m_uu_row_id,
          m.time_created AS m_time_created,
          m.time_updated AS m_time_updated,
          m.name AS m_name,
          m.literal AS m_literal,
          mod.id AS mod_id,
          mod.uu_row_id AS mod_uu_row_id,
          mod.time_created AS mod_time_created,
          mod.time_updated AS mod_time_updated,
          mod.name AS mod_name,
          mod.literal AS mod_literal,
          mod.menu_id AS mod_menu_id,
          cma.id AS cma_id,
          cma.uu_row_id AS cma_uu_row_id,
          cma.time_created AS cma_time_created,
          cma.time_updated AS cma_time_updated,
          cma.level AS cma_level
       FROM area a
          JOIN menu m
            ON a.menu_id = m.id
          JOIN module mod
             ON m.id = mod.menu_id
          JOIN company_module_access cma
             ON mod.id = cma.module_id
       WHERE :level >= a.level
          AND :level >= cma.level
          AND a.company_id = :company_id
       ORDER BY a_id, m_id, mod_id, cma_id
   """.trimIndent()

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

   fun findAll(level: Int, company: Company): Set<MenuTree> {
      val menus: MutableSet<MenuTree> = mutableSetOf()
      var currentMenu: MenuTree? = null

      jdbc.query(menuLevelQuery, mutableMapOf("level" to level, "company_id" to company.id)) { rs ->
         val rowMenuId = rs.getLong("m_id")
         val menuTree = if (currentMenu?.id != rowMenuId) {
            val mappedMenuTree = menuTreeRowMapper.mapRow(rs, 0)

            menus.add(mappedMenuTree)

            currentMenu = mappedMenuTree

            mappedMenuTree
         } else {
            currentMenu!!
         }

         moduleRepository.mapRow(rs = rs)?.also { menuTree.modules.add(it) }
      }

      return menus
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

private class MenuTreeRowMapper: RowMapper<MenuTree> {
   override fun mapRow(rs: ResultSet, rowNum: Int): MenuTree =
      MenuTree(
         id = rs.getLong("m_id"),
         uuRowId = rs.getUuid("m_uu_row_id"),
         timeCreated = rs.getOffsetDateTime("m_time_created"),
         timeUpdated = rs.getOffsetDateTime("m_time_updated"),
         name = rs.getString("m_name"),
         literal = rs.getString("m_literal"),
         modules = mutableSetOf()
      )
}
