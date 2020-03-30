package com.cynergisuite.middleware.audit

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
data class AuditSignOffAllExceptionsDataTransferObject(
   val signedOff: Int
)
