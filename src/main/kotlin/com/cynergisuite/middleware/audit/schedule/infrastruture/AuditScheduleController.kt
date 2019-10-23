package com.cynergisuite.middleware.audit.schedule.infrastruture

import io.micronaut.http.annotation.Controller
import io.micronaut.security.annotation.Secured
import io.micronaut.security.rules.SecurityRule

@Secured(SecurityRule.IS_AUTHENTICATED)
@Controller("/api/audit/schedule")
class AuditScheduleController {
}
