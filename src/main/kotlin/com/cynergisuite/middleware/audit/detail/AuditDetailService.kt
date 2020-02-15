package com.cynergisuite.middleware.audit.detail

import com.cynergisuite.domain.Page
import com.cynergisuite.domain.PageRequest
import com.cynergisuite.middleware.audit.detail.infrastructure.AuditDetailRepository
import com.cynergisuite.middleware.audit.detail.scan.area.AuditScanAreaValueObject
import com.cynergisuite.middleware.audit.detail.scan.area.infrastructure.AuditScanAreaRepository
import com.cynergisuite.middleware.audit.infrastructure.AuditRepository
import com.cynergisuite.middleware.authentication.user.User
import com.cynergisuite.middleware.company.Company
import com.cynergisuite.middleware.error.NotFoundException
import com.cynergisuite.middleware.inventory.infrastructure.InventoryRepository
import com.cynergisuite.middleware.localization.LocalizationService
import io.micronaut.validation.Validated
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import javax.validation.Valid

@Singleton
class AuditDetailService @Inject constructor(
   private val auditRepository: AuditRepository,
   private val auditDetailRepository: AuditDetailRepository,
   private val auditDetailValidator: AuditDetailValidator,
   private val auditScanAreaRepository: AuditScanAreaRepository,
   private val inventoryRepository: InventoryRepository,
   private val localizationService: LocalizationService
) {
   fun fetchById(id: Long, company: Company, locale: Locale): AuditDetailValueObject? =
      auditDetailRepository.findOne(id, company)?.let { transformEntity(it, locale) }

   @Validated
   fun fetchAll(auditId: Long, company: Company, @Valid pageRequest: PageRequest, locale: Locale): Page<AuditDetailValueObject> {
      val audit = auditRepository.findOne(auditId, company) ?: throw NotFoundException(auditId)
      val found = auditDetailRepository.findAll(audit, company, pageRequest)

      return found.toPage { transformEntity(it, locale) }
   }

   fun exists(id: Long): Boolean =
      auditDetailRepository.exists(id = id)

   @Validated
   fun create(auditId: Long, @Valid vo: AuditDetailCreateDataTransferObject, scannedBy: User, locale: Locale): AuditDetailValueObject {
      val auditDetail = auditDetailValidator.validateCreate(auditId, scannedBy, vo)

      return transformEntity(auditDetailRepository.insert(auditDetail), locale)
   }

   @Validated
   fun update(@Valid vo: AuditDetailValueObject, company: Company, locale: Locale): AuditDetailValueObject {
      val auditDetail = auditDetailValidator.validateUpdate(vo, company)
      val auditDetailUpdated = auditDetailRepository.update(auditDetail)

      return transformEntity(auditDetailUpdated, locale)
   }

   private fun transformEntity(auditDetail: AuditDetailEntity, locale: Locale): AuditDetailValueObject {
      val localizedDescription = auditDetail.scanArea.localizeMyDescription(locale, localizationService)

      return AuditDetailValueObject(entity = auditDetail, auditScanArea = AuditScanAreaValueObject(auditDetail.scanArea, localizedDescription))
   }
}
