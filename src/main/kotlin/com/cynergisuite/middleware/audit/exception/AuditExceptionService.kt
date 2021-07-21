package com.cynergisuite.middleware.audit.exception

import com.cynergisuite.domain.Page
import com.cynergisuite.domain.PageRequest
import com.cynergisuite.middleware.audit.detail.scan.area.AuditScanAreaDTO
import com.cynergisuite.middleware.audit.exception.infrastructure.AuditExceptionRepository
import com.cynergisuite.middleware.audit.infrastructure.AuditRepository
import com.cynergisuite.middleware.authentication.user.User
import com.cynergisuite.middleware.company.Company
import com.cynergisuite.middleware.error.NotFoundException
import java.util.Locale
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuditExceptionService @Inject constructor(
   private val auditRepository: AuditRepository,
   private val auditExceptionRepository: AuditExceptionRepository,
   private val auditExceptionValidator: AuditExceptionValidator
) {
   fun fetchById(id: UUID, company: Company, locale: Locale): AuditExceptionValueObject? =
      auditExceptionRepository.findOne(id = id, company = company)?.let { transformEntity(it) }

   fun fetchAll(auditId: UUID, company: Company, pageRequest: PageRequest, locale: Locale): Page<AuditExceptionValueObject> {
      val audit = auditRepository.findOne(auditId, company) ?: throw NotFoundException(auditId)
      val found = auditExceptionRepository.findAll(audit, company, pageRequest)

      return found.toPage { transformEntity(it) }
   }

   fun exists(id: UUID): Boolean =
      auditExceptionRepository.exists(id = id)

   fun create(auditId: UUID, vo: AuditExceptionCreateDTO, scannedBy: User, locale: Locale): AuditExceptionValueObject {
      val auditException = auditExceptionValidator.validateCreate(auditId, vo, scannedBy)

      return transformEntity(auditExceptionRepository.insert(auditException))
   }

   fun update(auditId: UUID, vo: AuditExceptionUpdateValueObject, enteredBy: User, locale: Locale): AuditExceptionValueObject {
      val auditException = auditExceptionValidator.validateUpdate(auditId, vo, enteredBy)

      return transformEntity(auditExceptionRepository.update(auditException))
   }

   private fun transformEntity(auditException: AuditExceptionEntity): AuditExceptionValueObject {
      return AuditExceptionValueObject(auditException, auditException.scanArea?.let { AuditScanAreaDTO(it) })
   }
}
