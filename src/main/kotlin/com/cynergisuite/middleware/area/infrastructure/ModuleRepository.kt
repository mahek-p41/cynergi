package com.cynergisuite.middleware.area.infrastructure

import com.cynergisuite.extensions.findFirstOrNull
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
class ModuleRepository @Inject constructor(
   private val jdbc: NamedParameterJdbcTemplate
) {
   private val logger: Logger = LoggerFactory.getLogger(ModuleRepository::class.java)

   private fun selectBaseQuery(): String {
      return """
         SELECT
                  module.id AS module_id,
                  module.menu_type_id AS module_menu_type_id,
                  module.value AS module_value,
                  module.program AS module_program,
                  module.description AS module_description,
                  module.localization_code AS module_localization_code,
                  menu.id AS menu_id,
                  menu.value AS menu_value,
                  menu.description AS menu_description,
                  menu.localization_code AS menu_localization_code,
                  areaEntity.id AS area_id,
                  areaEntity.value AS area_value,
                  areaEntity.description AS area_description,
                  areaEntity.localization_code AS area_localization_code
         FROM module_type_domain module
            JOIN menu_type_domain menu ON menu.id = module.menu_type_id
            JOIN area_type_domain areaEntity ON areaEntity.id = menu.area_type_id
      """
   }

   @Transactional
   fun insertConfig(moduleType: ModuleType, company: Company): ModuleType {
      logger.debug("Inserting module level {}", moduleType)

      jdbc.update(
         """
         INSERT INTO module(company_id, module_type_id, level)
         VALUES (:company_id, :module_type_id, :level)
         """,
         mapOf(
            "company_id" to company.myId(),
            "module_type_id" to moduleType.myId(),
            "level" to moduleType.level
         )
      )

      return moduleType.copy(level = moduleType.level)
   }

   @Transactional
   fun updateConfig(moduleType: ModuleType, company: Company): ModuleType {
      logger.debug("Updating module level {}", moduleType)

      jdbc.update(
         """
         UPDATE module
         SET
            level = :level
         WHERE company_id = :company_id AND module_type_id = :module_type_id
         """,
         mapOf(
            "company_id" to company.myId(),
            "module_type_id" to moduleType.myId(),
            "level" to moduleType.level
         )
      )

      return moduleType.copy(level = moduleType.level)
   }

   fun findOne(moduleTypeId: Long, company: Company): ModuleType? {
      val found = jdbc.findFirstOrNull(
         "${selectBaseQuery()} WHERE module.id = :type_id",
         mapOf("type_id" to moduleTypeId),
         RowMapper { rs, _ -> mapSimpleModule(rs, company, "module_") }
      )

      logger.trace("Searching for ModuleTypeDomain: {} resulted in {}", moduleTypeId, found)

      return found
   }

   fun isConfigExists(moduleTypeId: Long): Boolean {
      val exists = jdbc.queryForObject(
         """
         SELECT EXISTS (SELECT * FROM module WHERE module_type_id = :module_type_id)
         """,
         mapOf("module_type_id" to moduleTypeId), Boolean::class.java
      )!!

      logger.trace("Checking if Module config exists {}")

      return exists
   }

   fun mapSimpleModule(rs: ResultSet, company: Company, columnPrefix: String = EMPTY): ModuleType =
      ModuleType(
         id = rs.getLong("${columnPrefix}id"),
         value = rs.getString("${columnPrefix}value"),
         program = rs.getString("${columnPrefix}program"),
         description = rs.getString("${columnPrefix}description"),
         localizationCode = rs.getString("${columnPrefix}localization_code")
      )
}
