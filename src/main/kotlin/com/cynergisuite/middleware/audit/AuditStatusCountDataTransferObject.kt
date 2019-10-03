package com.cynergisuite.middleware.audit

import com.cynergisuite.domain.DataTransferObject
import com.cynergisuite.middleware.audit.status.AuditStatusCount
import com.cynergisuite.middleware.audit.status.AuditStatusValueObject
import com.cynergisuite.middleware.localization.LocalizationService
import java.util.Locale

@DataTransferObject
data class AuditStatusCountDataTransferObject(
   var count: Int? = 0,
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
