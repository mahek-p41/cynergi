package com.cynergisuite.middleware.audit

import com.cynergisuite.domain.DataTransferObject
import com.cynergisuite.middleware.audit.status.AuditStatusCount
import com.cynergisuite.middleware.audit.status.AuditStatusValueObject
import com.cynergisuite.middleware.localization.LocalizationService
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.swagger.v3.oas.annotations.media.Schema
import java.util.Locale

@DataTransferObject
@JsonInclude(NON_NULL)
@Schema(name = "AuditStatusCount", title = "State of Audit Statuses", description = "Lists counts for current audit statuses")
data class AuditStatusCountDataTransferObject(

   @field:Schema(name = "count", description = "Number of audits at the associated status")
   var count: Int? = 0,

   @field:Schema(name = "status", description = "The Audit status")
   var status: AuditStatusValueObject? = null
) {
   constructor(auditStatusCount: AuditStatusCount, locale: Locale, localizationService: LocalizationService) :
      this (
         count = auditStatusCount.count,
         status = AuditStatusValueObject(
            id = auditStatusCount.id,
            value = auditStatusCount.value,
            description = auditStatusCount.localizeMyDescription(locale, localizationService),
            color = auditStatusCount.color
         )
      )
}
