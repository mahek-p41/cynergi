package com.cynergisuite.middleware.audit.exception

import com.cynergisuite.domain.Page
import com.cynergisuite.domain.PageRequest
import com.cynergisuite.middleware.audit.detail.scan.area.AuditScanAreaValueObject
import com.cynergisuite.middleware.audit.exception.infrastructure.AuditExceptionRepository
import com.cynergisuite.middleware.audit.infrastructure.AuditRepository
import com.cynergisuite.middleware.authentication.user.User
import com.cynergisuite.middleware.company.Company
import com.cynergisuite.middleware.error.NotFoundException
import com.cynergisuite.middleware.localization.LocalizationService
import io.micronaut.validation.Validated
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import javax.validation.Valid

@Singleton
class AuditExceptionService @Inject constructor(
   private val auditRepository: AuditRepository,
   private val auditExceptionRepository: AuditExceptionRepository,
   private val auditExceptionValidator: AuditExceptionValidator,
   private val localizationService: LocalizationService
) {
   fun fetchById(id: Long, company: Company, locale: Locale): AuditExceptionValueObject? =
      auditExceptionRepository.findOne(id = id, company = company)?.let { transformEntity(it, locale) }

   fun fetchAll(auditId: Long, company: Company, pageRequest: PageRequest, locale: Locale): Page<AuditExceptionValueObject> {
      val audit = auditRepository.findOne(auditId, company) ?: throw NotFoundException(auditId)
      val found = auditExceptionRepository.findAll(audit, company, pageRequest)

      return found.toPage { transformEntity(it, locale) }
   }

   fun exists(id: Long): Boolean =
      auditExceptionRepository.exists(id = id)

   @Validated
   fun create(auditId: Long, @Valid vo: AuditExceptionCreateValueObject, scannedBy: User, locale: Locale): AuditExceptionValueObject {
      val auditException = auditExceptionValidator.validateCreate(auditId, vo, scannedBy)

      return transformEntity(auditExceptionRepository.insert(auditException), locale)
   }

   @Validated
   fun update(auditId: Long, @Valid vo: AuditExceptionUpdateValueObject, @Valid enteredBy: User, locale: Locale): AuditExceptionValueObject {
      val auditException = auditExceptionValidator.validateUpdate(auditId, vo, enteredBy)

      return transformEntity(
         auditExceptionRepository.update(auditException),
         locale
      )
   }

   private fun transformEntity(auditException: AuditExceptionEntity, locale: Locale): AuditExceptionValueObject {
      val scanArea = auditException.scanArea?.localizeMyDescription(locale, localizationService)?.let { AuditScanAreaValueObject(auditException.scanArea, it) }

      return AuditExceptionValueObject(auditException, scanArea)
   }
}
