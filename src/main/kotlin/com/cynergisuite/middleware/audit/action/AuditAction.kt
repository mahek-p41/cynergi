package com.cynergisuite.middleware.audit.action

import com.cynergisuite.domain.Entity
import com.cynergisuite.middleware.audit.status.AuditStatus
import com.cynergisuite.middleware.employee.Employee
import java.time.OffsetDateTime
import java.util.UUID

data class AuditAction (
   val id: Long? = null,
   val uuRowId: UUID = UUID.randomUUID(),
   val timeCreated: OffsetDateTime = OffsetDateTime.now(),
   val timeUpdated: OffsetDateTime = timeCreated,
   val status: AuditStatus,
   val changedBy: Employee
) : Entity<AuditAction> {

   override fun myId(): Long? = id
   override fun rowId(): UUID = uuRowId
   override fun copyMe(): AuditAction = copy()

   fun getEmpName() : String = changedBy.firstNameMi + " " + changedBy.lastName
}
