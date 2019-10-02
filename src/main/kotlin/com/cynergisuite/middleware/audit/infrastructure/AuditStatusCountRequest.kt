package com.cynergisuite.middleware.audit.infrastructure

import com.cynergisuite.domain.DataTransferObject
import com.cynergisuite.extensions.saturday
import com.cynergisuite.extensions.sunday
import java.time.OffsetDateTime

@DataTransferObject
data class AuditStatusCountRequest(
   val from: OffsetDateTime = OffsetDateTime.now().sunday(),
   val thru: OffsetDateTime = OffsetDateTime.now().saturday(),
   val statuses: MutableSet<String> = mutableSetOf("OPENED", "IN-PROGRESS", "COMPLETED", "CANCELED", "SIGNED-OFF")
)
