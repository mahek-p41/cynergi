package com.cynergisuite.middleware.audit.exception

import com.cynergisuite.domain.Page
import com.cynergisuite.domain.PageRequest
import com.cynergisuite.middleware.audit.detail.scan.area.AuditScanArea
import com.cynergisuite.middleware.audit.detail.scan.area.AuditScanAreaValueObject
import com.cynergisuite.middleware.audit.detail.scan.area.infrastructure.AuditScanAreaRepository
import com.cynergisuite.middleware.audit.exception.infrastructure.AuditExceptionRepository
import com.cynergisuite.middleware.audit.exception.note.AuditExceptionNote
import com.cynergisuite.middleware.audit.infrastructure.AuditRepository
import com.cynergisuite.middleware.authentication.User
import com.cynergisuite.middleware.employee.EmployeeEntity.Companion.fromUser
import com.cynergisuite.middleware.error.NotFoundException
import com.cynergisuite.middleware.inventory.infrastructure.InventoryRepository
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
   private val auditScanAreaRepository: AuditScanAreaRepository,
   private val inventoryRepository: InventoryRepository,
   private val localizationService: LocalizationService
) {
   fun fetchById(id: Long, locale: Locale): AuditExceptionValueObject? =
      auditExceptionRepository.findOne(id = id)?.let { transformEntity(it, locale) }

   fun fetchAll(auditId: Long, pageRequest: PageRequest, locale: Locale): Page<AuditExceptionValueObject> {
      val audit = auditRepository.findOne(auditId) ?: throw NotFoundException(auditId)
      val found = auditExceptionRepository.findAll(audit, pageRequest)

      return found.toPage { transformEntity(it, locale) }
   }

   fun exists(id: Long): Boolean =
      auditExceptionRepository.exists(id = id)

   @Validated
   fun create(auditId: Long, @Valid vo: AuditExceptionCreateValueObject, @Valid scannedBy: User, locale: Locale): AuditExceptionValueObject {
      auditExceptionValidator.validateCreate(auditId, vo)

      val inventoryId = vo.inventory?.id
      val barcode = vo.barcode
      val scanArea = vo.scanArea
      val auditException = auditExceptionRepository.insert(createAuditException(auditId, scannedBy, vo.exceptionCode!!, inventoryId, barcode, scanArea))

      return transformEntity(auditException, locale)
   }

   private fun createAuditException(auditId: Long, scannedBy: User, exceptionCode: String, inventoryId: Long?, barcode: String?, scanArea: AuditScanAreaValueObject?): AuditExceptionEntity {
      return if (inventoryId != null) {
         val inventory = inventoryRepository.findOne(inventoryId)!!

         AuditExceptionEntity(auditId, inventory, createScanArea(scanArea), fromUser(scannedBy), exceptionCode)
      } else {
         AuditExceptionEntity(auditId, barcode!!, createScanArea(scanArea), fromUser(scannedBy), exceptionCode)
      }
   }

   private fun createScanArea(scanArea: AuditScanAreaValueObject?): AuditScanArea? =
      scanArea?.let { auditScanAreaRepository.findOne(it.value!!) }

   @Validated
   fun update(auditId: Long, @Valid vo: AuditExceptionUpdateValueObject, @Valid enteredBy: User, locale: Locale): AuditExceptionValueObject {
      auditExceptionValidator.validateUpdate(auditId, vo)

      val auditException = auditExceptionRepository.findOne(vo.id!!)!!

      auditException.notes.add(AuditExceptionNote(vo.note!!, enteredBy, auditException))

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
