package com.cynergisuite.middleware.schedule

import com.cynergisuite.domain.Identifiable
import com.cynergisuite.middleware.schedule.argument.ScheduleArgumentEntity
import com.cynergisuite.middleware.schedule.command.ScheduleCommandType
import com.cynergisuite.middleware.schedule.command.ScheduleCommandTypeEntity
import com.cynergisuite.middleware.schedule.type.ScheduleType

data class ScheduleEntity(
   val id: Long? = null,
   val title: String,
   val description: String?,
   val schedule: String,
   val command: ScheduleCommandType,
   val type: ScheduleType,
   val enabled: Boolean = true,
   val arguments: MutableSet<ScheduleArgumentEntity> = mutableSetOf()
) : Identifiable {

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
}
