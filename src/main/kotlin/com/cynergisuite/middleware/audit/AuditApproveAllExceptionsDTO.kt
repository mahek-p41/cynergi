package com.cynergisuite.middleware.audit

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.micronaut.core.annotation.Introspected

@Introspected
@JsonInclude(NON_NULL)
data class AuditApproveAllExceptionsDTO(
   val approved: Int
)
