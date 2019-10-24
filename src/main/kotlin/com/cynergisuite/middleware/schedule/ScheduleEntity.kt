package com.cynergisuite.middleware.schedule

import com.cynergisuite.domain.Entity
import com.cynergisuite.middleware.schedule.argument.ScheduleArgumentEntity
import com.cynergisuite.middleware.schedule.command.ScheduleCommandTypeEntity
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
   val command: ScheduleCommandTypeEntity,
   val type: ScheduleTypeEntity,
   val enabled: Boolean = true,
   val arguments: MutableList<ScheduleArgumentEntity> = mutableListOf()
) : Entity<ScheduleEntity> {

   constructor(title: String, description: String?, schedule: String, command: ScheduleCommandTypeEntity, type: ScheduleTypeEntity) :
      this(
         id = null,
         title = title,
         description = description ?: title,
         schedule = schedule,
         command = command,
         type = type
      )

   constructor(title: String, description: String?, schedule: String, command: ScheduleCommandTypeEntity, type: ScheduleTypeEntity, arguments: MutableList<ScheduleArgumentEntity>) :
      this(
         id = null,
         title = title,
         description = description ?: title,
         schedule = schedule,
         command = command,
         type = type,
         arguments = arguments
      )

   override fun entityId(): Long? = id
   override fun rowId(): UUID = uuRowId
   override fun copyMe(): ScheduleEntity = copy()
}
