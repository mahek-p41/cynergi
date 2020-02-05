package com.cynergisuite.middleware.audit.permission.infrastructure

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import javax.inject.Singleton

@Singleton
class AuditPermissionTypeRepository(
   private val jdbc: NamedParameterJdbcTemplate
) {
}
