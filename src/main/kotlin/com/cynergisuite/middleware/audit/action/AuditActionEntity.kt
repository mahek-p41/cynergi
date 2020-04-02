package com.cynergisuite.middleware.audit.action

import com.cynergisuite.domain.Identifiable
import com.cynergisuite.middleware.audit.status.AuditStatus
import com.cynergisuite.middleware.employee.EmployeeEntity
import java.time.OffsetDateTime
import java.util.UUID

data class AuditActionEntity (
   val id: Long? = null,
   val uuRowId: UUID = UUID.randomUUID(),
   val timeCreated: OffsetDateTime = OffsetDateTime.now(),
   val timeUpdated: OffsetDateTime = timeCreated,
   val status: AuditStatus,
   val changedBy: EmployeeEntity // FIXME use Employee
) : Identifiable {
   override fun myId(): Long? = id
}
