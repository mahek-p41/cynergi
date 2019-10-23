package com.cynergisuite.middleware.audit.schedule.infrastruture

import io.micronaut.http.annotation.Controller
import io.micronaut.security.annotation.Secured
import io.micronaut.security.rules.SecurityRule
import io.micronaut.security.rules.SecurityRule.IS_AUTHENTICATED

@Secured(IS_AUTHENTICATED)
@Controller("/api/audit/schedule")
class AuditScheduleController {
}
