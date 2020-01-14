package com.cynergisuite.middleware.audit.exception

import com.cynergisuite.domain.ValidatorBase
import com.cynergisuite.middleware.audit.AuditEntity
import com.cynergisuite.middleware.audit.detail.scan.area.AuditScanArea
import com.cynergisuite.middleware.audit.detail.scan.area.AuditScanAreaValueObject
import com.cynergisuite.middleware.audit.detail.scan.area.infrastructure.AuditScanAreaRepository
import com.cynergisuite.middleware.audit.exception.infrastructure.AuditExceptionRepository
import com.cynergisuite.middleware.audit.exception.note.AuditExceptionNote
import com.cynergisuite.middleware.audit.infrastructure.AuditRepository
import com.cynergisuite.middleware.audit.status.SIGNED_OFF
import com.cynergisuite.middleware.authentication.User
import com.cynergisuite.middleware.employee.EmployeeEntity
import com.cynergisuite.middleware.error.NotFoundException
import com.cynergisuite.middleware.error.ValidationError
import com.cynergisuite.middleware.error.ValidationException
import com.cynergisuite.middleware.inventory.infrastructure.InventoryRepository
import com.cynergisuite.middleware.localization.AuditExceptionMustHaveInventoryOrBarcode
import com.cynergisuite.middleware.localization.AuditHasBeenSignedOffNoNewNotesAllowed
import com.cynergisuite.middleware.localization.AuditMustBeInProgressDiscrepancy
import com.cynergisuite.middleware.localization.AuditScanAreaNotFound
import com.cynergisuite.middleware.localization.AuditUpdateRequiresSignedOffOrNote
import com.cynergisuite.middleware.localization.NotFound
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuditExceptionValidator @Inject constructor (
   private val auditRepository: AuditRepository,
   private val auditExceptionRepository: AuditExceptionRepository,
   private val auditScanAreaRepository: AuditScanAreaRepository,
   private val inventoryRepository: InventoryRepository,
   private val scanAreaRepository: AuditScanAreaRepository
) : ValidatorBase() {
   private val logger: Logger = LoggerFactory.getLogger(AuditExceptionValidator::class.java)

   @Throws(ValidationException::class, NotFoundException::class)
   fun validateCreate(auditId: Long, dataset: String, auditException: AuditExceptionCreateValueObject, scannedBy: User): AuditExceptionEntity {
      doValidation { errors ->
         doSharedValidation(auditId)

         val inventoryId = auditException.inventory?.id
         val barcode = auditException.barcode
         val audit: AuditEntity = auditRepository.findOne(auditId, dataset)!!
         val auditStatus = audit.currentStatus()
         val scanArea = auditException.scanArea

         if (inventoryId != null && inventoryRepository.doesNotExist(inventoryId, dataset)) {
            errors.add(
               ValidationError("inventory.id", NotFound(inventoryId))
            )
         } else if (inventoryId == null && barcode == null) {
            errors.add(
               ValidationError("barcode", AuditExceptionMustHaveInventoryOrBarcode())
            )
         }

         if ("IN-PROGRESS" != auditStatus.value) {
            errors.add(
               ValidationError("audit.status", AuditMustBeInProgressDiscrepancy(auditId))
            )
         }

         if (scanArea != null) {
            val scanAreaValue = scanArea.value

            if (scanAreaValue != null && scanAreaRepository.doesNotExist(scanAreaValue) ) {
               errors.add(
                  ValidationError("audit.scanArea", AuditScanAreaNotFound(scanAreaValue))
               )
            }
         }
      }

      val inventoryId = auditException.inventory?.id
      val barcode = auditException.barcode
      val scanArea = auditException.scanArea

      return createAuditException(auditId, scannedBy, auditException.exceptionCode!!, inventoryId, barcode, scanArea)
   }

   private fun createAuditException(auditId: Long, scannedBy: User, exceptionCode: String, inventoryId: Long?, barcode: String?, scanArea: AuditScanAreaValueObject?): AuditExceptionEntity {
      return if (inventoryId != null) {
         val inventory = inventoryRepository.findOne(inventoryId, scannedBy.myDataset())!!

         AuditExceptionEntity(auditId, inventory, createScanArea(scanArea), EmployeeEntity.fromUser(scannedBy), exceptionCode)
      } else {
         AuditExceptionEntity(auditId, barcode!!, createScanArea(scanArea), EmployeeEntity.fromUser(scannedBy), exceptionCode)
      }
   }

   private fun createScanArea(scanArea: AuditScanAreaValueObject?): AuditScanArea? =
      scanArea?.let { auditScanAreaRepository.findOne(it.value!!) }

   @Throws(ValidationException::class, NotFoundException::class)
   fun validateUpdate(auditId: Long, dataset: String, auditExceptionUpdate: AuditExceptionUpdateValueObject, enteredBy: User): AuditExceptionEntity {
      doValidation { errors ->
         doSharedValidation(auditId)

         val auditExceptionId = auditExceptionUpdate.id!!
         val signedOff = auditExceptionUpdate.signedOff
         val note = auditExceptionUpdate.note
         val audit: AuditEntity = auditRepository.findOne(auditId, dataset)!!

         if (auditExceptionRepository.doesNotExist(auditExceptionId)) {
            errors.add(
               ValidationError("id", NotFound(auditExceptionId))
            )
         }

         if (signedOff == null && note == null) {
            errors.add(
               ValidationError(null, AuditUpdateRequiresSignedOffOrNote())
            )
         }

         if (audit.currentStatus() == SIGNED_OFF) {
            errors.add(
               ValidationError(null, AuditHasBeenSignedOffNoNewNotesAllowed(auditId))
            )
         }
      }

      val auditException = auditExceptionRepository.findOne(auditExceptionUpdate.id!!, dataset)!!
      val auditExceptionNote = auditExceptionUpdate.note
      val notes = auditException.notes

      if (auditExceptionNote != null) {
         notes.add(AuditExceptionNote(auditExceptionNote, enteredBy, auditException))
      }

      return auditException.copy(
         signedOff = auditExceptionUpdate.signedOff ?: false,
         notes = notes
      )
   }

   private fun doSharedValidation(auditId: Long) {
      if (auditRepository.doesNotExist(auditId)) {
         throw NotFoundException(auditId)
      }
   }
}
