package com.cynergisuite.middleware.audit.permission.infrastructure

import com.cynergisuite.extensions.findFirstOrNull
import com.cynergisuite.middleware.audit.permission.AuditPermissionType
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import javax.inject.Singleton

@Singleton
class AuditPermissionTypeRepository(
   private val jdbc: NamedParameterJdbcTemplate
) {
   private val logger: Logger = LoggerFactory.getLogger(AuditPermissionTypeRepository::class.java)

   fun findOne(id: Int): AuditPermissionType? {
      logger.debug("Searching for AuditPermissionType by ID {}", id)

      return jdbc.findFirstOrNull(
         """
         SELECT
            id,
            value,
            description,
            localization_code
         FROM audit_permission_type_domain
         WHERE id = :id""",
         mapOf("id" to id)
      ) { rs, _ ->
         AuditPermissionType(
            id = rs.getInt("id"),
            value = rs.getString("value"),
            description = rs.getString("description"),
            localizationCode = rs.getString("localization_code")
         )
      }
   }

   fun exists(id: Int): Boolean {
      val exists = jdbc.queryForObject("SELECT EXISTS(SELECT id FROM audit_permission_type_domain WHERE id = :id)", mapOf("id" to id), Boolean::class.java)!!

      logger.trace("Checking if AuditPermissionType: {} exists resulted in {}", id, exists)

      return exists
   }

   fun doesNotExist(id: Int) = !exists(id)
}
