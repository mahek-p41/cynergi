package com.cynergisuite.middleware.audit.schedule

import com.cynergisuite.domain.Entity
import com.cynergisuite.middleware.schedule.Schedule
import com.cynergisuite.middleware.store.Store
import java.time.OffsetDateTime
import java.util.UUID

data class AuditSchedule(
   val id: Long? = null,
   val uuRowId: UUID = UUID.randomUUID(),
   val timeCreated: OffsetDateTime = OffsetDateTime.now(),
   val timeUpdated: OffsetDateTime = timeCreated,
   val store: Store,
   val departmentAccess: String,
   val schedule: Schedule
) : Entity<AuditSchedule> {
   override fun entityId(): Long? = id
   override fun rowId(): UUID = uuRowId
   override fun copyMe(): AuditSchedule = copy()
}
