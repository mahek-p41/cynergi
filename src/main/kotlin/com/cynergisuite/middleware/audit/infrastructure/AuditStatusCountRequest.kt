package com.cynergisuite.middleware.audit.infrastructure

import com.cynergisuite.domain.DataTransferObject
import com.cynergisuite.extensions.saturday
import com.cynergisuite.extensions.sunday
import java.time.OffsetDateTime

@DataTransferObject
data class AuditStatusCountRequest(
   val from: OffsetDateTime = OffsetDateTime.now().sunday(),
   val thru: OffsetDateTime = OffsetDateTime.now().saturday(),
   val statuses: Set<String> = setOf("OPENED", "IN-PROGRESS", "COMPLETED", "CANCELED", "SIGNED-OFF")
) {
   override fun toString(): String {
      return "?from=$from&thru=$thru&statuses=${statuses.joinToString(",")}"
   }
}
