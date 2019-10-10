package com.cynergisuite.middleware.audit

import com.cynergisuite.domain.Page
import com.cynergisuite.domain.infrastructure.RepositoryPage
import com.cynergisuite.middleware.audit.infrastructure.AuditPageRequest
import com.cynergisuite.middleware.audit.infrastructure.AuditRepository
import com.cynergisuite.middleware.employee.EmployeeValueObject
import com.cynergisuite.middleware.localization.LocalizationService
import io.micronaut.validation.Validated
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import javax.validation.Valid

@Singleton
class AuditService @Inject constructor(
   private val auditRepository: AuditRepository,
   private val auditValidator: AuditValidator,
   private val localizationService: LocalizationService
) {
   fun fetchById(id: Long, locale: Locale): AuditValueObject? =
      auditRepository.findOne(id)?.let { AuditValueObject(it, locale, localizationService) }

   @Validated
   fun fetchAll(@Valid pageRequest: AuditPageRequest, locale: Locale): Page<AuditValueObject> {
      val validaPageRequest = auditValidator.validationFetchAll(pageRequest)
      val found: RepositoryPage<Audit> = auditRepository.findAll(validaPageRequest)

      return found.toPage(pageRequest) {
         AuditValueObject(it, locale, localizationService)
      }
   }

   fun exists(id: Long): Boolean =
      auditRepository.exists(id = id)

   fun findAuditStatusCounts(@Valid pageRequest: AuditPageRequest, locale: Locale): List<AuditStatusCountDataTransferObject> {
      val validPageRequest = auditValidator.validateFindAuditStatusCounts(pageRequest)

      return auditRepository
         .findAuditStatusCounts(validPageRequest)
         .map { auditStatusCount ->
            AuditStatusCountDataTransferObject(auditStatusCount, locale, localizationService)
         }
   }

   @Validated
   fun create(@Valid vo: AuditCreateValueObject, @Valid employee: EmployeeValueObject, locale: Locale): AuditValueObject {
      val validAudit = auditValidator.validateCreate(vo, employee)

      val audit = auditRepository.insert(validAudit)

      return AuditValueObject(audit, locale, localizationService)
   }

   @Validated
   fun update(@Valid audit: AuditUpdateValueObject, @Valid employee: EmployeeValueObject, locale: Locale): AuditValueObject {
      val (validAuditAction, existingAudit) = auditValidator.validateUpdate(audit, employee, locale)

      existingAudit.actions.add(validAuditAction)

      return AuditValueObject(auditRepository.update(existingAudit), locale, localizationService)
   }
}
