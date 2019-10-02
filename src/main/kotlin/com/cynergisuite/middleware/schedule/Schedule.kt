package com.cynergisuite.middleware.schedule

import com.cynergisuite.domain.Entity
import java.time.OffsetDateTime
import java.util.UUID

data class Schedule(
   val id: Long? = null,
   val uuRowId: UUID = UUID.randomUUID(),
   val timeCreated: OffsetDateTime = OffsetDateTime.now(),
   val timeUpdated: OffsetDateTime = timeCreated,
   val title: String,
   val description: String?,
   val schedule: String,
   val command: String,
   val type: ScheduleType
) : Entity<Schedule> {
   override fun entityId(): Long? = id
   override fun rowId(): UUID = uuRowId
   override fun copyMe(): Schedule = copy()
}
