package com.cynergisuite.middleware.audit.action

import com.cynergisuite.domain.Entity
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
) : Entity<AuditActionEntity> {

   override fun myId(): Long? = id
   override fun rowId(): UUID = uuRowId
   override fun copyMe(): AuditActionEntity = copy()
}
