package com.cynergisuite.middleware.schedule

import com.cynergisuite.domain.Identifiable
import com.cynergisuite.middleware.localization.LocalizationService
import com.cynergisuite.middleware.schedule.argument.ScheduleArgumentValueObject
import com.cynergisuite.middleware.schedule.command.ScheduleCommandTypeValueObject
import com.cynergisuite.middleware.schedule.type.ScheduleTypeValueObject
import io.swagger.v3.oas.annotations.media.Schema
import java.util.*
import javax.validation.Valid
import javax.validation.constraints.NotNull
import javax.validation.constraints.Positive
import javax.validation.constraints.Size

@Schema(name = "Schedule", description = "A user managed scheduled job in the system")
data class ScheduleValueObject(

   @field:Positive
   var id: Long? = null,

   @field:NotNull
   @field:Size(min = 3, max = 64)
   var title: String? = null,

   @field:NotNull
   @field:Size(min = 3, max = 256)
   var description: String?,

   @field:NotNull
   @field:Size(min = 5, max = 592)
   var schedule: String,

   @field:NotNull
   @field:Size(min = 5, max = 1024)
   var command: ScheduleCommandTypeValueObject,

   @field:NotNull
   @field:Valid
   var type: ScheduleTypeValueObject,

   var arguments: MutableList<ScheduleArgumentValueObject> = mutableListOf()
) : Identifiable {

   constructor(entity: ScheduleEntity, locale: Locale, localizationService: LocalizationService) :
      this(
         id = entity.id,
         title = entity.title,
         description = entity.description,
         schedule = entity.schedule,
         command = ScheduleCommandTypeValueObject(entity.command, locale, localizationService),
         type = ScheduleTypeValueObject(entity.type, entity.type.localizeMyDescription(locale, localizationService)),
         arguments = entity.arguments.asSequence().map { ScheduleArgumentValueObject(it) }.toMutableList()
      )

   override fun myId(): Long? = id
}
