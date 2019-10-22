package com.cynergisuite.middleware.audit.schedule

import com.cynergisuite.domain.Page
import com.cynergisuite.domain.PageRequest
import com.cynergisuite.middleware.audit.schedule.infrastruture.AuditScheduleRepository
import com.cynergisuite.middleware.localization.LocalizationService
import io.micronaut.validation.Validated
import java.util.Locale
import javax.inject.Singleton
import javax.validation.Valid

@Singleton
class AuditScheduleService(
   private val auditScheduleRepository: AuditScheduleRepository,
   private val auditScheduleValidator: AuditScheduleValidator,
   private val localizationService: LocalizationService
) {

   fun fetchById(id: Long, locale: Locale): AuditScheduleValueObject? =
      auditScheduleRepository.findOne(id)?.let { AuditScheduleValueObject(it, locale, localizationService) }

   @Validated
   fun fetchAll(@Valid pageRequest: PageRequest, locale: Locale): Page<AuditScheduleValueObject> {
      val found = auditScheduleRepository.findAll(pageRequest)

      return found.toPage(pageRequest) { AuditScheduleValueObject(it, locale, localizationService) }
   }

   @Validated
   fun create(@Valid auditScheduleCreate: AuditScheduleCreateDataTransferObject): AuditScheduleValueObject {
      val auditScheduleEntity = auditScheduleValidator.validateCreate(auditScheduleCreate)
   }
}
