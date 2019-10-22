package com.cynergisuite.middleware.schedule

import com.cynergisuite.domain.ValueObject
import com.cynergisuite.middleware.localization.LocalizationService
import com.cynergisuite.middleware.schedule.argument.ScheduleArgumentValueObject
import com.cynergisuite.middleware.schedule.type.ScheduleTypeValueObject
import java.util.Locale

@ValueObject
data class ScheduleValueObject(
   val id: Long? = null,
   val title: String,
   val description: String?,
   val schedule: String,
   val command: String,
   val type: ScheduleTypeValueObject,
   val arguments: MutableList<ScheduleArgumentValueObject> = mutableListOf()
) {
   constructor(entity: ScheduleEntity, locale: Locale, localizationService: LocalizationService) :
      this(
         id = entity.id,
         title = entity.title,
         description = entity.description,
         schedule = entity.schedule,
         command = entity.command,
         type = ScheduleTypeValueObject(entity.type, entity.type.localizeMyDescription(locale, localizationService)),
         arguments = entity.arguments.asSequence().map { ScheduleArgumentValueObject(it) }.toMutableList()
      )
}
