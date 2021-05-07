package com.cynergisuite.middleware.audit.detail

import com.cynergisuite.domain.Page
import com.cynergisuite.domain.PageRequest
import com.cynergisuite.middleware.audit.detail.infrastructure.AuditDetailRepository
import com.cynergisuite.middleware.audit.detail.scan.area.AuditScanAreaDTO
import com.cynergisuite.middleware.audit.infrastructure.AuditRepository
import com.cynergisuite.middleware.authentication.user.User
import com.cynergisuite.middleware.company.Company
import com.cynergisuite.middleware.error.NotFoundException
import io.micronaut.validation.Validated
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import javax.validation.Valid

@Singleton
class AuditDetailService @Inject constructor(
   private val auditRepository: AuditRepository,
   private val auditDetailRepository: AuditDetailRepository,
   private val auditDetailValidator: AuditDetailValidator
) {
   fun fetchById(id: Long, company: Company, locale: Locale): AuditDetailValueObject? =
      auditDetailRepository.findOne(id, company)?.let { transformEntity(it) }

   fun fetchAll(auditId: Long, company: Company, pageRequest: PageRequest, locale: Locale): Page<AuditDetailValueObject> {
      val audit = auditRepository.findOne(auditId, company) ?: throw NotFoundException(auditId)
      val found = auditDetailRepository.findAll(audit, company, pageRequest)

      return found.toPage { transformEntity(it) }
   }

   @Validated
   fun create(auditId: Long, @Valid vo: AuditDetailCreateUpdateDTO, scannedBy: User, locale: Locale): AuditDetailValueObject {
      val auditDetail = auditDetailValidator.validateCreate(auditId, scannedBy, vo)

      return transformEntity(auditDetailRepository.insert(auditDetail))
   }

   @Validated
   fun update(auditId: Long, @Valid vo: AuditDetailCreateUpdateDTO, scannedBy: User, locale: Locale): AuditDetailValueObject {
      val auditDetail = auditDetailValidator.validateUpdate(auditId, scannedBy, vo)

      return transformEntity(auditDetailRepository.update(auditDetail))
   }

   private fun transformEntity(auditDetail: AuditDetailEntity): AuditDetailValueObject {
      return AuditDetailValueObject(entity = auditDetail, auditScanArea = AuditScanAreaDTO(auditDetail.scanArea))
   }
}
