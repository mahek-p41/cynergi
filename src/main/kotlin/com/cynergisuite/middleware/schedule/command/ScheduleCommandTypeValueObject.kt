package com.cynergisuite.middleware.schedule.command

import com.cynergisuite.domain.ValueObject
import com.cynergisuite.domain.ValueObjectBase
import com.cynergisuite.middleware.localization.LocalizationService
import java.util.Locale
import javax.validation.constraints.NotNull
import javax.validation.constraints.Positive

@ValueObject
data class ScheduleCommandTypeValueObject(
   @field:Positive
   var id: Long? = null,

   @field:NotNull
   var value: String? = null,

   @field:NotNull
   var description: String? = null

) : ValueObjectBase<ScheduleCommandTypeValueObject>() {

   constructor(type: ScheduleCommandType, locale: Locale, localizationService: LocalizationService) :
      this (
         id = type.id,
         value = type.value,
         description = type.localizeMyDescription(locale, localizationService)
      )

   override fun myId(): Long? = id
   override fun copyMe(): ScheduleCommandTypeValueObject = copy()
}
