package com.cynergisuite.middleware.schedule

import com.cynergisuite.domain.Identifiable
import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.schedule.argument.ScheduleArgumentEntity
import com.cynergisuite.middleware.schedule.command.ScheduleCommandTypeEntity
import com.cynergisuite.middleware.schedule.type.ScheduleTypeEntity
import java.util.UUID

data class ScheduleEntity(
   val id: UUID? = null,
   val title: String,
   val description: String?,
   val schedule: String,
   val command: ScheduleCommandTypeEntity,
   val type: ScheduleTypeEntity,
   val enabled: Boolean = true,
   val company: CompanyEntity,
   val arguments: MutableSet<ScheduleArgumentEntity> = mutableSetOf()
) : Identifiable {

   constructor(title: String, description: String?, schedule: String, command: ScheduleCommandTypeEntity, type: ScheduleTypeEntity, company: CompanyEntity) :
      this(
         id = null,
         title = title,
         description = description ?: title,
         schedule = schedule,
         command = command,
         type = type,
         company = company
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
