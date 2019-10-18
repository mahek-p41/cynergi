package com.cynergisuite.middleware.audit.schedule.infrastruture

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import javax.inject.Singleton

@Singleton
class AuditScheduleRepository(
   private val jdbc: NamedParameterJdbcTemplate
) {
}
