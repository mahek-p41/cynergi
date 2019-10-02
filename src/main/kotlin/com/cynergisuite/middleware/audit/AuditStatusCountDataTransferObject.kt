package com.cynergisuite.middleware.audit

import com.cynergisuite.domain.DataTransferObject
import com.cynergisuite.middleware.audit.status.AuditStatusValueObject

@DataTransferObject
data class AuditStatusCountDataTransferObject(
   val count: Int = 0,
   val status: AuditStatusValueObject
)
