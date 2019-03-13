package com.hightouchinc.cynergi.middleware.repository

import com.hightouchinc.cynergi.middleware.entity.Area
import com.hightouchinc.cynergi.middleware.entity.Company
import com.hightouchinc.cynergi.middleware.entity.Menu
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
class AreaRepository @Inject constructor(
   private val jdbc: NamedParameterJdbcTemplate
) : Repository<Area> {
   private val logger: Logger = LoggerFactory.getLogger(AreaRepository::class.java)
   private val simpleAreaRowMapper = AreaRowMapper()
   private val areaLevelQuery = """
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
   """.trimIndent()

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

   @Transactional
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

   @Transactional
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

   fun findAreasByLevelAndCompany(level: Int, company: Company): List<Area> {
      val areas = mutableListOf<Area>()



      return areas
   }

   fun associate(menu: Menu, company: Company, level: Int): Area {
      return insert(
         Area(
            company = company,
            menu = menu,
            level = level
         )
      )
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
