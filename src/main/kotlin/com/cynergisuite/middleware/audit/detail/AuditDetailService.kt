package com.cynergisuite.middleware.audit.detail

import com.cynergisuite.domain.Page
import com.cynergisuite.domain.PageRequest
import com.cynergisuite.domain.SimpleIdentifiableEntity
import com.cynergisuite.middleware.audit.detail.infrastructure.AuditDetailRepository
import com.cynergisuite.middleware.audit.detail.scan.area.AuditScanAreaValueObject
import com.cynergisuite.middleware.audit.detail.scan.area.infrastructure.AuditScanAreaRepository
import com.cynergisuite.middleware.audit.infrastructure.AuditRepository
import com.cynergisuite.middleware.employee.Employee
import com.cynergisuite.middleware.employee.EmployeeValueObject
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
   fun fetchById(id: Long, locale: Locale): AuditDetailValueObject? =
      auditDetailRepository.findOne(id = id)?.let { transformEntity(it, locale) }

   @Validated
   fun fetchAll(auditId: Long, @Valid pageRequest: PageRequest, locale: Locale): Page<AuditDetailValueObject> {
      val audit = auditRepository.findOne(auditId) ?: throw NotFoundException(auditId)
      val found = auditDetailRepository.findAll(audit, pageRequest)

      return found.toPage { transformEntity(it, locale) }
   }

   fun exists(id: Long): Boolean =
      auditDetailRepository.exists(id = id)

   @Validated
   fun create(auditId: Long, @Valid vo: AuditDetailCreateValueObject, @Valid scannedBy: EmployeeValueObject, locale: Locale): AuditDetailValueObject {
      auditDetailValidator.validateCreate(auditId, vo)

      val scanArea = auditScanAreaRepository.findOne(vo.scanArea!!.value!!)!!
      val inventory = inventoryRepository.findOne(vo.inventory!!.id!!)!!

      val auditDetail = auditDetailRepository.insert(
         AuditDetailEntity(
            inventory,
            scanArea = scanArea,
            scannedBy = Employee(scannedBy),
            audit = SimpleIdentifiableEntity(auditId)
         )
      )

      return transformEntity(auditDetail, locale)
   }

   @Validated
   fun update(@Valid vo: AuditDetailValueObject, locale: Locale): AuditDetailValueObject {
      auditDetailValidator.validateUpdate(vo)

      val auditDetail = auditDetailRepository.findOne(vo.id!!) ?: throw NotFoundException(vo.id!!)
      val auditDetailUpdated = auditDetailRepository.update(
         auditDetail.copy(
            scanArea = auditScanAreaRepository.findOne(vo.scanArea!!.value!!)!!
         )
      )

      return transformEntity(auditDetailUpdated, locale)
   }

   private fun transformEntity(auditDetail: AuditDetailEntity, locale: Locale): AuditDetailValueObject {
      val localizedDescription = auditDetail.scanArea.localizeMyDescription(locale, localizationService)

      return AuditDetailValueObject(entity = auditDetail, auditScanArea = AuditScanAreaValueObject(auditDetail.scanArea, localizedDescription))
   }
}
