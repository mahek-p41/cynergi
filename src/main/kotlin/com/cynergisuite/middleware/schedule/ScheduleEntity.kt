package com.cynergisuite.middleware.schedule

import com.cynergisuite.domain.Identifiable
import com.cynergisuite.middleware.company.Company
import com.cynergisuite.middleware.schedule.argument.ScheduleArgumentEntity
import com.cynergisuite.middleware.schedule.command.ScheduleCommandType
import com.cynergisuite.middleware.schedule.command.ScheduleCommandTypeEntity
import com.cynergisuite.middleware.schedule.type.ScheduleType
import java.util.UUID

data class ScheduleEntity(
   val id: UUID? = null,
   val title: String,
   val description: String?,
   val schedule: String,
   val command: ScheduleCommandType,
   val type: ScheduleType,
   val enabled: Boolean = true,
   val company: Company,
   val arguments: MutableSet<ScheduleArgumentEntity> = mutableSetOf()
) : Identifiable {

   constructor(title: String, description: String?, schedule: String, command: ScheduleCommandTypeEntity, type: ScheduleType, company: Company) :
      this(
         id = null,
         title = title,
         description = description ?: title,
         schedule = schedule,
         command = command,
         type = type,
         company = company
      )

   constructor(title: String, description: String?, schedule: String, command: ScheduleCommandTypeEntity, type: ScheduleType, company: Company, arguments: MutableSet<ScheduleArgumentEntity>) :
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

   override fun myId(): UUID? = id
}
