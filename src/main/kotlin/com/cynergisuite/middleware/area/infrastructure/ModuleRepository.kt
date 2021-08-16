package com.cynergisuite.middleware.area.infrastructure

import com.cynergisuite.extensions.findFirstOrNull
import com.cynergisuite.extensions.queryForObject
import com.cynergisuite.extensions.update
import com.cynergisuite.middleware.area.ModuleType
import com.cynergisuite.middleware.company.CompanyEntity
import io.micronaut.transaction.annotation.ReadOnly
import org.apache.commons.lang3.StringUtils.EMPTY
import org.jdbi.v3.core.Jdbi
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.sql.ResultSet
import javax.inject.Inject
import javax.inject.Singleton
import javax.transaction.Transactional

@Singleton
class ModuleRepository @Inject constructor(
   private val jdbc: Jdbi
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
   fun insertConfig(moduleType: ModuleType, company: CompanyEntity): ModuleType {
      logger.debug("Inserting module level {}", moduleType)

      jdbc.update(
         """
         INSERT INTO module(company_id, module_type_id, level)
         VALUES (:company_id, :module_type_id, :level)
         """,
         mapOf(
            "company_id" to company.id,
            "module_type_id" to moduleType.myId(),
            "level" to moduleType.level
         )
      )

      return moduleType.copy(level = moduleType.level)
   }

   @Transactional
   fun updateConfig(moduleType: ModuleType, company: CompanyEntity): ModuleType {
      logger.debug("Updating module level {}", moduleType)

      jdbc.update(
         """
         UPDATE module
         SET
            level = :level
         WHERE company_id = :company_id AND module_type_id = :module_type_id
         """,
         mapOf(
            "company_id" to company.id,
            "module_type_id" to moduleType.myId(),
            "level" to moduleType.level
         )
      )

      return moduleType.copy(level = moduleType.level)
   }

   @ReadOnly
   fun findOne(moduleTypeId: Int, company: CompanyEntity): ModuleType? {
      val found = jdbc.findFirstOrNull(
         "${selectBaseQuery()} WHERE module.id = :type_id",
         mapOf("type_id" to moduleTypeId)
      ) { rs, _ -> mapSimpleModule(rs, company, "module_") }

      logger.trace("Searching for ModuleTypeDomain: {} resulted in {}", moduleTypeId, found)

      return found
   }

   @ReadOnly
   fun configExists(moduleTypeId: Int, company: CompanyEntity): Boolean {
      val exists = jdbc.queryForObject(
         """
         SELECT EXISTS (SELECT * FROM module WHERE module_type_id = :module_type_id AND company_id = :company_id)
         """,
         mapOf("module_type_id" to moduleTypeId, "company_id" to company.id),
         Boolean::class.java
      )

      logger.trace("Checking if Module config exists {}")

      return exists
   }

   fun mapSimpleModule(rs: ResultSet, company: CompanyEntity, columnPrefix: String = EMPTY): ModuleType =
      ModuleType(
         id = rs.getInt("${columnPrefix}id"),
         value = rs.getString("${columnPrefix}value"),
         program = rs.getString("${columnPrefix}program"),
         description = rs.getString("${columnPrefix}description"),
         localizationCode = rs.getString("${columnPrefix}localization_code")
      )
}
