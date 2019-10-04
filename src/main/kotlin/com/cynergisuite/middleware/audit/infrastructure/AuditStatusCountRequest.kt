package com.cynergisuite.middleware.audit.infrastructure

import com.cynergisuite.domain.DataTransferObject
import java.time.OffsetDateTime
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull

@DataTransferObject
data class AuditStatusCountRequest(

   @field:NotNull
   val from: OffsetDateTime? = null,

   @field:NotNull
   val thru: OffsetDateTime? = null,

   @field:NotNull
   @field:NotEmpty
   val status: Set<String>? = emptySet()
) {
   override fun toString(): String {
      return "?from=$from&thru=$thru&statuses=${status?.joinToString(",")}"
   }
}
