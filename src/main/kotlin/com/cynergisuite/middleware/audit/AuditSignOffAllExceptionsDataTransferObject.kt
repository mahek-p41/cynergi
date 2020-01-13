package com.cynergisuite.middleware.audit

import com.cynergisuite.domain.DataTransferObject
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonInclude

@DataTransferObject
@JsonInclude(JsonInclude.Include.NON_NULL)
data class AuditSignOffAllExceptionsDataTransferObject(
   val signedOff: Int
)
