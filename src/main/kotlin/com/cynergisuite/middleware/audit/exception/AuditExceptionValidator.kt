package com.cynergisuite.middleware.audit.exception

import com.cynergisuite.domain.ValidatorBase
import com.cynergisuite.middleware.audit.AuditEntity
import com.cynergisuite.middleware.audit.detail.scan.area.infrastructure.AuditScanAreaRepository
import com.cynergisuite.middleware.audit.exception.infrastructure.AuditExceptionRepository
import com.cynergisuite.middleware.audit.infrastructure.AuditRepository
import com.cynergisuite.middleware.error.NotFoundException
import com.cynergisuite.middleware.error.ValidationError
import com.cynergisuite.middleware.error.ValidationException
import com.cynergisuite.middleware.inventory.infrastructure.InventoryRepository
import com.cynergisuite.middleware.localization.AuditExceptionMustHaveInventoryOrBarcode
import com.cynergisuite.middleware.localization.AuditHasBeenSignedOffNoNewNotesAllowed
import com.cynergisuite.middleware.localization.AuditMustBeInProgressDiscrepancy
import com.cynergisuite.middleware.localization.AuditScanAreaNotFound
import com.cynergisuite.middleware.localization.NotFound
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuditExceptionValidator @Inject constructor (
   private val auditRepository: AuditRepository,
   private val auditExceptionsRepository: AuditExceptionRepository,
   private val inventoryRepository: InventoryRepository,
   private val scanAreaRepository: AuditScanAreaRepository
) : ValidatorBase() {
   private val logger: Logger = LoggerFactory.getLogger(AuditExceptionValidator::class.java)

   @Throws(ValidationException::class, NotFoundException::class)
   fun validateCreate(auditId: Long, dataset: String, auditException: AuditExceptionCreateValueObject) {
      doSharedValidation(auditId)

      doValidation { errors ->
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
   }

   @Throws(ValidationException::class, NotFoundException::class)
   fun validateUpdate(auditId: Long, dataset: String, auditExceptionUpdate: AuditExceptionUpdateValueObject) {
      doSharedValidation(auditId)

      doValidation { errors ->
         val auditExceptionId = auditExceptionUpdate.id!!
         val signedOff = auditExceptionUpdate.signedOff!!
         val audit: AuditEntity = auditRepository.findOne(auditId, dataset)!!

         if (auditExceptionsRepository.doesNotExist(auditExceptionId)) {
            errors.add(
               ValidationError("id", NotFound(auditExceptionId))
            )
         }

         if (audit.currentStatus().value == "SIGNED-OFF") {
            errors.add(
               ValidationError(null, AuditHasBeenSignedOffNoNewNotesAllowed(auditId))
            )
         }

         if (!signedOff && auditExceptionUpdate.note == null) {
            errors.add(
               ValidationError(null, AuditHasBeenSignedOffNoNewNotesAllowed(auditId))
            )
         }
      }
   }

   private fun doSharedValidation(auditId: Long) {
      if (auditRepository.doesNotExist(auditId)) {
         throw NotFoundException(auditId)
      }
   }
}
