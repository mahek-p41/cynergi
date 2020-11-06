package com.cynergisuite.middleware.audit

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
data class AuditApproveAllExceptionsDTO(
   val approved: Int
)
