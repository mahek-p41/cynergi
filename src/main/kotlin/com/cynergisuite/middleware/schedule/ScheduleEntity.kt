package com.cynergisuite.middleware.schedule

import com.cynergisuite.domain.Entity
import com.cynergisuite.middleware.schedule.argument.ScheduleArgumentEntity
import com.cynergisuite.middleware.schedule.command.ScheduleCommandTypeEntity
import com.cynergisuite.middleware.schedule.type.ScheduleType
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
   val command: ScheduleCommandTypeEntity,
   val type: ScheduleType,
   val enabled: Boolean = true,
   val arguments: MutableSet<ScheduleArgumentEntity> = mutableSetOf()
) : Entity<ScheduleEntity> {

   constructor(title: String, description: String?, schedule: String, command: ScheduleCommandTypeEntity, type: ScheduleType) :
      this(
         id = null,
         title = title,
         description = description ?: title,
         schedule = schedule,
         command = command,
         type = type
      )

   constructor(title: String, description: String?, schedule: String, command: ScheduleCommandTypeEntity, type: ScheduleType, arguments: MutableSet<ScheduleArgumentEntity>) :
      this(
         id = null,
         title = title,
         description = description ?: title,
         schedule = schedule,
         command = command,
         type = type,
         arguments = arguments
      )

   override fun myId(): Long? = id
   override fun rowId(): UUID = uuRowId
   override fun copyMe(): ScheduleEntity = copy()
}
