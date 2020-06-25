package com.cynergisuite.middleware.area.infrastructure

import com.cynergisuite.extensions.insertReturning
import com.cynergisuite.middleware.area.ModuleEntity
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

   @Transactional
   fun insert(moduleEntity: ModuleEntity, company: Company): ModuleEntity {
      logger.debug("Inserting area {}", moduleEntity)

      return jdbc.insertReturning("""
         INSERT INTO module(company_id, module_type_id, level)
         VALUES (:company_id, :module_type_id, :level)
         RETURNING
            *
         """,
         mapOf(
            "company_id" to company.myId(),
            "module_type_id" to moduleEntity.moduleType.myId(),
            "level" to moduleEntity.level
         ),
         RowMapper { rs, _ -> mapRowEntity(rs, company, moduleEntity) }
      )
   }

   fun mapRowEntity(rs: ResultSet, company: Company, moduleEntity: ModuleEntity, columnPrefix: String = EMPTY): ModuleEntity =
      ModuleEntity(
         id = rs.getLong("${columnPrefix}id"),
         company = company,
         moduleType = moduleEntity.moduleType,
         level = moduleEntity.level
      )
}
