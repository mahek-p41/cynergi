package com.cynergisuite.middleware.schedule

import com.cynergisuite.domain.Entity
import com.cynergisuite.middleware.schedule.argument.ScheduleArgument
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
   val type: ScheduleType,
   val arguments: MutableList<ScheduleArgument> = mutableListOf()
) : Entity<Schedule> {

   constructor(title: String, description: String?, schedule: String, command: String, type: ScheduleType) :
      this(
         id = null,
         title = title,
         description = description,
         schedule = schedule,
         command = command,
         type = type
      )

   override fun entityId(): Long? = id
   override fun rowId(): UUID = uuRowId
   override fun copyMe(): Schedule = copy()
}
