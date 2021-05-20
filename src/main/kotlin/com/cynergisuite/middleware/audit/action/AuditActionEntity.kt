package com.cynergisuite.middleware.audit.action

import com.cynergisuite.domain.Identifiable
import com.cynergisuite.middleware.audit.status.AuditStatus
import com.cynergisuite.middleware.employee.EmployeeEntity
import java.time.OffsetDateTime
import java.util.UUID

data class AuditActionEntity(
   val id: UUID? = null,
   val timeCreated: OffsetDateTime = OffsetDateTime.now(),
   val timeUpdated: OffsetDateTime = timeCreated,
   val status: AuditStatus,
   val changedBy: EmployeeEntity
) : Identifiable {
   override fun myId(): UUID? = id
}
