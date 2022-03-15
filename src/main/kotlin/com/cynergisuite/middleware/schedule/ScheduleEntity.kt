package com.cynergisuite.middleware.schedule

import com.cynergisuite.domain.Identifiable
import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.schedule.argument.ScheduleArgumentEntity
import com.cynergisuite.middleware.schedule.command.ScheduleCommandType
import com.cynergisuite.middleware.schedule.command.ScheduleCommandTypeEntity
import com.cynergisuite.middleware.schedule.type.ScheduleType
import java.util.UUID

data class ScheduleEntity(
   val id: UUID? = null,
   val title: String,
   val description: String?,
   @Deprecated("This needs to be moved to an schedule_arg") // TODO schedule this shouldn't be deprecated.  Utilize it to determine what should process.
   val schedule: String,
   val command: ScheduleCommandType,
   val type: ScheduleType,
   val enabled: Boolean = true,
   val company: CompanyEntity,
   val arguments: MutableSet<ScheduleArgumentEntity> = mutableSetOf()
) : Identifiable {

   constructor(title: String, description: String?, schedule: String, command: ScheduleCommandTypeEntity, type: ScheduleType, company: CompanyEntity) :
      this(
         id = null,
         title = title,
         description = description ?: title,
         schedule = schedule,
         command = command,
         type = type,
         company = company
      )

   constructor(title: String, description: String?, schedule: String, command: ScheduleCommandTypeEntity, type: ScheduleType, company: CompanyEntity, arguments: MutableSet<ScheduleArgumentEntity>) :
      this(
         id = null,
         title = title,
         description = description ?: title,
         schedule = schedule,
         command = command,
         type = type,
         company = company,
         arguments = arguments
      )

   constructor(scheduleEntity: ScheduleEntity, arguments: MutableSet<ScheduleArgumentEntity> = mutableSetOf()) :
      this(
         id = scheduleEntity.id,
         title = scheduleEntity.title,
         description = scheduleEntity.description,
         schedule = scheduleEntity.schedule,
         command = scheduleEntity.command,
         type = scheduleEntity.type,
         company = scheduleEntity.company,
         arguments = arguments
      )

   override fun myId(): UUID? = id
}
