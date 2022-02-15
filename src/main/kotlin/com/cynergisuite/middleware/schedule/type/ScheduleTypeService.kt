package com.cynergisuite.middleware.schedule.type

import com.cynergisuite.domain.Page
import com.cynergisuite.domain.PageRequest
import com.cynergisuite.middleware.localization.LocalizationService
import com.cynergisuite.middleware.schedule.type.infrastructure.ScheduleTypeRepository
import jakarta.inject.Inject
import jakarta.inject.Singleton
import java.util.Locale

@Singleton
class ScheduleTypeService @Inject constructor(
   private val localizationService: LocalizationService,
   private val scheduleTypeRepository: ScheduleTypeRepository
) {

   fun fetchAll(pageRequest: PageRequest, locale: Locale): Page<ScheduleTypeValueObject> {
      val found = scheduleTypeRepository.findAll(pageRequest)

      return found.toPage { ScheduleTypeValueObject(it, it.localizeMyDescription(locale, localizationService)) }
   }
}
