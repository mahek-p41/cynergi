package com.cynergisuite.middleware.area.infrastructure

import com.cynergisuite.extensions.getIntOrNull
import com.cynergisuite.extensions.insertReturning
import com.cynergisuite.extensions.query
import com.cynergisuite.extensions.queryForObject
import com.cynergisuite.extensions.update
import com.cynergisuite.middleware.area.AreaEntity
import com.cynergisuite.middleware.area.AreaType
import com.cynergisuite.middleware.area.AreaTypeEntity
import com.cynergisuite.middleware.area.MenuType
import com.cynergisuite.middleware.area.ModuleType
import com.cynergisuite.middleware.company.CompanyEntity
import io.micronaut.data.annotation.Join
import io.micronaut.data.annotation.Query
import io.micronaut.data.annotation.repeatable.JoinSpecifications
import io.micronaut.data.jdbc.annotation.JdbcRepository
import io.micronaut.data.repository.CrudRepository
import io.micronaut.transaction.annotation.ReadOnly
import jakarta.inject.Inject
import org.jdbi.v3.core.Jdbi
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.sql.ResultSet
import java.util.UUID
import javax.transaction.Transactional

@JdbcRepository
abstract class AreaRepository @Inject constructor(
   private val jdbc: Jdbi
) : CrudRepository<AreaEntity, UUID> {
   private val logger: Logger = LoggerFactory.getLogger(AreaRepository::class.java)

   @JoinSpecifications(
      Join("areaType")
   )
   abstract fun existsByCompanyAndAreaType(company: CompanyEntity, areaType: AreaTypeEntity): Boolean

   @JoinSpecifications(
      Join("areaType"),
      Join("company")
   )
   abstract fun findByCompanyAndAreaType(company: CompanyEntity, areaType: AreaTypeEntity): AreaEntity?

   /*@Query("""
      SELECT
         area.id                     AS id,
         areaType.id                 AS area_type_id,
         areaType.value              AS area_type_value,
         areaType.description        AS area_type_description,
         areaType.localization_code  AS area_type_localization_code,
         comp.id                     AS company_id,
         comp.name                   AS company_name,
         comp.doing_business_as      AS company_doing_business_as,
         comp.client_code            AS company_client_code,
         comp.client_id              AS company_client_id,
         comp.dataset_code           AS company_dataset_code,
         comp.federal_id_number      AS company_federal_id_number,
         comp.deleted                AS company_deleted,
         comp.address_id             AS company_address_id,
         compAddress.id              AS comp_address_id,
         compAddress.name            AS comp_address_name,
         compAddress.address1        AS comp_address_address1,
         compAddress.address2        AS comp_address_address2,
         compAddress.city            AS comp_address_city,
         compAddress.state           AS comp_address_state,
         compAddress.postal_code     AS comp_address_postal_code,
         compAddress.latitude        AS comp_address_latitude,
         compAddress.longitude       AS comp_address_longitude,
         compAddress.country         AS comp_address_country,
         compAddress.county          AS comp_address_county,
         compAddress.phone           AS comp_address_phone,
         compAddress.fax             AS comp_address_fax,
         menuTypes.id                AS area_type_menus_id,
         menuTypes.value             AS area_type_menus_value,
         menuTypes.description       AS area_type_menus_description,
         menuTypes.localization_code AS area_type_menus_localization_code,
         menuTypes.parent_id         AS area_type_menus_parent_id,
         menuTypes.order_number      AS area_type_menus_order_number
      FROM area_type_domain areaType
           LEFT OUTER JOIN area area ON areaType.id = area.area_type_id
           LEFT OUTER JOIN menu_type_domain menuTypes ON areaType.id = menuTypes.area_type_id
           LEFT OUTER JOIN module_type_domain moduleTypes ON menuTypes.id = moduleTypes.menu_type_id
           LEFT OUTER JOIN company comp ON area.company_id = comp.id
           LEFT OUTER JOIN address compAddress ON comp.address_id = compAddress.id
           LEFT OUTER JOIN module module ON moduleTypes.id = module.module_type_id
      ORDER BY areaType.id, menuTypes.id, menuTypes.parent_id, moduleTypes.id
   """, nativeQuery = true)
   @JoinSpecifications(
      Join("company"),
      Join("areaType"),
      Join("areaType.menus"),
      Join("areaType.menus.modules"),
   )
   abstract fun findByCompany(company: CompanyEntity): List<AreaEntity>*/

   @ReadOnly
   fun findAll(company: CompanyEntity): List<AreaEntity> {
      logger.trace("Loading all areas for company {}", company)

      val areas = mutableListOf<AreaEntity>()
      var currentArea: AreaType? = null
      var currentMenu: MenuType? = null

      jdbc.query(
         """
         SELECT
            areas.id AS area_id,
            areas.value AS area_value,
            areas.description AS area_description,
            areas.localization_code AS area_localization_code,
            (areas_configs.area_type_id IS NOT NULL) AS area_enabled,
            menus.id AS menu_id,
            menus.parent_id AS menu_parent_id,
            menus.value AS menu_value,
            menus.description AS menu_description,
            menus.localization_code AS menu_localization_code,
            menus.order_number AS menu_order_number,
            modules.id AS module_id,
            modules.value AS module_value,
            modules.description AS module_description,
            modules.localization_code AS module_localization_code,
            module_configs.level AS module_configs_level
         FROM area_type_domain areas
            LEFT OUTER JOIN menu_type_domain menus ON areas.id = menus.area_type_id
            LEFT OUTER JOIN module_type_domain modules ON menus.id = modules.menu_type_id
            LEFT OUTER JOIN area areas_configs ON areas.id = areas_configs.area_type_id AND areas_configs.company_id = :comp_id
            LEFT OUTER JOIN module module_configs ON modules.id = module_configs.module_type_id AND module_configs.company_id = :comp_id
         ORDER BY areas.id, menus.id, menus.parent_id, modules.id
         """.trimIndent(),
         mapOf("comp_id" to company.id)
      ) { rs, _ ->
         do {
            val tempArea = if (currentArea?.id != rs.getInt("area_id")) {
               val localArea = mapArea(rs)
               areas.add(localArea)
               currentArea = localArea

               localArea
            } else {
               currentArea!!
            }

            val tempMenu = if (rs.getString("menu_id") == null) {
               null
            } else if (currentMenu?.id != rs.getInt("menu_id")) {
               val localMenu = mapMenu(rs)

               tempArea.menus.add(localMenu)
               currentMenu = localMenu

               localMenu
            } else {
               currentMenu!!
            }

            mapModule(rs)?.also { tempMenu?.modules?.add(it) }
         } while (rs.next())
      }

      return groupingMenus(areas)
   }

   @Transactional
   fun enable(company: CompanyEntity, areaTypeId: Int) {
      logger.debug("Enable area {} for company {}", areaTypeId, company.datasetCode)

      jdbc.update(
         """
         INSERT INTO area(company_id, area_type_id)
         VALUES (:company_id, :area_type_id)
         """,
         mapOf(
            "company_id" to company.id,
            "area_type_id" to areaTypeId
         )
      )
   }

   abstract fun deleteByCompanyAndAreaType(company: CompanyEntity, areaType: AreaTypeEntity)

   /*@Transactional
   fun disable(company: CompanyEntity, areaTypeId: Int) {
      logger.debug("Disable area {} for company {}", areaTypeId, company.datasetCode)

      jdbc.update(
         """
         DELETE FROM area
         WHERE company_id = :company_id AND area_type_id = :area_type_id
         """,
         mapOf("company_id" to company.id, "area_type_id" to areaTypeId)
      )
   }*/

   @Transactional
   fun insert(company: CompanyEntity, areaType: AreaType): AreaType {
      logger.debug("Inserting area {}", areaType)

      return jdbc.insertReturning(
         """
         INSERT INTO area(company_id, area_type_id)
         VALUES (:company_id, :area_type_id)
         RETURNING
            *
         """,
         mapOf(
            "company_id" to company.id,
            "area_type_id" to areaType.myId()
         )
      ) { _, _ -> areaType }
   }

   @ReadOnly
   fun exists(areaTypeId: Int): Boolean {
      val exists = jdbc.queryForObject(
         """
         SELECT EXISTS (SELECT id FROM area_type_domain WHERE id = :area_type_id)
         """,
         mapOf("area_type_id" to areaTypeId),
         Boolean::class.java
      )

      logger.trace("Checking if Area exists {}")

      return exists
   }

   private fun groupingMenus(areas: List<AreaEntity>): List<AreaEntity> {
      return areas.map { area ->
         val subMenu = area.areaType.menus
            .asSequence()
            .filter { it.parentId != null && it.parentId > 0 }
            .groupBy { it.parentId }

         val menus: List<MenuType> = area.areaType.menus
            .filter { it.parentId == null }
            .map { menu ->
               subMenu[menu.id]?.let { menu.menus.addAll(it) }
               menu
            }

         //area.copy(menus = menus as MutableList<MenuType>)
         area.areaType.menus.clear()
         area.areaType.menus.addAll(menus)
         area
      }
   }

   private fun mapArea(rs: ResultSet): AreaType {
      return AreaTypeEntity(
         id = rs.getInt("area_id"),
         value = rs.getString("area_value"),
         description = rs.getString("area_description"),
         localizationCode = rs.getString("area_localization_code"),
         menus = mutableListOf()
      )
   }

   private fun mapMenu(rs: ResultSet): MenuType {
      return MenuType(
         id = rs.getInt("menu_id"),
         parentId = rs.getIntOrNull("menu_parent_id"),
         value = rs.getString("menu_value"),
         description = rs.getString("menu_description"),
         orderNumber = rs.getInt("menu_order_number"),
         localizationCode = rs.getString("menu_localization_code"),
         modules = mutableListOf()
      )

   }

   private fun mapModule(rs: ResultSet): ModuleType? {
      return if (rs.getString("module_id") != null) {
         ModuleType(
            id = rs.getInt("module_id"),
            value = rs.getString("module_value"),
            description = rs.getString("module_description"),
            localizationCode = rs.getString("module_localization_code"),
            level = rs.getIntOrNull("module_configs_level")
         )
      } else {
         null
      }
   }
}
