package com.cynergisuite.middleware.audit

import com.cynergisuite.middleware.audit.status.AuditStatusValueObject

data class AuditStatusReportDataTransferObject(
   val count: Int = 0,
   val status: AuditStatusValueObject
)
