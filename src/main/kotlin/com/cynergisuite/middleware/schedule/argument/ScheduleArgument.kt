package com.cynergisuite.middleware.schedule.argument

import com.cynergisuite.domain.Entity
import java.time.OffsetDateTime
import java.util.UUID

data class ScheduleArgument(
   val id: Long? = null,
   val uuRowId: UUID = UUID.randomUUID(),
   val timeCreated: OffsetDateTime = OffsetDateTime.now(),
   val timeUpdated: OffsetDateTime = timeCreated,
   val value: String,
   val description: String
) : Entity<ScheduleArgument> {

   override fun entityId(): Long? = id
   override fun rowId(): UUID = uuRowId
   override fun copyMe(): ScheduleArgument = copy()
}
