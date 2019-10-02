package com.cynergisuite.middleware.audit

import com.cynergisuite.domain.DataTransferObject
import com.cynergisuite.middleware.audit.status.AuditStatusValueObject

@DataTransferObject
data class AuditStatusCountDataTransferObject(
   val count: Int = 0,
   val status: AuditStatusValueObject // TODO needs to be localized somehow, this currently won't support doing that.  Need to use the AuditStatus entity and convert to this for the JSON somehow
)
