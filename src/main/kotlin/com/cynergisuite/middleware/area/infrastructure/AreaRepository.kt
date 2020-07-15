package com.cynergisuite.middleware.area.infrastructure

import com.cynergisuite.extensions.getIntOrNull
import com.cynergisuite.extensions.insertReturning
import com.cynergisuite.middleware.area.AreaEntity
import com.cynergisuite.middleware.area.AreaType
import com.cynergisuite.middleware.area.MenuType
import com.cynergisuite.middleware.area.ModuleType
import com.cynergisuite.middleware.company.Company
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
) {
   private val logger: Logger = LoggerFactory.getLogger(AreaRepository::class.java)

   fun findAll(company: Company): List<AreaType> {
      logger.trace("Loading all areas for company {}", company)

      val areas = mutableListOf<AreaType>()
      var currentArea: AreaType? = null
      var currentMenu: MenuType? = null

      jdbc.query("""
         SELECT
            areas.id AS area_id,
            areas.value AS area_value,
            areas.description AS area_description,
            areas.localization_code AS area_localization_code,
            (areas_configs.id IS NOT NULL) AS area_enabled,
            menus.id AS menu_id,
            menus.value AS menu_value,
            menus.description AS menu_description,
            menus.localization_code AS menu_localization_code,
            modules.id AS module_id,
            modules.value AS module_value,
            modules.description AS module_description,
            modules.localization_code AS module_localization_code,
            module_configs.id AS module_configs_id,
            module_configs.level AS module_configs_level
         FROM area_type_domain areas
            LEFT OUTER JOIN menu_type_domain menus ON areas.id = menus.area_type_id
            LEFT OUTER JOIN module_type_domain modules ON menus.id = modules.menu_type_id
            LEFT OUTER JOIN area areas_configs ON areas.id = areas_configs.area_type_id AND areas_configs.company_id = :comp_id
            LEFT OUTER JOIN module module_configs ON modules.id = module_configs.module_type_id
         ORDER BY areas.id, menus.id, modules.id
      """.trimIndent(), mapOf("comp_id" to company.myId())) { rs, _ ->
         do {
            val tempArea = if (currentArea?.id != rs.getLong("area_id")) {
               val localArea = mapArea(rs)
               areas.add(localArea)
               currentArea = localArea

               localArea
            } else {
               currentArea!!
            }

            val tempMenu = if (currentMenu?.id != rs.getLong("menu_id")) {
               val localMenu = mapMenu(rs)
               tempArea.menus.add(localMenu)
               currentMenu = localMenu

               localMenu
            } else {
               currentMenu!!
            }

            mapModule(rs)?.also { tempMenu.modules.add(it) }
         } while (rs.next())
      }

      return areas
   }

   @Transactional
   fun enable(company: Company, areaTypeId: Long) {
      logger.debug("Enable area {} for company {}", areaTypeId, company.myDataset())

      jdbc.update("""
         INSERT INTO area(company_id, area_type_id)
         VALUES (:company_id, :area_type_id)
         """,
         mapOf(
            "company_id" to company.myId(),
            "area_type_id" to areaTypeId
         )
      )
   }

   @Transactional
   fun disable(company: Company, areaTypeId: Long) {
      logger.debug("Disable area {} for company {}", areaTypeId, company.myDataset())

      jdbc.update("""
         DELETE FROM area
         WHERE company_id = :company_id AND area_type_id = :area_type_id
         """,
         mapOf("company_id" to company.myId(), "area_type_id" to areaTypeId)
      )
   }

   @Transactional
   fun insert(company: Company, areaEntity: AreaEntity): AreaEntity {
      logger.debug("Inserting area {}", areaEntity)

      return jdbc.insertReturning("""
         INSERT INTO area(company_id, area_type_id)
         VALUES (:company_id, :area_type_id)
         RETURNING
            *
         """,
         mapOf(
            "company_id" to company.myId(),
            "area_type_id" to areaEntity.areaType.myId()
         ),
         RowMapper { rs, _ -> mapEntity(rs, company, areaEntity.areaType) }
      )
   }

   fun exists(areaTypeId: Long): Boolean {
      val exists = jdbc.queryForObject("""
         SELECT EXISTS (SELECT id FROM area_type_domain WHERE id = :area_type_id)
         """,
         mapOf("area_type_id" to areaTypeId), Boolean::class.java)!!

      logger.trace("Checking if Area exists {}")

      return exists
   }

   private fun mapEntity(rs: ResultSet, company: Company, areaType: AreaType, columnPrefix: String = EMPTY): AreaEntity =
      AreaEntity(
         id = rs.getLong("${columnPrefix}id"),
         company = company,
         areaType = areaType
      )

   private fun mapArea(rs: ResultSet): AreaType {
      return AreaType(
         id = rs.getLong("area_id"),
         value = rs.getString("area_value"),
         description = rs.getString("area_description"),
         localizationCode = rs.getString("area_localization_code"),
         enabled = rs.getBoolean("area_enabled"),
         menus = mutableListOf()
      )
   }

   private fun mapMenu(rs: ResultSet): MenuType {
      return MenuType(
         id = rs.getLong("menu_id"),
         value = rs.getString("menu_value"),
         description = rs.getString("menu_description"),
         localizationCode = rs.getString("menu_localization_code"),
         modules = mutableListOf()
      )
   }

   private fun mapModule(rs: ResultSet): ModuleType? {
      return if (rs.getString("module_id") != null) {
         ModuleType(
            id = rs.getLong("module_id"),
            value = rs.getString("module_value"),
            description = rs.getString("module_description"),
            localizationCode = rs.getString("module_localization_code"),
            level = rs.getIntOrNull("module_configs_level") ?: 100
         )
      } else {
         null
      }
   }
}
