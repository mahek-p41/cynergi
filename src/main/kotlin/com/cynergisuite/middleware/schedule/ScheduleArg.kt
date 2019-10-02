package com.cynergisuite.middleware.schedule

import com.cynergisuite.domain.Entity
import java.time.OffsetDateTime
import java.util.UUID

data class ScheduleArg(
   val id: Long? = null,
   val uuRowId: UUID = UUID.randomUUID(),
   val timeCreated: OffsetDateTime = OffsetDateTime.now(),
   val timeUpdated: OffsetDateTime = timeCreated
) : Entity<ScheduleArg> {
   override fun entityId(): Long? = id
   override fun rowId(): UUID = uuRowId
   override fun copyMe(): ScheduleArg = copy()
}
