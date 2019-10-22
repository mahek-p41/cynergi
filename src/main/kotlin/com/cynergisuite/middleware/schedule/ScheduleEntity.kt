package com.cynergisuite.middleware.schedule

import com.cynergisuite.domain.Entity
import com.cynergisuite.middleware.schedule.argument.ScheduleArgumentEntity
import com.cynergisuite.middleware.schedule.type.ScheduleTypeEntity
import java.time.OffsetDateTime
import java.util.UUID

data class ScheduleEntity(
   val id: Long? = null,
   val uuRowId: UUID = UUID.randomUUID(),
   val timeCreated: OffsetDateTime = OffsetDateTime.now(),
   val timeUpdated: OffsetDateTime = timeCreated,
   val title: String,
   val description: String?,
   val schedule: String,
   val command: String,
   val type: ScheduleTypeEntity,
   val arguments: MutableList<ScheduleArgumentEntity> = mutableListOf()
) : Entity<ScheduleEntity> {

   constructor(title: String, description: String?, schedule: String, command: String, type: ScheduleTypeEntity) :
      this(
         id = null,
         title = title,
         description = description ?: title,
         schedule = schedule,
         command = command,
         type = type
      )

   override fun entityId(): Long? = id
   override fun rowId(): UUID = uuRowId
   override fun copyMe(): ScheduleEntity = copy()
}
